package ee.lutsu.alpha.mc.mytown.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.event.PlayerEvents;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;

public class MyTownNonResident 
{
	public static List<String> getAutoComplete(ICommandSender cs, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();

		if (!(cs instanceof EntityPlayer)) // no commands for console
			return list;
		
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs);
		if (res.town() != null)
			return list;
		
		if (args.length == 1)
		{
			list.add(Term.TownCmdNew.toString());
			list.add(Term.TownCmdAccept.toString());
			list.add(Term.TownCmdDeny.toString());
		}

		return list;
	}
	
	public static boolean handleCommand(ICommandSender cs, String[] args) throws Exception
	{
		if (!(cs instanceof EntityPlayer)) // no commands for console
			return false;
		
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs);
		if (res.town() != null)
			return false;
		
		boolean handled = false;
		String color = "2";
		if (args.length < 1 || (args.length == 1 && args[0].equals("?") || args[0].equalsIgnoreCase(Term.CommandHelp.toString())))
		{
			handled = true;
			cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNew.toString(), Term.TownCmdNewArgs.toString(), Term.TownCmdNewDesc.toString(), color));
			cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdAccept.toString(), "", Term.TownCmdAcceptDesc.toString(), color));
			cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdDeny.toString(), "", Term.TownCmdDenyDesc.toString(), color));
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdNew.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.new.dim" + res.onlinePlayer.dimension);
			handled = true;

			if (args.length < 2 || args.length > 2)
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNew.toString(), Term.TownCmdNewArgs.toString(), Term.TownCmdNewDesc.toString(), color));
			else
			{
				TownBlock home = MyTownDatasource.instance.getOrMakeBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
				try
				{
					Town.assertNewTownParams(args[1], res, home);
				}
				catch (Exception e)
				{
					if (home != null && home.town() == null)
						MyTownDatasource.instance.unloadBlock(home);
					
					throw e;
				}
				
				res.pay.requestPayment("townnew", Cost.TownNew.item, new PayHandler.IDone() 
				{
					@Override
					public void run(Resident res, Object[] ar2) 
					{
						String[] args = (String[])ar2[0];
						
						Town t = null;
						try { // should never crash because we're doing the same checks before
							t = new Town(args[1], res, (TownBlock)ar2[1]);
						} catch (CommandException e) {
							Log.severe("Town creating failed after taking payment", e);
						}
						
						// emulate that the player just entered it
						res.checkLocation();

						String msg = Term.TownBroadcastCreated.toString(res.name(), t.name());
						for(Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
						{
							((EntityPlayer)obj).sendChatToPlayer(msg);
						}
						
						t.sendTownInfo(res.onlinePlayer, res.shouldShowTownBlocks());
					}
				}, (Object)args, home);
			}
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdAccept.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.accept");
			handled = true;

			if (res.inviteActiveFrom == null)
				throw new CommandException(Term.TownErrYouDontHavePendingInvitations);
			
			res.setRank(Rank.Resident);
			res.inviteActiveFrom.addResident(res);
			
			res.inviteActiveFrom.sendNotification(Level.INFO, Term.TownPlayerJoinedTown.toString(res.name()));
			res.inviteActiveFrom = null;
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdDeny.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.deny");
			handled = true;

			if (res.inviteActiveFrom == null)
				throw new CommandException(Term.TownErrYouDontHavePendingInvitations);
			
			res.inviteActiveFrom = null;

			res.onlinePlayer.sendChatToPlayer(Term.TownPlayerDeniedInvitation.toString());
		}
		
		return handled;
	}
}
