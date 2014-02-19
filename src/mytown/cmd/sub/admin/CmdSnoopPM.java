package mytown.cmd.sub.admin;

import java.util.List;

import mytown.MyTown;
import mytown.Term;
import mytown.cmd.CmdPrivateMsg;
import mytown.cmd.api.MyTownSubCommandAdapter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

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
		boolean done = CmdPrivateMsg.snoopers.remove(MinecraftServer.getServer());
		if (!done) {
			CmdPrivateMsg.snoopers.add(MinecraftServer.getServer());
		}

		MyTown.sendChatToPlayer(sender, "§aSnooping is now " + (done ? "§4off" : "§2on"));
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