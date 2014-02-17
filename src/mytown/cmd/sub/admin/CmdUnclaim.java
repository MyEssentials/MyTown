package mytown.cmd.sub.admin;

import java.util.List;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdUnclaim extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "unclaim";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.unclaim";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		int radius_rec = 0;
		if (args.length > 0) {
			radius_rec = Integer.parseInt(args[0]);
		}

		Resident res = sender instanceof EntityPlayer ? MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender) : null;
		if (res == null) {
			throw new CommandException(Term.ErrNotUsableByConsole.toString());
		}

		int cx = res.onlinePlayer.chunkCoordX;
		int cz = res.onlinePlayer.chunkCoordZ;
		int dim = res.onlinePlayer.dimension;

		StringBuilder sb = new StringBuilder();
		int nr = 0;

		for (int z = cz - radius_rec; z <= cz + radius_rec; z++) {
			for (int x = cx - radius_rec; x <= cx + radius_rec; x++) {
				TownBlock b = MyTownDatasource.instance.getBlock(dim, x, z);
				if (b == null || b.town() != res.town()) {
					continue;
				}

				b.town().removeBlock(b);

				nr++;
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(String.format("(%s,%s)", x, z));

			}
		}

		// emulate that the player just entered it
		res.checkLocation();
		MyTown.sendChatToPlayer(sender, Term.TownBlocksUnclaimed.toString(nr, sb.toString()));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
