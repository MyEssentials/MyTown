package mytown.cmd.sub.assistant;

import mytown.*;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.TownBlock;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class CmdTownPlot extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "plot";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.plot";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null)
			throw new CommandException(Term.ChatErrNotInTown.toString());
		if (res.rank().compareTo(Rank.Assistant) <= 0)
			throw new CommandException(Term.ErrPermRankNotEnough.toString());
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (args.length < 1) {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdPlot.toString(), Term.TownCmdPlotArgs.toString(), Term.TownCmdPlotDesc.toString(), null));
		} else {
			int radius_rec = 0;
			if (args.length > 2) {
				if (args[1].equalsIgnoreCase("rect")) {
					radius_rec = Integer.parseInt(args[2]);
				} else {
					throw new CommandException(String.format(Term.TownErrCmdUnknownArgument.toString(), args[2]));
				}
			}

			Resident target = null;

			if (args[0] != null && !args[0].equals("") && !args[0].equalsIgnoreCase("none") && !args[0].equalsIgnoreCase("null")) {
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
						Assert.Perm(sender, "mytown.cmd.plot.unassign");
						canUnAssign = true;
					}
					if (target != null && b.owner() != null && !canReAssign) {
						Assert.Perm(sender, "mytown.cmd.plot.reassign");
						canReAssign = true;
					}

					b.setOwner(target);
				}
			}

			if (target != null) {
				MyTown.sendChatToPlayer(sender, Term.TownPlotAssigned.toString(target.name()));
			} else {
				MyTown.sendChatToPlayer(sender, Term.TownPlotUnAssigned.toString());
			}
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdPlotDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdPlotArgs.toString();
	}
}