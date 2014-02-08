package mytown.cmd.sub.admin;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;

public class CmdExtraRes extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "extrares";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.extrares";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		if (args.length != 3) {
			MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdExtraRes.toString(), Term.TownadmCmdExtraResArgs.toString(), Term.TownadmCmdExtraResDesc.toString(), null));
		} else {
			Resident t = MyTownDatasource.instance.getResident(args[0]);
			String cmd = args[1];
			int cnt = Integer.parseInt(args[2]);

			if (t == null) {
				throw new CommandException(Term.TownErrPlayerNotFound.toString(), args[0]);
			}

			if (cmd.equalsIgnoreCase("add")) {
				cnt = t.extraBlocks + cnt;
			} else if (cmd.equalsIgnoreCase("sub")) {
				cnt = t.extraBlocks - cnt;
			}

			t.setExtraBlocks(cnt);
			MyTown.sendChatToPlayer(sender, Term.TownadmResExtraSet.toString());
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

}
