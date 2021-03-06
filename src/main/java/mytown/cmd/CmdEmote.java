package mytown.cmd;

import java.util.List;

import mytown.Assert;
import mytown.Formatter;
import mytown.MyTownDatasource;
import mytown.cmd.api.MyTownCommand;
import mytown.entities.Resident;
import net.minecraft.command.CommandServerEmote;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CmdEmote extends CommandServerEmote implements MyTownCommand {
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cs) {
		Assert.Perm(cs, getPermNode(), canConsoleUse());
		return true;
	}

	@Override
	public boolean canConsoleUse() {
		return true;
	}

	@Override
	public void processCommand(ICommandSender cs, String[] arg) {
		canCommandSenderUseCommand(cs);
		if (!Formatter.formatChat || arg.length < 1) {
			super.processCommand(cs, arg);
		} else {
			Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) cs);
			CmdChat.sendToChannel(res, func_82360_a(cs, arg, 0), res.activeChannel, true);
		}
	}

	/**
	 * Adds the strings available in this command to the given list of tab
	 * completion options.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
		return getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames());
	}

	@Override
	public List<String> dumpCommands() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.ecmd.me";
	}
	
	@Override
	public int compareTo(Object o) {
		return 0;
	}
}
