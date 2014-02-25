package mytown.cmd.sub.everyone;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class CmdFriend extends MyTownSubCommandAdapter {
	ArrayList<String> tabComplete;

	public CmdFriend() {
		tabComplete = new ArrayList<String>();
		tabComplete.add(Term.TownCmdFriendArgsAdd.toString());
		tabComplete.add(Term.TownCmdFriendArgsRemove.toString());
	}

	@Override
	public String getName() {
		return "friend";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.friend";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (args.length == 2) {
			String cmd = args[0];
			Resident target = MyTownDatasource.instance.getResident(args[1]);
			if (target == null) {
				throw new CommandException(Term.TownErrPlayerNotFound.toString());
			}

			if (cmd.equalsIgnoreCase(Term.TownCmdFriendArgsAdd.toString())) {
				if (!res.addFriend(target)) {
					throw new CommandException(Term.ErrPlayerAlreadyInFriendList.toString(), target.name());
				}
			} else if (cmd.equalsIgnoreCase(Term.TownCmdFriendArgsRemove.toString())) {
				if (!res.removeFriend(target)) {
					throw new CommandException(Term.ErrPlayerNotInFriendList.toString(), target.name());
				}
			}
			res.sendInfoTo(sender, res.shouldShowPlayerLocation());
		} else {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdFriend.toString(), Term.TownCmdFriendArgs.toString(), Term.TownCmdFriendDesc.toString(), null));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		if (args.length < 1) {
			return tabComplete;
		}
		String cmd = args[0];
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		ArrayList<String> list = new ArrayList<String>();

		if (cmd.equalsIgnoreCase(Term.TownCmdFriendArgsAdd.toString())) {
			for (Resident r : MyTownDatasource.instance.residents.values()) {
				if (res.friends.contains(r)) {
					continue;
				}

				list.add(r.name());
			}
		} else if (cmd.equalsIgnoreCase(Term.TownCmdFriendArgsRemove.toString())) {
			for (Resident r : res.friends) {
				list.add(r.name());
			}
		}
		return list;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdFriendDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdFriendArgs.toString();
	}
}