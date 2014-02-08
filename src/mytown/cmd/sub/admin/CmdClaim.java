package mytown.cmd.sub.admin;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;

public class CmdClaim extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "claim";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.claim";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		if (args.length < 1) { // /ta claim townname [playername] [x.y:x.y]
			MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdClaim.toString(), Term.TownadmCmdClaimArgs.toString(), Term.TownadmCmdClaimDesc.toString(), null));
			return;
		}

		Town t = null;

		if (args[0].equals("none") || args[0].equals("null")) {
			t = null;
		} else {
			t = MyTownDatasource.instance.getTown(args[0]);
			if (t == null) {
				throw new CommandException(Term.TownErrNotFound.toString(), args[0]);
			}
		}

		Resident target_res = null;
		if (args.length > 1) {
			if (args[1].equals("none") || args[1].equals("null")) {
				target_res = null;
			} else {
				target_res = MyTownDatasource.instance.getResident(args[1]);
				if (target_res == null) {
					throw new CommandException(Term.TownErrPlayerNotFound.toString());
				}
			}
		}

		int ax, az, bx, bz, dim;
		if (args.length > 2) {
			String[] sp = args[2].split(":");
			String[] sp2 = sp[0].split("\\.");

			ax = bx = Integer.parseInt(sp2[0]);
			az = bz = Integer.parseInt(sp2[1]);

			if (sp.length > 1) {
				sp2 = sp[1].split("\\.");

				bx = Integer.parseInt(sp2[0]);
				bz = Integer.parseInt(sp2[1]);
			}
			if (sp.length > 2) {
				dim = Integer.parseInt(sp[2]);
			} else {
				Resident res = sender instanceof EntityPlayer ? MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender) : null;
				if (res == null) {
					throw new CommandException(Term.ErrNotUsableByConsole.toString()); // console needs up to this
				}

				dim = res.onlinePlayer.dimension;
			}
		} else {
			Resident res = sender instanceof EntityPlayer ? MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender) : null;
			if (res == null) {
				throw new CommandException(Term.ErrNotUsableByConsole.toString());
			}

			ax = bx = res.onlinePlayer.chunkCoordX;
			az = bz = res.onlinePlayer.chunkCoordZ;
			dim = res.onlinePlayer.dimension;
		}

		StringBuilder sb = new StringBuilder();
		int nr = 0;

		for (int z = az; z <= bz; z++) {
			for (int x = ax; x <= bx; x++) {
				TownBlock b = MyTownDatasource.instance.getOrMakeBlock(dim, x, z);
				if (b.town() == t && b.owner() == target_res) {
					continue;
				}

				if (b.town() != null && b.town() != t) {
					b.town().removeBlock(b);
				}

				if (t != null) {
					b.sqlSetOwner(target_res);
					t.addBlock(b, true);
				}

				nr++;
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(String.format("(%s,%s)", x, z));
			}
		}

		MyTown.sendChatToPlayer(sender, Term.TownBlocksClaimed.toString(nr, sb.toString()));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

}
