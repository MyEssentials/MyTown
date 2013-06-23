package ee.lutsu.alpha.mc.mytown.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import ee.lutsu.alpha.mc.mytown.Assert;
import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.NoAccessException;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.entities.TownSetting;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;

public class MyTownResident 
{
	public static List<String> getAutoComplete(ICommandSender cs, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();

		if (!(cs instanceof EntityPlayer)) // no commands for console
			return list;
		
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs);
		if (res.town() == null)
			return list;
		
		if (args.length == 1)
		{
			list.add(Term.TownCmdLeave.toString());
			list.add(Term.TownCmdOnline.toString());
			list.add(Term.TownCmdPerm.toString()); // /t perm town|res|plot [force|(set key [val])]
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase(Term.TownCmdPerm.toString()))
		{
			list.add(Term.TownCmdPermArgsTown.toString());
			list.add(Term.TownCmdPermArgsResident.toString());
			list.add(Term.TownCmdPermArgsPlot.toString());
		}
		else if (args.length == 3 && args[0].equalsIgnoreCase(Term.TownCmdPerm.toString()))
		{
			list.add(Term.TownCmdPermArgs2Set.toString());
			list.add(Term.TownCmdPermArgs2Force.toString());
		}
		else if (args.length == 4 && args[0].equalsIgnoreCase(Term.TownCmdPerm.toString()))
		{
			for (TownSetting s : MyTown.instance.serverSettings.settings)
			{
				list.add(s.getSerializationKey());
			}
		}
		else if (args.length == 5 && args[0].equalsIgnoreCase(Term.TownCmdPerm.toString()) && args[2].equalsIgnoreCase(Term.TownCmdPermArgs2Set.toString()))
		{
			TownSetting s = MyTown.instance.serverSettings.getSetting(args[3]);
			if (s != null)
			{
				Class c = s.getValueClass();
				
				if (c == TownSettingCollection.Permissions.class)
				{
					for (TownSettingCollection.Permissions p : TownSettingCollection.Permissions.values())
						list.add(p.toString());
				}
				else if (c == boolean.class)
				{
					list.add("yes");
					list.add("no");
				}
			}
		}

