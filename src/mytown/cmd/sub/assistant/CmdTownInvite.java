package mytown.cmd.sub.assistant;

import java.util.List;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdTownInvite extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "invite";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.invite";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null)
			throw new CommandException(Term.ChatErrNotInTown.toString());
		if (res.rank().compareTo(Rank.Assistant) < 0)
			throw new CommandException(Term.ErrPermRankNotEnough.toString());
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (args.length != 1) {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdInvite.toString(), Term.TownCmdInviteArgs.toString(), Term.TownCmdInviteDesc.toString(), null));
		} else {
			Resident target = MyTownDatasource.instance.getResident(args[0]);
			if (target == null || target.onlinePlayer == null) {
				throw new CommandException(Term.TownErrPlayerNotFoundOrOnline.toString());
			}

			if (target == res) {
				throw new CommandException(Term.TownErrInvitationSelf.toString());
			}
			if (target.town() == res.town()) {
				throw new CommandException(Term.TownErrInvitationAlreadyInYourTown.toString());
			}
			if (target.town() != null) {
				throw new CommandException(Term.TownErrInvitationInTown.toString());
			}
			if (target.inviteActiveFrom != null) {
				throw new CommandException(Term.TownErrInvitationActive.toString());
			}

			target.inviteActiveFrom = res.town();

			MyTown.sendChatToPlayer(target.onlinePlayer, Term.TownInvitation.toString(res.name(), res.town().name()));
			MyTown.sendChatToPlayer(sender, Term.TownInvitedPlayer.toString(target.name()));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdInviteDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdInviteArgs.toString();
	}
}