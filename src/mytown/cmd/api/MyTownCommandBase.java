package mytown.cmd.api;

import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;

import com.sperion.forgeperms.ForgePerms;

/**
 * Base for all MyTown commands
 * 
 * @author Joe Goett
 */
public abstract class MyTownCommandBase implements MyTownCommand {
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		if (sender instanceof EntityPlayerMP) {
			return ForgePerms.getPermissionManager().canAccess(sender.getCommandSenderName(), ((EntityPlayerMP) sender).worldObj.provider.getDimensionName(), getPermNode());
		} else if ((sender instanceof MinecraftServer || sender instanceof RConConsoleSource) && canConsoleUse()) {
			return true;
		}
		return false;
	}

	@Override
	public List<?> getCommandAliases() {
		return null;
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender icommandsender, String[] astring) {
		return null;
	}

	@Override
	public boolean canConsoleUse() {
		return false;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i) {
		return false;
	}

	public int compareTo(ICommand par1ICommand) {
		return getCommandName().compareTo(par1ICommand.getCommandName());
	}

	@Override
	public int compareTo(Object par1Obj) {
		return this.compareTo((ICommand) par1Obj);
	}
}