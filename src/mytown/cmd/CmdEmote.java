package mytown.cmd;

import java.util.List;

import mytown.Formatter;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownCommand;
import mytown.entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandServerEmote;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.sperion.forgeperms.ForgePerms;

public class CmdEmote extends CommandServerEmote implements MyTownCommand {
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cs) {
		if (cs instanceof EntityPlayerMP) {
			EntityPlayerMP p = (EntityPlayerMP) cs;
			return ForgePerms.getPermissionManager().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.ecmd.me");
		}
		return false;
	}

	@Override
	public boolean canConsoleUse() {
		return false;
	}

	@Override
	public void processCommand(ICommandSender cs, String[] arg) {
		if (!canCommandSenderUseCommand(cs))
			throw new CommandException(Term.ErrCannotAccessCommand.toString());
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
}
