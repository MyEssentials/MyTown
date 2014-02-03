package mytown.cmd.sub.nation;

import java.util.List;
import java.util.logging.Level;

import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Nation;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdNationAccept extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "accept";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.nationaccept";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException, NoAccessException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null) {
			throw new CommandException(Term.ErrNotInTown.toString());
		}
		if (res.rank() != Rank.Mayor) {
			throw new CommandException(Term.ErrNotMayor.toString());
		}
		if (res.town().pendingNationInvitation == null) {
			throw new CommandException(Term.TownErrNationYouDontHavePendingInvitations.toString());
		}
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Town town = res.town();
		Nation n = town.pendingNationInvitation;
		n.addTown(town);

		n.sendNotification(Level.INFO, Term.NationTownJoinedNation.toString(town.name()));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
