package ee.lutsu.alpha.mc.mytown.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import ee.lutsu.alpha.mc.mytown.Assert;
import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.NoAccessException;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Nation;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;

public class MyTownNation 
{
	public static List<String> getAutoComplete(ICommandSender cs, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();

		if (args.length == 1)
		{
			list.add(Term.TownCmdNation.toString());
		}
		else if (args.length == 2 && (args[0].equals("?") || args[0].equalsIgnoreCase(Term.CommandHelp.toString())))
		{
			list.add(Term.CommandHelpNation.toString());
		}
		else if (args.length > 1 && args[0].equalsIgnoreCase(Term.TownCmdNation.toString()))
		{
			if (args.length == 2)
			{
				list.add(Term.TownCmdNationInfo.toString());
				list.add(Term.TownCmdNationList.toString());
			}
			else if (args.length == 3 && args[1].equalsIgnoreCase(Term.TownCmdNationInfo.toString()))
			{
				for (Nation n : MyTownDatasource.instance.nations)
					list.add(n.name());
			}
		}
		
		if (cs instanceof EntityPlayer) // no commands for console from here
		{
			Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs);
			if (res.town() != null && res.rank() == Rank.Mayor)
			{
				Town town = res.town();
				Nation nation = town.nation();
				
				if (nation == null) // not in nation - new, accept, reject
				{
					if (args.length == 1)
					{
						list.add(Term.TownCmdAccept.toString());
						list.add(Term.TownCmdDeny.toString());
					}
					else if (args.length > 1 && args[0].equalsIgnoreCase(Term.TownCmdNation.toString()))
					{
						if (args.length == 2)
						{
							list.add(Term.TownCmdNationNew.toString());
						}
					}
				}
				else if (nation.capital() == res.town()) // capitol city - invite, delete, kick, transfer
				{
					if (args.length > 1 && args[0].equalsIgnoreCase(Term.TownCmdNation.toString()))
					{
						if (args.length == 2)
						{
							list.add(Term.TownCmdNationInvite.toString());
							list.add(Term.TownCmdNationKick.toString());
							list.add(Term.TownCmdNationTransfer.toString());
							list.add(Term.TownCmdNationDel.toString());
						}
						else if (args.length == 3 && args[1].equalsIgnoreCase(Term.TownCmdNationInvite.toString()))
						{
							for (Town n : MyTownDatasource.instance.towns)
								if (n.nation() == null)
									list.add(n.name());
						}
						else if (args.length == 3 && (
								args[1].equalsIgnoreCase(Term.TownCmdNationKick.toString()) ||
								args[1].equalsIgnoreCase(Term.TownCmdNationTransfer.toString())))
						{
							for (Town n : nation.towns())
								list.add(n.name());
						}
					}
				}
				else // member town - leave
				{
					if (args.length > 1 && args[0].equalsIgnoreCase(Term.TownCmdNation.toString()))
					{
						if (args.length == 2)
						{
							list.add(Term.TownCmdNationLeave.toString());
						}
					}
				}
			}
		}
		
