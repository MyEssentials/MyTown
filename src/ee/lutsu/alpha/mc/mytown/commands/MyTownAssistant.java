package ee.lutsu.alpha.mc.mytown.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.common.collect.Lists;

import ee.lutsu.alpha.mc.mytown.Assert;
import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Cost;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.NoAccessException;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.PayHandler;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.entities.TownSetting;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;
import ee.lutsu.alpha.mc.mytown.event.PlayerEvents;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;

public class MyTownAssistant 
{
	public static List<String> getAutoComplete(ICommandSender cs, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();

		if (!(cs instanceof EntityPlayer)) // no commands for console
			return list;
		
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer)cs);
		if (res.town() == null || (res.rank() != Rank.Mayor && res.rank() != Rank.Assistant))
			return list;
		
		if (args.length == 1)
		{
			list.add(Term.TownCmdClaim.toString());
			list.add(Term.TownCmdUnclaim.toString());
			list.add(Term.TownCmdInvite.toString());
			list.add(Term.TownCmdKick.toString());
			list.add(Term.TownCmdSetSpawn.toString());
			list.add(Term.TownCmdPlot.toString());
		}
		else if (args.length == 2 && (args[0].equals("?") || args[0].equalsIgnoreCase(Term.CommandHelp.toString())))
		{
			list.add(Term.CommandHelpAssistant.toString());
		}
		else if (args.length == 2 && (args[0].equalsIgnoreCase(Term.TownCmdClaim.toString()) || args[0].equalsIgnoreCase(Term.TownCmdUnclaim.toString())))
		{
			list.add(Term.TownCmdClaimArgs1.toString());
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase(Term.TownCmdInvite.toString()))
		{
			for (Resident r : MyTownDatasource.instance.residents)
			{
				if (r.town() == null)
					list.add(r.name());
			}
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase(Term.TownCmdKick.toString()))
		{
			for (Resident r : res.town().residents())
			{
				if (r != res && r.rank() != Rank.Mayor && (res.rank() == Rank.Mayor || r.rank() != Rank.Assistant))
					list.add(r.name());
			}
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase(Term.TownCmdPlot.toString()))
		{
			for (Resident r : res.town().residents())
			{
				list.add(r.name());
			}
		}
		else if (args.length == 3 && args[0].equalsIgnoreCase(Term.TownCmdPlot.toString()))
		{
			list.add("rect");
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
		if (res.town() == null || (res.rank() != Rank.Mayor && res.rank() != Rank.Assistant))
			return false;
		
		boolean handled = false;
		String color = "6";
		if (args[0].equals("?") || args[0].equalsIgnoreCase(Term.CommandHelp.toString()))
		{
			if (args.length < 2)
			{
				cs.sendChatToPlayer(Formatter.formatGroupCommand(Term.CommandHelp.toString(), Term.CommandHelpAssistant.toString(), Term.CommandHelpAssistantDesc.toString(), color));
				handled = true;
			}
			else if (args[1].equalsIgnoreCase(Term.CommandHelpAssistant.toString()))
			{
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdClaim.toString(), Term.TownCmdClaimArgs.toString(), Term.TownCmdClaimDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdUnclaim.toString(), Term.TownCmdUnclaimArgs.toString(), Term.TownCmdUnclaimDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdInvite.toString(), Term.TownCmdInviteArgs.toString(), Term.TownCmdInviteDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdKick.toString(), Term.TownCmdKickArgs.toString(), Term.TownCmdKickDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdSetSpawn.toString(), "", Term.TownCmdSetSpawnDesc.toString(), color));
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdPlot.toString(), Term.TownCmdPlotArgs.toString(), Term.TownCmdPlotDesc.toString(), color));
				handled = true;
			}
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdClaim.toString()))
		{
			if (res.onlinePlayer == null)
				throw new NullPointerException("Onlineplayer is null");
			int dim = res.onlinePlayer.dimension;
			
			Assert.Perm(cs, "mytown.cmd.claim.dim" + dim);
			handled = true;

			int radius_rec = 0;
			if (args.length > 1)
			{
				if (args[1].equalsIgnoreCase(Term.TownCmdClaimArgs1.toString()))
					radius_rec = Integer.parseInt(args[2]);
				else
					throw new CommandException(Term.TownErrCmdUnknownArgument, args[1]);
			}
			
			int cx = res.onlinePlayer.chunkCoordX;
			int cz = res.onlinePlayer.chunkCoordZ;
			

			CommandException firstError = null;
			int requestedBlocks = 0, ableToClaim = 0, alreadyOwn = 0;
			List<TownBlock> blocks = Lists.newArrayList();

			for(int z = cz - radius_rec; z <= cz + radius_rec; z++)
			{
				for(int x = cx - radius_rec; x <= cx + radius_rec; x++)
				{
					requestedBlocks++;
					
					TownBlock b = MyTownDatasource.instance.getOrMakeBlock(dim, x, z);
					if (b.town() == res.town())
					{
						alreadyOwn++;
						continue;
					}
					
					try
					{
						Town.canAddBlock(b, false, res.town());
						ableToClaim++;
						blocks.add(b);
					}
					catch (CommandException e)
					{
						if (b != null && b.town() == null)
							MyTownDatasource.instance.unloadBlock(b);
						
						if (firstError == null)
							firstError = e;
					}
				}
			}

			cs.sendChatToPlayer(Term.TownBlocksClaimedDisclaimer.toString(requestedBlocks, ableToClaim, alreadyOwn));
			if (firstError != null)
				cs.sendChatToPlayer(Term.TownBlocksClaimedDisclaimer2.toString(firstError.errorCode.toString(firstError.args)));

			if (blocks.size() > 0)
			{
				
				ItemStack request = Cost.TownClaimBlock.item;
				if (request != null && request.stackSize > 0)
				{
					request = request.copy();
					request.stackSize = request.stackSize * blocks.size();
				}
				
				res.pay.requestPayment("townclaimblock", request, new PayHandler.IDone() 
				{
					@Override
					public void run(Resident res, Object[] args) 
					{
						StringBuilder sb = new StringBuilder();
						int nr = 0;
						List<TownBlock> blocks = (List<TownBlock>)args[0];
						
						for (TownBlock b : blocks)
						{
							try 
							{
								res.town().addBlock(b);
								
								nr++;
								if (sb.length() > 0)
									sb.append(", ");
								
								sb.append(String.format("(%s,%s)", b.x(), b.z()));
							} 
							catch (CommandException e)
							{
								Log.severe("Block claiming failed after payment", e);
							}
						}
						
						res.checkLocation(); // emulate that the player just entered it
						res.onlinePlayer.sendChatToPlayer(Term.TownBlocksClaimed.toString(nr, sb.toString()));
					}
				}, blocks);
			}
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdUnclaim.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.unclaim");
			handled = true;

			if (res.onlinePlayer == null)
				throw new NullPointerException("Onlineplayer is null");
			
			int radius_rec = 0;
			if (args.length > 1)
			{
				if (args[1].equalsIgnoreCase(Term.TownCmdUnclaimArgs1.toString()))
					radius_rec = Integer.parseInt(args[2]);
				else
					throw new CommandException(Term.TownErrCmdUnknownArgument, args[1]);
			}
			
			int cx = res.onlinePlayer.chunkCoordX;
			int cz = res.onlinePlayer.chunkCoordZ;
			int dim = res.onlinePlayer.dimension;
			
			StringBuilder sb = new StringBuilder();
			int nr = 0;
			ArrayList<TownBlock> blocks = new ArrayList<TownBlock>();

			for(int z = cz - radius_rec; z <= cz + radius_rec; z++)
			{
				for(int x = cx - radius_rec; x <= cx + radius_rec; x++)
				{
					TownBlock b = MyTownDatasource.instance.getBlock(dim, x, z);
					if (b == null || b.town() != res.town())
						continue;
					
					blocks.add(b);
					
					if (b == res.town().spawnBlock)
						cs.sendChatToPlayer(Term.TownSpawnReset.toString());

					nr++;
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(String.format("(%s,%s)", x, z));

				}
			}

			res.town().removeBlocks(blocks);
			
			// emulate that the player just entered it
			res.checkLocation();
			cs.sendChatToPlayer(Term.TownBlocksUnclaimed.toString(nr, sb.toString()));
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdInvite.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.invite");
			handled = true;

			if (args.length < 2)
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdInvite.toString(), Term.TownCmdInviteArgs.toString(), Term.TownCmdInviteDesc.toString(), color));
			else
			{
				Resident target = MyTownDatasource.instance.getResident(args[1]);
				if (target == null || target.onlinePlayer == null)
					throw new CommandException(Term.TownErrPlayerNotFoundOrOnline);
				
				if (target == res)
					throw new CommandException(Term.TownErrInvitationSelf);
				if (target.town() == res.town())
					throw new CommandException(Term.TownErrInvitationAlreadyInYourTown);
				if (target.town() != null)
					throw new CommandException(Term.TownErrInvitationInTown);
				if (target.inviteActiveFrom != null)
					throw new CommandException(Term.TownErrInvitationActive);
				
				target.inviteActiveFrom = res.town();
				
				target.onlinePlayer.sendChatToPlayer(Term.TownInvitation.toString(res.name(), res.town().name()));
				cs.sendChatToPlayer(Term.TownInvitedPlayer.toString(target.name()));
			}
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdKick.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.kick");
			handled = true;

			if (args.length < 2)
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdKick.toString(), Term.TownCmdKickArgs.toString(), Term.TownCmdKickDesc.toString(), color));
			else
			{
				Resident target = MyTownDatasource.instance.getResident(args[1]);
				
				if (target == null) // all town residents are always loaded
					throw new CommandException(Term.TownErrPlayerNotFound);
				
				if (target == res)
					throw new CommandException(Term.TownErrCannotKickYourself);
				if (target.town() != res.town())
					throw new CommandException(Term.TownErrPlayerNotInYourTown);
				if (target.rank() == Rank.Mayor && res.rank() == Rank.Assistant)
					throw new CommandException(Term.TownErrCannotKickMayor);
				if (target.rank() == Rank.Assistant && res.rank() == Rank.Assistant)
					throw new CommandException(Term.TownErrCannotKickAssistants);
				
				res.town().removeResident(target);
				
				res.town().sendNotification(Level.INFO, Term.TownKickedPlayer.toString(res.name(), target.name()));
			}
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdPlot.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.plot");
			handled = true;

			if (args.length < 2)
				cs.sendChatToPlayer(Formatter.formatCommand(Term.TownCmdPlot.toString(), Term.TownCmdPlotArgs.toString(), Term.TownCmdPlotDesc.toString(), color));
			else
			{
				int radius_rec = 0;
				if (args.length > 3)
				{
					if (args[2].equalsIgnoreCase("rect"))
						radius_rec = Integer.parseInt(args[3]);
					else
						throw new CommandException(Term.TownErrCmdUnknownArgument, args[2]);
				}
				
				Resident target = null;
				
				if (args[1] != null && !args[1].equals("") && !args[1].equalsIgnoreCase("none") && !args[1].equalsIgnoreCase("null"))
				{
					target = MyTownDatasource.instance.getResident(args[1]);
					if (target == null) // all town residents are always loaded
						throw new CommandException(Term.TownErrPlayerNotFound);
					if (res.town() != target.town())
						throw new CommandException(Term.TownErrPlayerNotInYourTown);
				}
				
				boolean canUnAssign = false, canReAssign = false;
				int cx = res.onlinePlayer.chunkCoordX;
				int cz = res.onlinePlayer.chunkCoordZ;
				for(int z = cz - radius_rec; z <= cz + radius_rec; z++)
				{
					for(int x = cx - radius_rec; x <= cx + radius_rec; x++)
					{
						TownBlock b = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, x, z);
						if (b == null || b.town() == null)
							throw new CommandException(Term.ErrPermPlotNotInTown);
						if (b.town() != res.town())
							throw new CommandException(Term.ErrPermPlotNotInYourTown);
						
						if (b.owner() == target)
							continue;
						
						if (target == null && b.owner() != null && !canUnAssign)
						{
							Assert.Perm(cs, "mytown.cmd.plot.unassign");
							canUnAssign = true;
						}
						if (target != null && b.owner() != null && !canReAssign)
						{
							Assert.Perm(cs, "mytown.cmd.plot.reassign");
							canReAssign = true;
						}

						b.setOwner(target);
					}
				}

				if (target != null)
					cs.sendChatToPlayer(Term.TownPlotAssigned.toString(target.name()));
				else
					cs.sendChatToPlayer(Term.TownPlotUnAssigned.toString());
			}
		}
		else if (args[0].equalsIgnoreCase(Term.TownCmdSetSpawn.toString()))
		{
			Assert.Perm(cs, "mytown.cmd.setspawn");
			handled = true;

			TownBlock b = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
			
			if (b == null || b.town() == null)
				throw new CommandException(Term.ErrPermPlotNotInTown);
			if (b.town() != res.town())
				throw new CommandException(Term.ErrPermPlotNotInYourTown);
			
			Vec3 vec = Vec3.createVectorHelper(res.onlinePlayer.posX, res.onlinePlayer.posY, res.onlinePlayer.posZ);
			res.town().setSpawn(b, vec, res.onlinePlayer.rotationPitch, res.onlinePlayer.rotationYaw);
			
			cs.sendChatToPlayer(Term.TownSpawnSet.toString());
		}
		
		return handled;
	}
}
