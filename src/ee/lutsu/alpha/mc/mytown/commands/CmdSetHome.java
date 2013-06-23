package ee.lutsu.alpha.mc.mytown.commands;

import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ee.lutsu.alpha.mc.mytown.Assert;
import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Cost;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.NoAccessException;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.PayHandler;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.SavedHome;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection;

public class CmdSetHome extends CommandBase
{
	@Override
	public String getCommandName() 
	{
		return "sethome";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cs)
	{
		return cs instanceof EntityPlayerMP && Permissions.canAccess(cs, "mytown.ecmd.sethome");
	}
	
	@Override
	public String getCommandUsage(ICommandSender cs) 
	{
		return getCommandName() + " [name] - Sets a new home location";
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) 
	{
		EntityPlayerMP pl = (EntityPlayerMP)cs;
		Resident res = MyTownDatasource.instance.getOrMakeResident(pl);
		
		try 
		{
			if (!res.canInteract(pl.dimension, (int)pl.posX, (int)pl.posY, (int)pl.posZ, TownSettingCollection.Permissions.Build))
				throw new CommandException(Term.HomeCmdCannotSetHere);
			
			res.home.assertSetHome(args.length == 0 ? null : args[0], pl);
			
			ItemStack request = null;
			SavedHome h = res.home.get(args.length == 0 ? null : args[0]);
			String action = null;
			if (h == null)
			{
				if (Cost.HomeSetNew.item != null)
				{
					request = Cost.HomeSetNew.item.copy();
					request.stackSize += Cost.homeSetNewAdditional * res.home.size();
				}
			}
			else
			{
				if (Cost.HomeReplace.item != null)
					request = Cost.HomeReplace.item;
			}
			
			res.pay.requestPayment(h == null ? "homenew" : "homereplace", request, new PayHandler.IDone()
			{
				@Override
				public void run(Resident res, Object[] args) 
				{
					setHome(res, (EntityPlayerMP)res.onlinePlayer, (String[])args[0]);
				}
			}, (Object)args);
		} 
		catch (NoAccessException ex)
		{
			cs.sendChatToPlayer(ex.toString());
		}
		catch (CommandException ex)
		{
			cs.sendChatToPlayer(Formatter.commandError(Level.WARNING, ex.errorCode.toString(ex.args)));
		}
		catch (Throwable ex)
		{
			Log.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
			cs.sendChatToPlayer(Formatter.commandError(Level.SEVERE, ex.toString()));
		}
	}
	
	public static void setHome(Resident res, EntityPlayerMP pl, String[] args)
	{
		res.home.set(args.length == 0 ? null : args[0], pl);
		pl.sendChatToPlayer(args.length == 0 ? Term.HomeCmdHomeSet.toString() : Term.HomeCmdHome2Set.toString(args[0]));
	}
}
