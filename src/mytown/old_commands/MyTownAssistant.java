package mytown.old_commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mytown.Assert;
import mytown.Cost;
import mytown.Formatter;
import mytown.Log;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.entities.PayHandler;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sperion.forgeperms.ForgePerms;

public class MyTownAssistant {
	public static List<String> getAutoComplete(ICommandSender cs, String[] args) {
		ArrayList<String> list = new ArrayList<String>();

		if (!(cs instanceof EntityPlayer)) {
			return list;
		}

		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) cs);
		if (res.town() == null || res.rank() != Rank.Mayor && res.rank() != Rank.Assistant) {
			return list;
		}

		if (args.length == 1) {
			list.add(Term.TownCmdClaim.toString());
			list.add(Term.TownCmdUnclaim.toString());
			list.add(Term.TownCmdInvite.toString());
			list.add(Term.TownCmdKick.toString());
			list.add(Term.TownCmdSetSpawn.toString());
			list.add(Term.TownCmdPlot.toString());
		} else if (args.length == 2 && (args[0].equals("?") || args[0].equalsIgnoreCase(Term.CommandHelp.toString()))) {
			list.add(Term.CommandHelpAssistant.toString());
		} else if (args.length == 2 && (args[0].equalsIgnoreCase(Term.TownCmdClaim.toString()) || args[0].equalsIgnoreCase(Term.TownCmdUnclaim.toString()))) {
			list.add(Term.TownCmdClaimArgs1.toString());
		} else if (args.length == 2 && args[0].equalsIgnoreCase(Term.TownCmdInvite.toString())) {
			for (Resident r : MyTownDatasource.instance.residents.values()) {
				if (r.town() == null) {
					list.add(r.name());
				}
			}
		} else if (args.length == 2 && args[0].equalsIgnoreCase(Term.TownCmdKick.toString())) {
			for (Resident r : res.town().residents()) {
				if (r != res && r.rank() != Rank.Mayor && (res.rank() == Rank.Mayor || r.rank() != Rank.Assistant)) {
					list.add(r.name());
				}
			}
		} else if (args.length == 2 && args[0].equalsIgnoreCase(Term.TownCmdPlot.toString())) {
			for (Resident r : res.town().residents()) {
				list.add(r.name());
			}
		} else if (args.length == 3 && args[0].equalsIgnoreCase(Term.TownCmdPlot.toString())) {
			list.add("rect");
		}

		return list;
	}

	public static boolean handleCommand(ICommandSender cs, String[] args) throws CommandException, NoAccessException {
		if (args.length < 1) {
			return false;
		}

		if (!(cs instanceof EntityPlayer)) {
			return false;
		}

		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) cs);
		if (res.town() == null || res.rank() != Rank.Mayor && res.rank() != Rank.Assistant) {
			return false;
		}

		boolean handled = false;
		String color = "6";
		if (args[0].equals("?") || args[0].equalsIgnoreCase(Term.CommandHelp.toString())) {
			if (args.length < 2) {
				MyTown.sendChatToPlayer(cs, Formatter.formatGroupCommand(Term.CommandHelp.toString(), Term.CommandHelpAssistant.toString(), Term.CommandHelpAssistantDesc.toString(), color));
				// cs.sendChatToPlayer(Formatter.formatGroupCommand(Term.CommandHelp.toString(),
				// Term.CommandHelpAssistant.toString(),
				// Term.CommandHelpAssistantDesc.toString(), color));
				handled = true;
			} else if (args[1].equalsIgnoreCase(Term.CommandHelpAssistant.toString())) {
				MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdClaim.toString(), Term.TownCmdClaimArgs.toString(), Term.TownCmdClaimDesc.toString(), color));
				MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdUnclaim.toString(), Term.TownCmdUnclaimArgs.toString(), Term.TownCmdUnclaimDesc.toString(), color));
				MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdInvite.toString(), Term.TownCmdInviteArgs.toString(), Term.TownCmdInviteDesc.toString(), color));
				MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdKick.toString(), Term.TownCmdKickArgs.toString(), Term.TownCmdKickDesc.toString(), color));
				MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdSetSpawn.toString(), "", Term.TownCmdSetSpawnDesc.toString(), color));
				MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdPlot.toString(), Term.TownCmdPlotArgs.toString(), Term.TownCmdPlotDesc.toString(), color));
				handled = true;
			}
		} else if (args[0].equalsIgnoreCase(Term.TownCmdClaim.toString())) {
			if (res.onlinePlayer == null) {
				throw new NullPointerException("Onlineplayer is null");
			}
			int dim = res.onlinePlayer.worldObj.provider.dimensionId;

			Assert.Perm(cs, "mytown.cmd.claim.dim" + dim);
			handled = true;

			int radius_rec = 0;
			if (args.length > 1) {
				if (args[1].equalsIgnoreCase(Term.TownCmdClaimArgs1.toString())) {
					radius_rec = Integer.parseInt(args[2]);
				} else {
					throw new CommandException(Term.TownErrCmdUnknownArgument.toString(), args[1]);
				}
			}

			int cx = res.onlinePlayer.chunkCoordX;
			int cz = res.onlinePlayer.chunkCoordZ;

			CommandException firstError = null;
			int requestedBlocks = 0, ableToClaim = 0, alreadyOwn = 0;
			boolean bypassFarawayRestriction = ForgePerms.getPermissionManager().canAccess(res.name(), res.onlinePlayer.worldObj.provider.getDimensionName(), "mytown.adm.bypass.faraway");
			List<TownBlock> blocks = Lists.newArrayList();

			for (int z = cz - radius_rec; z <= cz + radius_rec; z++) {
				for (int x = cx - radius_rec; x <= cx + radius_rec; x++) {
					requestedBlocks++;

					TownBlock b = MyTownDatasource.instance.getOrMakeBlock(dim, x, z);
					if (b.town() == res.town()) {
						alreadyOwn++;
						continue;
					}

					try {
						Town.canAddBlock(b, false, res.town());
						ableToClaim++;
						blocks.add(b);
					} catch (CommandException e) {
						if (b != null && b.town() == null) {
							MyTownDatasource.instance.unloadBlock(b);
						}

						if (firstError == null) {
							firstError = e;
						}
					}
				}
			}

			boolean noChunksInDim = true;
			for (TownBlock block : res.town().blocks()) {
				if (block.worldDimension() == dim) {
					noChunksInDim = false;
					break;
				}
			}

			// throw new CommandException(Term.TownErrNotAdjacent);

			// Does new claim have to be adjacent to existing claims?
			if (!bypassFarawayRestriction && !Town.allowFarawayClaims && !noChunksInDim) {
				Map<String, TownBlock> adjacentBlocksMap = Maps.newHashMap();
				List<TownBlock> adjacentBlocks = Lists.newArrayList();

				// First check, grab all chunks adjacent to existing town chunks
				for (TownBlock block : blocks) {
					if (Town.isBlockAdjacentToTown(block, res.town())) {
						adjacentBlocksMap.put(MyTownDatasource.getTownBlockKey(block), block);
						adjacentBlocks.add(block);
					}
				}
				blocks.removeAll(adjacentBlocks);

				// Not all blocks are immediatly adjacent, need to iterate
				if (blocks.size() > 0) {
					boolean addedBlocks = false;
					do {
						addedBlocks = false;
						for (TownBlock block : blocks) {
							if (Town.isBlockAdjacentToBlocks(block, adjacentBlocksMap)) {
								adjacentBlocksMap.put(MyTownDatasource.getTownBlockKey(block), block);
								ableToClaim--;
								adjacentBlocks.add(block);
								addedBlocks = true;
							}
						}
						blocks.removeAll(adjacentBlocks);
					} while (addedBlocks);
				}

				// Blocks that couldn't be added due to adjacent requirements
				if (!blocks.isEmpty()) {
					for (TownBlock b : blocks) {
						if (b.town() == null) {
							MyTownDatasource.instance.unloadBlock(b);
						}
					}
				}
				blocks = adjacentBlocks;
				ableToClaim = blocks.size();
			}

			MyTown.sendChatToPlayer(cs, Term.TownBlocksClaimedDisclaimer.toString(requestedBlocks, ableToClaim, alreadyOwn));
			if (firstError != null) {
//				MyTown.sendChatToPlayer(cs, Term.TownBlocksClaimedDisclaimer2.toString(firstError.errorCode.toString(firstError.args)));
				throw firstError;
			}

			if (blocks.size() > 0) {

				ItemStack request = Cost.TownClaimBlock.item;
				if (request != null && request.stackSize > 0) {
					request = request.copy();
					request.stackSize = request.stackSize * blocks.size();
				}

				res.pay.requestPayment("townclaimblock", request, new PayHandler.IDone() {
					@SuppressWarnings("unchecked")
					@Override
					public void run(Resident res, Object[] args) {
						StringBuilder sb = new StringBuilder();
						int nr = 0;
						List<TownBlock> blocks = (List<TownBlock>) args[0];

						for (TownBlock b : blocks) {
							try {
								res.town().addBlock(b);

								nr++;
								if (sb.length() > 0) {
									sb.append(", ");
								}

								sb.append(String.format("(%s,%s)", b.x(), b.z()));
							} catch (CommandException e) {
								Log.severe("Block claiming failed after payment", e);
							}
						}

						res.checkLocation(); // emulate that the player just
												// entered it
						MyTown.sendChatToPlayer(res.onlinePlayer, Term.TownBlocksClaimed.toString(nr, sb.toString()));
					}
				}, blocks);
			}
		} else if (args[0].equalsIgnoreCase(Term.TownCmdUnclaim.toString())) {
			Assert.Perm(cs, "mytown.cmd.unclaim");
			handled = true;

			if (res.onlinePlayer == null) {
				throw new NullPointerException("Onlineplayer is null");
			}

			int radius_rec = 0;
			if (args.length > 1) {
				if (args[1].equalsIgnoreCase(Term.TownCmdUnclaimArgs1.toString())) {
					radius_rec = Integer.parseInt(args[2]);
				} else {
					throw new CommandException(Term.TownErrCmdUnknownArgument.toString(), args[1]);
				}
			}

			int cx = res.onlinePlayer.chunkCoordX;
			int cz = res.onlinePlayer.chunkCoordZ;
			int dim = res.onlinePlayer.dimension;

			StringBuilder sb = new StringBuilder();
			int nr = 0;
			ArrayList<TownBlock> blocks = new ArrayList<TownBlock>();

			for (int z = cz - radius_rec; z <= cz + radius_rec; z++) {
				for (int x = cx - radius_rec; x <= cx + radius_rec; x++) {
					TownBlock b = MyTownDatasource.instance.getBlock(dim, x, z);
					if (b == null || b.town() != res.town()) {
						continue;
					}

					blocks.add(b);

					if (b == res.town().spawnBlock) {
						MyTown.sendChatToPlayer(cs, Term.TownSpawnReset.toString());
					}

					nr++;
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(String.format("(%s,%s)", x, z));

				}
			}

			res.town().removeBlocks(blocks);

			// emulate that the player just entered it
			res.checkLocation();
			MyTown.sendChatToPlayer(cs, Term.TownBlocksUnclaimed.toString(nr, sb.toString()));
		} else if (args[0].equalsIgnoreCase(Term.TownCmdInvite.toString())) {
			Assert.Perm(cs, "mytown.cmd.invite");
			handled = true;

			if (args.length < 2) {
				MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdInvite.toString(), Term.TownCmdInviteArgs.toString(), Term.TownCmdInviteDesc.toString(), color));
			} else {
				Resident target = MyTownDatasource.instance.getResident(args[1]);
				if (target == null || target.onlinePlayer == null) {
					throw new CommandException(Term.TownErrPlayerNotFoundOrOnline.toString());
				}

				if (target == res) {
					throw new CommandException(Term.TownErrInvitationSelf.toString());
				}
				if (target.town() == res.town()) {
					throw new CommandException(Term.TownErrInvitationAlreadyInYourTown.toString());
				}
				if (target.town() != null) {
					throw new CommandException(Term.TownErrInvitationInTown.toString());
				}
				if (target.inviteActiveFrom != null) {
					throw new CommandException(Term.TownErrInvitationActive.toString());
				}

				target.inviteActiveFrom = res.town();

				MyTown.sendChatToPlayer(target.onlinePlayer, Term.TownInvitation.toString(res.name(), res.town().name()));
				MyTown.sendChatToPlayer(cs, Term.TownInvitedPlayer.toString(target.name()));
			}
		} else if (args[0].equalsIgnoreCase(Term.TownCmdKick.toString())) {
			Assert.Perm(cs, "mytown.cmd.kick");
			handled = true;

			if (args.length < 2) {
				MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdKick.toString(), Term.TownCmdKickArgs.toString(), Term.TownCmdKickDesc.toString(), color));
			} else {
				Resident target = MyTownDatasource.instance.getResident(args[1]);

				if (target == null) {
					throw new CommandException(Term.TownErrPlayerNotFound.toString());
				}

				if (target == res) {
					throw new CommandException(Term.TownErrCannotKickYourself.toString());
				}
				if (target.town() != res.town()) {
					throw new CommandException(Term.TownErrPlayerNotInYourTown.toString());
				}
				if (target.rank() == Rank.Mayor && res.rank() == Rank.Assistant) {
					throw new CommandException(Term.TownErrCannotKickMayor.toString());
				}
				if (target.rank() == Rank.Assistant && res.rank() == Rank.Assistant) {
					throw new CommandException(Term.TownErrCannotKickAssistants.toString());
				}

				res.town().removeResident(target);

				res.town().sendNotification(Level.INFO, Term.TownKickedPlayer.toString(res.name(), target.name()));
			}
		} else if (args[0].equalsIgnoreCase(Term.TownCmdPlot.toString())) {
			Assert.Perm(cs, "mytown.cmd.plot");
			handled = true;

			if (args.length < 2) {
				MyTown.sendChatToPlayer(cs, Formatter.formatCommand(Term.TownCmdPlot.toString(), Term.TownCmdPlotArgs.toString(), Term.TownCmdPlotDesc.toString(), color));
			} else {
				int radius_rec = 0;
				if (args.length > 3) {
					if (args[2].equalsIgnoreCase("rect")) {
						radius_rec = Integer.parseInt(args[3]);
					} else {
						throw new CommandException(Term.TownErrCmdUnknownArgument.toString(), args[2]);
					}
				}

				Resident target = null;

				if (args[1] != null && !args[1].equals("") && !args[1].equalsIgnoreCase("none") && !args[1].equalsIgnoreCase("null")) {
					target = MyTownDatasource.instance.getResident(args[1]);
					if (target == null) {
						throw new CommandException(Term.TownErrPlayerNotFound.toString());
					}
					if (res.town() != target.town()) {
						throw new CommandException(Term.TownErrPlayerNotInYourTown.toString());
					}
				}

				boolean canUnAssign = false, canReAssign = false;
				int cx = res.onlinePlayer.chunkCoordX;
				int cz = res.onlinePlayer.chunkCoordZ;
				for (int z = cz - radius_rec; z <= cz + radius_rec; z++) {
					for (int x = cx - radius_rec; x <= cx + radius_rec; x++) {
						TownBlock b = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, x, z);
						if (b == null || b.town() == null) {
							throw new CommandException(Term.ErrPermPlotNotInTown.toString());
						}
						if (b.town() != res.town()) {
							throw new CommandException(Term.ErrPermPlotNotInYourTown.toString());
						}

						if (b.owner() == target) {
							continue;
						}

						if (target == null && b.owner() != null && !canUnAssign) {
							Assert.Perm(cs, "mytown.cmd.plot.unassign");
							canUnAssign = true;
						}
						if (target != null && b.owner() != null && !canReAssign) {
							Assert.Perm(cs, "mytown.cmd.plot.reassign");
							canReAssign = true;
						}

						b.setOwner(target);
					}
				}

				if (target != null) {
					MyTown.sendChatToPlayer(cs, Term.TownPlotAssigned.toString(target.name()));
				} else {
					MyTown.sendChatToPlayer(cs, Term.TownPlotUnAssigned.toString());
				}
			}
		} else if (args[0].equalsIgnoreCase(Term.TownCmdSetSpawn.toString())) {
			Assert.Perm(cs, "mytown.cmd.setspawn");
			handled = true;

			TownBlock b = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);

			if (b == null || b.town() == null) {
				throw new CommandException(Term.ErrPermPlotNotInTown.toString());
			}
			if (b.town() != res.town()) {
				throw new CommandException(Term.ErrPermPlotNotInYourTown.toString());
			}

			Vec3 vec = Vec3.createVectorHelper(res.onlinePlayer.posX, res.onlinePlayer.posY, res.onlinePlayer.posZ);
			res.town().setSpawn(b, vec, res.onlinePlayer.rotationPitch, res.onlinePlayer.rotationYaw);

			MyTown.sendChatToPlayer(cs, Term.TownSpawnSet.toString());
		}

		return handled;
	}
}
