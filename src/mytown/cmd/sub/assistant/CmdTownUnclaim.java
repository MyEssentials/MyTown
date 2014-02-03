package mytown.cmd.sub.assistant;

import java.util.ArrayList;
import java.util.List;

import mytown.CommandException;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdTownUnclaim extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "unclaim";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.unclaim";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.onlinePlayer == null) {
			throw new NullPointerException("Onlineplayer is null");
		}

		int radius_rec = 0;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase(Term.TownCmdUnclaimArgs1.toString())) {
				radius_rec = Integer.parseInt(args[21]);
			} else {
				throw new CommandException(Term.TownErrCmdUnknownArgument, args[0]);
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
					MyTown.sendChatToPlayer(sender, Term.TownSpawnReset.toString());
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
		MyTown.sendChatToPlayer(sender, Term.TownBlocksUnclaimed.toString(nr, sb.toString()));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}