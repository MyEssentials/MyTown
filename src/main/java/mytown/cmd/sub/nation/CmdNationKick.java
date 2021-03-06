package mytown.cmd.sub.nation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Nation;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdNationKick extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "kick";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.nationkick";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null) {
			throw new CommandException(Term.ErrNotInTown.toString());
		}
		if (res.rank() != Rank.Mayor) {
			throw new CommandException(Term.ErrNotMayor.toString());
		}
		if (res.town().nation() != null) {
			throw new CommandException(Term.TownErrAlreadyInNation.toString());
		}
		if (res.town().nation().capital() != res.town()) {
			throw new CommandException(Term.TownErrNationNotCapital.toString(res.town().nation().name()));
		}
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Town town = res.town();
		Nation nation = town.nation();

		if (args.length == 1) {
			Town t = MyTownDatasource.instance.getTown(args[0]);
			if (t == null) {
				throw new CommandException(Term.TownErrNotFound.toString(), args[0]);
			}

			if (t.nation() != nation) {
				throw new CommandException(Term.TownErrNationNotPartOfNation.toString());
			}

			if (t == town) {
				throw new CommandException(Term.TownErrNationCannotKickSelf.toString());
			}

			nation.removeTown(t);
			t.sendNotification(Level.INFO, Term.NationLeft.toString(nation.name()));

			MyTown.sendChatToPlayer(sender, Term.TownKickedFromNation.toString(t.name()));
		} else {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationTransfer.toString(), Term.TownCmdNationTransferArgs.toString(), Term.TownCmdNationTransferDesc.toString(), null));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		List<String> towns = new ArrayList<String>();
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Town town = res.town();
		Nation nation = town.nation();
		towns.addAll(nation.towns().keySet());
		return towns;
	}

	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdNationKickDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdNationKickArgs.toString();
	}
}
