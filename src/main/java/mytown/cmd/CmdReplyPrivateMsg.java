package mytown.cmd;

import java.util.Arrays;
import java.util.List;

import mytown.MyTown;
import mytown.cmd.api.MyTownCommandBase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CmdReplyPrivateMsg extends MyTownCommandBase {
	@Override
	public List<?> getCommandAliases() {
		return Arrays.asList(new String[] { "r" });
	}

	@Override
	public String getCommandName() {
		return "reply";
	}

	@Override
	public void processCommand(ICommandSender cs, String[] arg) {
		canCommandSenderUseCommand(cs);
		ICommandSender pl = CmdPrivateMsg.lastMessages.get(cs);

		if (pl == null) {
			MyTown.sendChatToPlayer(cs, "§4Noone to reply to");
		} else {
			if (arg.length > 0) {
				CmdPrivateMsg.sendChat(cs, pl, CommandBase.func_82360_a(cs, arg, 0));
			} else {
				CmdPrivateMsg.lockChatWithNotify(cs, pl);
			}
		}
	}

	/**
	 * Adds the strings available in this command to the given list of tab
	 * completion options.
	 */
	@Override
	public List<?> addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
		return CommandBase.getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames());
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> dumpCommands() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.ecmd.reply";
	}
}
