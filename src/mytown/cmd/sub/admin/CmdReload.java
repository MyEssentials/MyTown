package mytown.cmd.sub.admin;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import mytown.MyTown;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;

public class CmdReload extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.reload";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		MyTown.instance.reload();
		MyTown.sendChatToPlayer(sender, Term.TownadmModReloaded.toString());
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

}
