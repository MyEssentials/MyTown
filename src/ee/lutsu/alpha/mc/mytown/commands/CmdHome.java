package ee.lutsu.alpha.mc.mytown.commands;

import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Cost;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.PayHandler;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.SavedHome;

public class CmdHome extends CommandBase
{
	@Override
	public String getCommandName() 
	{
		return "home";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cs)
	{
		return cs instanceof EntityPlayerMP && Permissions.canAccess(cs, "mytown.ecmd.home");
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) 
	{
		EntityPlayerMP pl = (EntityPlayerMP)cs;
		Resident res = MyTownDatasource.instance.getOrMakeResident(pl);
		
		try 
		{
			SavedHome h = res.home.get(args.length == 0 ? null : args[0]);
			
			if (!res.home.hasHomes())
				throw new CommandException(Term.HomeCmdNoHomes);
			
			if (h == null)
				throw new CommandException(Term.HomeCmdNoHomeByName);
			
			res.pay.requestPayment("hometeleport", Cost.HomeTeleport.item, new PayHandler.IDone() 
			{
				@Override
				public void run(Resident player, Object[] args) 
				{
					teleport(player, (SavedHome)args[0]);
				}
			}, h);

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
	
	public static void teleport(Resident res, SavedHome h)
	{
		if (Cost.HomeTeleport.item != null && Resident.teleportToHomeWaitSeconds > 0)
			res.onlinePlayer.sendChatToPlayer(Term.HomeCmdDontMove.toString());
		
		res.asyncStartSpawnTeleport(h);
	}
}
