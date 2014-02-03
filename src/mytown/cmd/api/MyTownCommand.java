package mytown.cmd.api;

import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

/**
 * Base for all MyTown commands
 * 
 * @author Joe Goett
 */
public interface MyTownCommand extends ICommand {
	/**
	 * Dumps this command and any associated sub-commands to a list
	 * 
	 * @return
	 */
	public List<String> dumpCommands();

	/**
	 * Gets the permission node for this command
	 * 
	 * @return
	 */
	public String getPermNode();

	public boolean canCommandSenderUseCommand(ICommandSender sender);
}