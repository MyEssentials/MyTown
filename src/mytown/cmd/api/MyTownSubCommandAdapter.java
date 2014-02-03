package mytown.cmd.api;

import mytown.Assert;
import mytown.NoAccessException;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/**
 * A helper class for MyTownSubCommand. Allows anyone with the given perm node,
 * and blocks the console by default.
 * 
 * @author Joe Goett
 */
public abstract class MyTownSubCommandAdapter implements MyTownSubCommand {
	@Override
	public void canUse(ICommandSender sender) throws CommandException, NoAccessException {
		Assert.Perm(sender, getPermNode(), canUseByConsole());
	}

	@Override
	public boolean canUseByConsole() {
		return false;
	}
}