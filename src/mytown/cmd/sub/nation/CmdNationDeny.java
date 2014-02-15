package mytown.cmd.sub.nation;

import java.util.List;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import net.minecraft.command.CommandException;
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
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null) {
			throw new CommandException(Term.ErrNotInTown.toString());
		}
		if (res.rank() != Rank.Mayor) {
			throw new CommandException(Term.ErrNotMayor.toString());
		}
		if (res.town().nation() != null){
			throw new CommandException(Term.TownErrAlreadyInNation.toString());
		}
		if (res.town().pendingNationInvitation == null) {
			throw new CommandException(Term.TownErrNationYouDontHavePendingInvitations.toString());
		}
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		res.town().pendingNationInvitation = null;
		MyTown.sendChatToPlayer(sender, Term.NationPlayerDeniedInvitation.toString());
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