		return list;
	}
	
	public static boolean handleCommand(ICommandSender cs, String[] args) throws CommandException, NoAccessException
	{
		if (!(cs instanceof EntityPlayer)) // no commands for console
			return false;
		
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs);
		if (res.town() == null)
			return false;
		
		boolean handled = false;
		String color = "2";
		if (args.length < 1)
		{
			Assert.Perm(cs, "mytown.cmd.info");
			handled = true;

			res.town().sendTownInfo(res.onlinePlayer, res.shouldShowTownBlocks());
		}
		else
		{
			if (args.length == 1 && (args[0].equals("?") || args[0].equalsIgnoreCase(Term.CommandHelp.toString())))
			{
				handled = true;
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdLeave.toString(), "", Term.TownCmdLeaveDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdOnline.toString(), "", Term.TownCmdOnlineDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), color));
			}
			else if (args[0].equalsIgnoreCase(Term.TownCmdLeave.toString()))
			{
				Assert.Perm(cs, "mytown.cmd.leave");
				handled = true;

				if (res.rank() == Rank.Mayor)
					throw new CommandException(Term.TownErrMayorsCantLeaveTheTown);
				
				Town t = res.town();
				t.sendNotification(Level.INFO, Term.TownPlayerLeft.toString(res.name()));
				t.removeResident(res);
			}
			else if (args[0].equalsIgnoreCase(Term.TownCmdOnline.toString()))
			{
				Assert.Perm(cs, "mytown.cmd.online");
				handled = true;

				Town t = res.town();
				
				StringBuilder sb = new StringBuilder();
				for (Resident r : t.residents())
				{
					if (!r.isOnline())
						continue;
					
					if (sb.length() > 0)
						sb.append("§2, ");
					
					sb.append(Formatter.formatResidentName(r));
				}
				
				cs.sendChatToPlayer(Term.TownPlayersOnlineStart.toString(sb.toString()));
			}
			else if (args[0].equalsIgnoreCase(Term.TownCmdPerm.toString()))
			{
				handled = true;
				if (args.length < 2)
				{
					cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), color));
					return true;
				}
				
				String node = args[1];
				if (!node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) && !node.equalsIgnoreCase(Term.TownCmdPermArgsResident.toString()) && !node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString()))
				{
					cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), color));
					return true;
				}
				
				if (args.length < 3) // show
				{
					Assert.Perm(cs, "mytown.cmd.perm.show." + node);
					showPermissions(cs, res, node);
				}
				else
				{
					String action = args[2];
					if (action.equalsIgnoreCase(Term.TownCmdPermArgs2Set.toString()) && args.length > 3)
					{
						Assert.Perm(cs, "mytown.cmd.perm.set." + node + "." + args[3]);
						
						setPermissions(cs, res, node, args[3], args.length > 4 ? args[4] : null);
					}
					else if (action.equalsIgnoreCase(Term.TownCmdPermArgs2Force.toString()))
					{
						Assert.Perm(cs, "mytown.cmd.perm.force." + node + "." + (args.length > 3 ? args[3] : "all"));

						flushPermissions(cs, res, node, args.length > 3 ? args[3] : null);
					}
					else
						cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), color));
				}
			}
		}
		
		return handled;
	}
	
	private static TownSettingCollection getPermNode(String node, Resident res) throws CommandException
	{
		TownSettingCollection set = null;
		if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()))
		{
			if (res.town() == null)
				throw new CommandException(Term.ErrPermYouDontHaveTown);
			
			set = res.town().settings;
		}
		else if (node.equalsIgnoreCase(Term.TownCmdPermArgsResident.toString()))
			set = res.settings;
		else if (node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString()))
		{
			TownBlock block = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
			if (block == null || block.town() == null)
				throw new CommandException(Term.ErrPermPlotNotInTown);
			
			if (block.town() != res.town())
				throw new CommandException(Term.ErrPermPlotNotInYourTown);
			
			set = block.settings;
		}
		else
			throw new CommandException(Term.ErrPermSettingCollectionNotFound, node);
		
		return set;
	}

	private static void showPermissions(ICommandSender sender, Resident res, String node) throws CommandException
	{
		TownSettingCollection set = getPermNode(node, res);
		
		String title = "";
		if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()))
			title = "your town '" + res.town().name() + "' (default for residents)";
		else if (node.equalsIgnoreCase(Term.TownCmdPermArgsResident.toString()))
			title = "you '" + res.name() + "'";
		else if (node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString()))
		{
			TownBlock block = (TownBlock)set.tag;
			title = String.format("the plot @ dim %s, %s,%s owned by '%s'", block.worldDimension(), block.x(), block.z(), block.ownerDisplay());
		}
		
		set.show(sender, title, node, false);
	}
	
	private static void flushPermissions(ICommandSender sender, Resident res, String node, String perm) throws CommandException
	{
		TownSettingCollection set = getPermNode(node, res);
		
		if (set.childs.size() < 1)
			throw new CommandException(Term.ErrPermNoChilds);
		
		if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) && res.rank() == Rank.Resident)
			throw new CommandException(Term.ErrPermRankNotEnough);
		
		set.forceChildsToInherit(perm);
		sender.sendChatToPlayer(Term.PermForced.toString(node, perm == null || perm.equals("") ? "all" : perm));
	}
	
	private static void setPermissions(ICommandSender sender, Resident res, String node, String key, String val) throws CommandException
	{
		TownSettingCollection set = getPermNode(node, res);

		if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) && res.rank() == Rank.Resident)
			throw new CommandException(Term.ErrPermRankNotEnough);
		
		if (node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString()) && res.rank() == Rank.Resident)
		{
			TownBlock b = (TownBlock)set.tag;
			if (b.owner() != res)
				throw new CommandException(Term.ErrPermRankNotEnough);
		}
		
		set.setValue(key, val);
		
		showPermissions(sender, res, node);
		sender.sendChatToPlayer(Term.PermSetDone.toString(key, node));
	}
}
