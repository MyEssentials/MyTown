package mytown.cmd.sub.nonresident;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import mytown.CommandException;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;

public class CmdTownDeny extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "deny";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.deny";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
        Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
        if (res.inviteActiveFrom == null) {
            throw new CommandException(Term.TownErrYouDontHavePendingInvitations);
        }

        res.inviteActiveFrom = null;

        MyTown.sendChatToPlayer(res.onlinePlayer, Term.TownPlayerDeniedInvitation.toString());
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}