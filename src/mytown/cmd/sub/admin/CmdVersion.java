package mytown.cmd.sub.admin;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import mytown.Constants;
import mytown.MyTown;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;

public class CmdVersion extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "version";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.version";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		MyTown.sendChatToPlayer(sender, String.format(Term.TownadmCmdVersionFormat.toString(), Constants.VERSION));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

}
