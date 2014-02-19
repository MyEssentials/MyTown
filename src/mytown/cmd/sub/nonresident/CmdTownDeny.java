package mytown.cmd.sub.nonresident;

import java.util.List;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

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
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() != null)
			throw new CommandException("Already in a town");
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.inviteActiveFrom == null) {
			throw new CommandException(Term.TownErrYouDontHavePendingInvitations.toString());
		}

		res.inviteActiveFrom = null;

		MyTown.sendChatToPlayer(res.onlinePlayer, Term.TownPlayerDeniedInvitation.toString());
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdDenyDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdDenyDesc.toString();
	}
}