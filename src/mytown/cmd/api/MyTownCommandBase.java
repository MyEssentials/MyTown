package mytown.cmd.api;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.sperion.forgeperms.ForgePerms;

/**
 * Base for all MyTown commands
 * 
 * @author Joe Goett
 */
public abstract class MyTownCommandBase extends CommandBase {
	/**
	 * Dumps this command and any associated sub-commands to a list
	 * 
	 * @return
	 */
	public abstract List<String> dumpCommands();

	/**
	 * Gets the permission node for this command
	 * 
	 * @return
	 */
	public abstract String getPermNode();

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		if (sender instanceof EntityPlayerMP) {
			return ForgePerms.getPermissionManager().canAccess(sender.getCommandSenderName(), ((EntityPlayerMP) sender).worldObj.provider.getDimensionName(), getPermNode());
		}
		return false;
	}
}