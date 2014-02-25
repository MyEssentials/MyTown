package mytown.cmd.sub.admin;

import java.util.List;

import mytown.MyTown;
import mytown.Term;
import mytown.cmd.CmdPrivateMsg;
import mytown.cmd.api.MyTownSubCommandAdapter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CmdSnoopPM extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "snoop";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.snoop";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		CmdPrivateMsg.snoop = !CmdPrivateMsg.snoop;
		MyTown.sendChatToPlayer(sender, "§aSnooping is now " + (CmdPrivateMsg.snoop ? "§4off" : "§2on"));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownadmCmdSnoopPrivateChatDesc.toString();
	}
}