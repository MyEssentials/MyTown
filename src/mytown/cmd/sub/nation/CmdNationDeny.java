package mytown.cmd.sub.nation;

import java.util.List;

import mytown.CommandException;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdNationDeny extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "deny";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.nationdeny";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException, NoAccessException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null) {
			throw new CommandException(Term.ErrNotInTown);
		}
		if (res.rank() != Rank.Mayor) {
			throw new CommandException(Term.ErrNotMayor);
		}
		if (res.town().pendingNationInvitation == null) {
			throw new CommandException(Term.TownErrNationYouDontHavePendingInvitations);
		}
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Town town = res.town();
		if (town.pendingNationInvitation == null) {
			throw new CommandException(Term.TownErrNationYouDontHavePendingInvitations);
		}

		town.pendingNationInvitation = null;
		MyTown.sendChatToPlayer(sender, Term.NationPlayerDeniedInvitation.toString());
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
