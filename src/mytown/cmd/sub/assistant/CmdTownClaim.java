package mytown.cmd.sub.assistant;

import java.util.List;
import java.util.Map;

import mytown.Assert;
import mytown.Cost;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.PayHandler;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import forgeperms.ForgePerms;

public class CmdTownClaim extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "claim";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.claim";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null)
			throw new CommandException(Term.ChatErrNotInTown.toString());
		if (res.rank().compareTo(Rank.Assistant) < 0)
			throw new CommandException(Term.ErrPermRankNotEnough.toString());
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		int dim = res.onlinePlayer.worldObj.provider.dimensionId;
		Assert.Perm(sender, "mytown.cmd.claim.dim" + dim);

		int radius_rec = 0;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase(Term.TownCmdClaimArgs1.toString())) {
				Assert.Perm(sender, "mytown.cmd.claim.rect");
				radius_rec = Integer.parseInt(args[1]);
			} else {
				throw new CommandException(Term.TownErrCmdUnknownArgument.toString(), args[0]);
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

		MyTown.sendChatToPlayer(sender, Term.TownBlocksClaimedDisclaimer.toString(requestedBlocks, ableToClaim, alreadyOwn));
		if (firstError != null) {
			// MyTown.sendChatToPlayer(sender,
			// Term.TownBlocksClaimedDisclaimer2.toString(firstError.errorCode.toString(firstError.args)));
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
							MyTown.instance.coreLog.severe("Block claiming failed after payment", e);
						}
					}

					res.checkLocation(); // emulate that the player just entered
											// it
					MyTown.sendChatToPlayer(res.onlinePlayer, Term.TownBlocksClaimed.toString(nr, sb.toString()));
				}
			}, blocks);
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdClaimDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdClaimArgs.toString();
	}
}