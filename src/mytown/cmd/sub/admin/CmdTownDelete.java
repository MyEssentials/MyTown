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
import mytown.entities.Town;

public class CmdTownDelete extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "delete";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.delete";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		if (args.length != 1) {
			MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdDelete.toString(), Term.TownadmCmdDeleteArgs.toString(), Term.TownadmCmdDeleteDesc.toString(), null));
		} else {
			Town t = MyTownDatasource.instance.getTown(args[0]);

			if (t == null) {
				throw new CommandException(Term.TownErrNotFound.toString(), args[0]);
			}

			t.deleteTown();
			MyTown.sendChatToPlayer(sender, Term.TownadmDeletedTown.toString(t.name()));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

}
