package mytown.cmd.sub.admin;

import java.util.List;

import mytown.Constants;
import mytown.MyTown;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

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
	public boolean canUseByConsole() {
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		MyTown.sendChatToPlayer(sender, String.format(Term.TownadmCmdVersionFormat.toString(), Constants.VERSION));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownadmCmdVersionDesc.toString();
	}
}
