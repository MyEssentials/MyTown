package mytown.cmd.api;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

import java.util.List;

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

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender);

	/**
	 * Returns whether the console can use this or not
	 * 
	 * @return
	 */
	public boolean canConsoleUse();
}