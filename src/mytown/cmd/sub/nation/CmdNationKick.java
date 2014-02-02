package mytown.cmd.sub.nation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import mytown.CommandException;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Nation;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
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
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null || res.rank() != Rank.Mayor) {
			return;
		}

		Town town = res.town();
		Nation nation = town.nation();

		if (args.length == 3) {
			Town t = MyTownDatasource.instance.getTown(args[2]);
			if (t == null) {
				throw new CommandException(Term.TownErrNotFound, args[2]);
			}

			if (t.nation() != nation) {
				throw new CommandException(Term.TownErrNationNotPartOfNation);
			}

			if (t == town) {
				throw new CommandException(Term.TownErrNationCannotKickSelf);
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

}
