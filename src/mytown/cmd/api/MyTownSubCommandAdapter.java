package mytown.cmd.api;

import java.util.List;

import mytown.Assert;
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
	public void canUse(ICommandSender sender) throws CommandException {
		Assert.Perm(sender, getPermNode(), canUseByConsole());
	}

	@Override
	public boolean canUseByConsole() {
		return false;
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}