package mytown.cmd.sub.admin;

import java.util.List;

import mytown.MyTown;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

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
	public boolean canUseByConsole() {
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		MyTown.instance.reload();
		MyTown.sendChatToPlayer(sender, Term.TownadmModReloaded.toString());
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownadmCmdReloadDesc.toString();
	}
}
