package ee.lutsu.alpha.mc.mytown.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import ee.lutsu.alpha.mc.mytown.Assert;
import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.NoAccessException;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;
import ee.lutsu.alpha.mc.mytown.event.PlayerEvents;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;

public class MyTownMayor 
{
	public static List<String> getAutoComplete(ICommandSender cs, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();

		if (!(cs instanceof EntityPlayer)) // no commands for console
			return list;
		
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs);
		if (res.town() == null || res.rank() != Rank.Mayor)
			return list;
		
		if (args.length == 1)
		{
			list.add(Term.TownCmdAssistant.toString());
			list.add(Term.TownCmdMayor.toString());
			list.add(Term.TownCmdRename.toString());
			list.add(Term.TownCmdDelete.toString());
		}
		else if (args.length == 2 && (args[0].equals("?") || args[0].equalsIgnoreCase(Term.CommandHelp.toString())))
		{
			list.add(Term.CommandHelpMayor.toString());
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase(Term.TownCmdAssistant.toString()))
		{
			list.add(Term.TownCmdAssistantArgs1.toString());
			list.add(Term.TownCmdAssistantArgs2.toString());
		}
		else if (args.length == 3 && args[0].equalsIgnoreCase(Term.TownCmdAssistant.toString()))
		{
			String action = args[1];
			for (Resident r : res.town().residents())
			{
				if (action == Term.TownCmdAssistantArgs1.toString() && r.rank() != Rank.Resident)
					continue;
				if (action == Term.TownCmdAssistantArgs2.toString() && r.rank() != Rank.Assistant)
					continue;
				
				if (r != res)
					list.add(r.name());
			}
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase(Term.TownCmdMayor.toString()))
		{
			for (Resident r : res.town().residents())
			{
				if (r != res)
					list.add(r.name());
			}
		}

		return list;
	}
	
	public static boolean handleCommand(ICommandSender cs, String[] args) throws CommandException, NoAccessException
	{
		if (args.length < 1)
			return false;
		
		if (!(cs instanceof EntityPlayer)) // no commands for console
			return false;
		
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs);
		if (res.town() == null || res.rank() != Rank.Mayor)
			return false;
		
		boolean handled = false;
		String color = "c";
		if (args[0].equals("?") || args[0].equalsIgnoreCase(Term.CommandHelp.toString()))
		{
			if (args.length < 2)
			{
				handled = true;
				cs.sendChatToPlayer(Formatter.formatGroupCommand(Term.CommandHelp.toString(), Term.CommandHelpMayor.toString(), Term.CommandHelpMayorDesc.toString(), color));
			}
			else if (args[1].equalsIgnoreCase(Term.CommandHelpMayor.toString()))
			{
				handled = true;
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdAssistant.toString(), Term.TownCmdAssistantArgs.toString(), Term.TownCmdAssistantDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdMayor.toString(), Term.TownCmdMayorArgs.toString(), Term.TownCmdMayorDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdRename.toString(), Term.TownCmdRenameArgs.toString(), Term.TownCmdRenameDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdDelete.toString(), "", Term.TownCmdDeleteDesc.toString(), color));
			}
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdAssistant.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.assistant");
			handled = true;

			if (args.length != 3)
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdAssistant.toString(), Term.TownCmdAssistantArgs.toString(), Term.TownCmdAssistantDesc.toString(), color));
			else
			{
				String cmd = args[1];
				String name = args[2];
				
				Resident r = MyTownDatasource.instance.getResident(name);
				if (r == null)
					throw new CommandException(Term.TownErrPlayerNotFound);
				if (r == res)
					throw new CommandException(Term.TownErrCannotDoWithYourself);
				if (r.town() != res.town())
					throw new CommandException(Term.TownErrPlayerNotInYourTown);

				if (cmd.equalsIgnoreCase(Term.TownCmdAssistantArgs1.toString())) // add
				{
					if (r.rank() == Rank.Mayor)
						throw new CommandException(Term.TownErrCannotUseThisDemoteMayor);
					if (r.rank() == Rank.Assistant)
						throw new CommandException(Term.TownErrPlayerIsAlreadyAssistant);
					
					res.town().setResidentRank(r, Rank.Assistant);
					res.town().sendNotification(Level.INFO, Term.TownPlayerPromotedToAssistant.toString(r.name()));
				}
				else if (cmd.equalsIgnoreCase(Term.TownCmdAssistantArgs2.toString())) // remove
				{
					if (r.rank() != Rank.Assistant)
						throw new CommandException(Term.TownErrPlayerIsNotAssistant);
					
					res.town().setResidentRank(r, Rank.Resident);
					res.town().sendNotification(Level.INFO, Term.TownPlayerDemotedFromAssistant.toString(r.name()));
				}
				else
					cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdAssistant.toString(), Term.TownCmdAssistantArgs.toString(), Term.TownCmdAssistantDesc.toString(), color));
			}
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdMayor.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.mayor");
			handled = true;

			if (args.length != 2)
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdMayor.toString(), Term.TownCmdMayorArgs.toString(), Term.TownCmdMayorDesc.toString(), color));
			else
			{
				String name = args[1];
				
				Resident r = MyTownDatasource.instance.getResident(name);
				if (r == null)
					throw new CommandException(Term.TownErrPlayerNotFound);
				if (r == res)
					throw new CommandException(Term.TownErrCannotDoWithYourself);
				if (r.town() != res.town())
					throw new CommandException(Term.TownErrPlayerNotInYourTown);
				
				if (!Permissions.canAccess(r, "mytown.cmd.new")) 
					throw new CommandException(Term.TownErrPlayerDoesntHaveAccessToTownManagement);
				

				res.town().setResidentRank(r, Rank.Mayor);
				res.town().setResidentRank(res, Rank.Assistant);
				 
				res.town().sendNotification(Level.INFO, Term.TownPlayerPromotedToMayor.toString(r.name()));
			}
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdDelete.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.delete");
			handled = true;

			if (args.length == 2 && args[1].equalsIgnoreCase("ok"))
			{
				String name = res.town().name();
				res.town().deleteTown();
				
				// emulate that the player just entered it
				res.checkLocation();
				
				String msg = Term.TownBroadcastDeleted.toString(name);
				for(Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
				{
					((EntityPlayer)obj).sendChatToPlayer(msg);
				}
			}
			else
				cs.sendChatToPlayer(Term.TownCmdDeleteAction.toString());
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdRename.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.rename");
			handled = true;

			if (args.length == 2)
			{
				res.town().setTownName(args[1]);
				res.town().sendNotification(Level.INFO, Term.TownRenamed.toString(res.town().name()));
			}
			else
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdRename.toString(), Term.TownCmdRenameArgs.toString(), Term.TownCmdRenameDesc.toString(), color));
		}
		
		return handled;
	}
}
