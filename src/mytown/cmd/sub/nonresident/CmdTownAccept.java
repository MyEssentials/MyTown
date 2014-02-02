package mytown.cmd.sub.nonresident;

import java.util.List;
import java.util.logging.Level;

import mytown.CommandException;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdTownAccept extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "accept";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.accept";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.inviteActiveFrom == null) {
			throw new CommandException(Term.TownErrYouDontHavePendingInvitations);
		}

		res.setRank(Rank.Resident);
		res.inviteActiveFrom.addResident(res);

		res.inviteActiveFrom.sendNotification(Level.INFO, Term.TownPlayerJoinedTown.toString(res.name()));
		res.inviteActiveFrom = null;
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}