		return list;
	}
	
	public static boolean handleCommand(ICommandSender cs, String[] args) throws CommandException, NoAccessException
	{
		boolean nonresident = cs instanceof EntityPlayer && MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs).town() == null;
		boolean handled = false;
		
		String color = "b";
		
		if (
				(nonresident && args.length < 1) || 
				(!nonresident && args.length > 0 && 
						(args[0].equals("?") || 
						args[0].equalsIgnoreCase(Term.CommandHelp.toString()) ||
						(args[0].equalsIgnoreCase(Term.TownCmdNation.toString()) && args.length < 2)
						)
				)
		)
		{
			if (args.length < 2 && (args.length < 1 || !args[0].equalsIgnoreCase(Term.TownCmdNation.toString())))
			{
				handled = true;
				cs.sendChatToPlayer(Formatter.formatGroupCommand(Term.CommandHelp.toString(), Term.CommandHelpNation.toString(), Term.CommandHelpNationDesc.toString(), color));
			}
			else if ((args[0].equalsIgnoreCase(Term.TownCmdNation.toString()) && args.length < 2) || args[1].equalsIgnoreCase(Term.CommandHelpNation.toString()))
			{
				handled = true;
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationInfo.toString(), Term.TownCmdNationInfoArgs.toString(), Term.TownCmdNationInfoDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationList.toString(), "", Term.TownCmdNationListDesc.toString(), color));
			}
		}
		else if (args.length > 1 && args[0].equalsIgnoreCase(Term.TownCmdNation.toString()) && args[1].equalsIgnoreCase(Term.TownCmdNationInfo.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.nationinfo");
			handled = true;

			if (args.length < 3 && cs instanceof EntityPlayer)
			{
				Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs);
				if (res.town() == null || res.town().nation() == null)
					throw new CommandException(Term.TownErrNationSelfNotPartOfNation);

				res.town().nation().sendNationInfo(cs);
			}
			else if (args.length == 3)
			{
				Nation n = MyTownDatasource.instance.getNation(args[2]);
				if (n == null)
					throw new CommandException(Term.TownErrNationNotFound, args[2]);

				n.sendNationInfo(cs);
			}
			else
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationInfo.toString(), Term.TownCmdNationInfoArgs.toString(), Term.TownCmdNationInfoDesc.toString(), color));
		}
		else if (args.length > 1 && args[0].equalsIgnoreCase(Term.TownCmdNation.toString()) && args[1].equalsIgnoreCase(Term.TownCmdNationList.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.nationlist");
			handled = true;

			ArrayList<Nation> sorted = new ArrayList<Nation>(MyTownDatasource.instance.nations);
			
			Collections.sort(sorted, new Comparator<Nation>()
			{
				@Override
				public int compare(Nation arg0, Nation arg1)
				{
					return Integer.compare(arg1.towns().size(), arg0.towns().size());
				}
			});
			
			StringBuilder sb = new StringBuilder();
			sb.append(Term.TownCmdNationListStart.toString(sorted.size(), ""));
			int i = 0;
			
			for (Nation e : sorted)
			{
				String n = Term.TownCmdNationListEntry.toString(e.name(), e.towns().size());
				if (i > 0)
				{
					sb.append(", ");
					/*
					if (sb.length() + n.length() > 70)
					{
						cs.sendChatToPlayer(sb.toString());
						sb = new StringBuilder();
						i = 0;
					}*/
				}
				i++;
				sb.append(n);
			}
			
			if (sb.length() > 0)
				cs.sendChatToPlayer(sb.toString());
		}
		
		if (args.length < 1) // "/t" command
			return handled;
		
		if (!(cs instanceof EntityPlayer)) // no commands for console from here
			return handled;
		
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs);
		if (res.town() == null || res.rank() != Rank.Mayor)
			return handled;
		
		Town town = res.town();
		Nation nation = town.nation();
		
		if (nation == null) // not in nation - new, accept, reject
		{
			if (
					(nonresident && args.length < 1) || 
					(!nonresident && args.length > 0 && 
							(args[0].equals("?") || 
							args[0].equalsIgnoreCase(Term.CommandHelp.toString()) ||
							(args[0].equalsIgnoreCase(Term.TownCmdNation.toString()) && args.length < 2)
							)
					)
			)
			{
				if (args.length < 2 && (args.length < 1 || !args[0].equalsIgnoreCase(Term.TownCmdNation.toString())))
				{
				}
				else if ((args[0].equalsIgnoreCase(Term.TownCmdNation.toString()) && args.length < 2) || args[1].equalsIgnoreCase(Term.CommandHelpNation.toString()))
				{
					handled = true;
					cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationNew.toString(), Term.TownCmdNationNewArgs.toString(), Term.TownCmdNationNewDesc.toString(), color));
					cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdAccept.toString(), "", Term.TownCmdAcceptDesc2.toString(), color));
					cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdDeny.toString(), "", Term.TownCmdDenyDesc2.toString(), color));
				}
			}
			else if (args[0].equalsIgnoreCase(Term.TownCmdAccept.toString()))
			{
				Assert.Perm(cs, "mytown.cmd.nationaccept");
				handled = true;

				if (town.pendingNationInvitation == null)
					throw new CommandException(Term.TownErrNationYouDontHavePendingInvitations);
				
				Nation n = town.pendingNationInvitation;
				n.addTown(town);
				
				n.sendNotification(Level.INFO, Term.NationTownJoinedNation.toString(town.name()));
			}
			else if (args[0].equalsIgnoreCase(Term.TownCmdDeny.toString()))
			{
				Assert.Perm(cs, "mytown.cmd.nationdeny");
				handled = true;

				if (town.pendingNationInvitation == null)
					throw new CommandException(Term.TownErrNationYouDontHavePendingInvitations);
				
				town.pendingNationInvitation = null;

				cs.sendChatToPlayer(Term.NationPlayerDeniedInvitation.toString());
			}
			else if (!args[0].equalsIgnoreCase(Term.TownCmdNation.toString()))
				return handled;
			else if (args[1].equalsIgnoreCase(Term.TownCmdNationNew.toString()))
			{
				Assert.Perm(cs, "mytown.cmd.nationnew");
				handled = true;

				if (args.length == 3)
				{
					String name = args[2];
					Nation n = new Nation(name, town);
					
					String msg = Term.NationBroadcastCreated.toString(town.name(), n.name());
					for(Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
					{
						((EntityPlayer)obj).sendChatToPlayer(msg);
					}
					
					town.sendTownInfo(cs, res.shouldShowTownBlocks());
				}
				else
					cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationNew.toString(), Term.TownCmdNationNewArgs.toString(), Term.TownCmdNationNewDesc.toString(), color));
			}
		}
		else if (nation.capital() == res.town()) // capitol city - invite, delete, kick, transfer
		{
			if (args.length < 2 && (args.length < 1 || !args[0].equalsIgnoreCase(Term.TownCmdNation.toString())))
			{
			}
			else if ((args[0].equalsIgnoreCase(Term.TownCmdNation.toString()) && args.length < 2) || args[1].equalsIgnoreCase(Term.CommandHelpNation.toString()))
			{
				handled = true;
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationInvite.toString(), Term.TownCmdNationInviteArgs.toString(), Term.TownCmdNationInviteDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationKick.toString(), Term.TownCmdNationKickArgs.toString(), Term.TownCmdNationKickDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationTransfer.toString(), Term.TownCmdNationTransferArgs.toString(), Term.TownCmdNationTransferDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationDel.toString(), "", Term.TownCmdNationDelDesc.toString(), color));
			}
			else if (!args[0].equalsIgnoreCase(Term.TownCmdNation.toString()))
				return handled;
			else if (args[1].equalsIgnoreCase(Term.TownCmdNationInvite.toString()))
			{
				Assert.Perm(cs, "mytown.cmd.nationinvite");
				handled = true;

				if (args.length == 3)
				{
					Town t = MyTownDatasource.instance.getTown(args[2]);
					if (t == null)
						throw new CommandException(Term.TownErrNotFound, args[2]);
					if (t == town)
						throw new CommandException(Term.TownErrNationInvitingSelf);
					
					boolean mayorOnline = false;
					for (Resident r : t.residents())
					{
						if (r.rank() == Rank.Mayor && r.isOnline())
						{
							mayorOnline = true;
							r.onlinePlayer.sendChatToPlayer(Term.NationInvitation.toString(res.name(), nation.name()));
							cs.sendChatToPlayer(Term.NationInvitedPlayer.toString(r.name(), t.name()));
						}
					}
					if (mayorOnline)
					{
						t.pendingNationInvitation = nation;
					}
					else
					{
						cs.sendChatToPlayer(Term.TownErrNationNoMayorOnline.toString(t.name()));
					}
				}
				else
					cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationKick.toString(), Term.TownCmdNationKickArgs.toString(), Term.TownCmdNationKickDesc.toString(), color));
			}
			else if (args[1].equalsIgnoreCase(Term.TownCmdNationKick.toString()))
			{
				Assert.Perm(cs, "mytown.cmd.nationkick");
				handled = true;

				if (args.length == 3)
				{
					Town t = MyTownDatasource.instance.getTown(args[2]);
					if (t == null)
						throw new CommandException(Term.TownErrNotFound, args[2]);
					
					if (t.nation() != nation)
						throw new CommandException(Term.TownErrNationNotPartOfNation);
					
					if (t == town)
						throw new CommandException(Term.TownErrNationCannotKickSelf);
					
					nation.removeTown(t);
					t.sendNotification(Level.INFO, Term.NationLeft.toString(nation.name()));
					
					cs.sendChatToPlayer(Term.TownKickedFromNation.toString(t.name()));
				}
				else
					cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationTransfer.toString(), Term.TownCmdNationTransferArgs.toString(), Term.TownCmdNationTransferDesc.toString(), color));
			}
			else if (args[1].equalsIgnoreCase(Term.TownCmdNationTransfer.toString()))
			{
				Assert.Perm(cs, "mytown.cmd.nationtransfer");
				handled = true;

				if (args.length == 3)
				{
					Town t = MyTownDatasource.instance.getTown(args[2]);
					if (t == null)
						throw new CommandException(Term.TownErrNotFound, args[2]);
					
					if (t.nation() != nation)
						throw new CommandException(Term.TownErrNationNotPartOfNation);
					
					if (t == town)
						throw new CommandException(Term.TownErrNationCannotTransferSelf);
					
					nation.setCapital(t);
					t.sendNotification(Level.INFO, Term.NationNowCapital.toString(nation.name()));
					
					cs.sendChatToPlayer(Term.NationCapitalTransfered.toString(t.name()));
				}
				else
					cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationInvite.toString(), Term.TownCmdNationInviteArgs.toString(), Term.TownCmdNationInviteDesc.toString(), color));
			}
			else if (args[1].equalsIgnoreCase(Term.TownCmdNationDel.toString()))
			{
				Assert.Perm(cs, "mytown.cmd.nationdelete");
				handled = true;

				if (args.length == 3 && args[2].equalsIgnoreCase("yes"))
				{
					nation.delete();

					String msg = Term.NationBroadcastDeleted.toString(nation.name());
					for(Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
					{
						((EntityPlayer)obj).sendChatToPlayer(msg);
					}
					
					town.sendTownInfo(cs, res.shouldShowTownBlocks());
				}
				else
					cs.sendChatToPlayer(Term.NationDeleteConfirmation.toString());
			}
		}
		else // member town - leave
		{
			if (args.length < 2 && (args.length < 1 || !args[0].equalsIgnoreCase(Term.TownCmdNation.toString())))
			{
			}
			else if ((args[0].equalsIgnoreCase(Term.TownCmdNation.toString()) && args.length < 2) || args[1].equalsIgnoreCase(Term.CommandHelpNation.toString()))
			{
				handled = true;
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationLeave.toString(), "", Term.TownCmdNationLeaveDesc.toString(), color));
			}
			else if (!args[0].equalsIgnoreCase(Term.TownCmdNation.toString()))
				return handled;
			else if (args[1].equalsIgnoreCase(Term.TownCmdNationLeave.toString()))
			{
				Assert.Perm(cs, "mytown.cmd.nationleave");
				handled = true;

				nation.removeTown(town);
				town.sendNotification(Level.INFO, Term.NationLeft.toString(nation.name()));
			}
		}
		
		return handled;
	}
}
