package mytown.cmd.api;

import mytown.Assert;
import mytown.CommandException;
import mytown.NoAccessException;
import net.minecraft.command.ICommandSender;

public abstract class MyTownSubCommandAdapter implements MyTownSubCommand {
	@Override
	public void canUse(ICommandSender sender) throws CommandException, NoAccessException {
		Assert.Perm(sender, getPermNode());
	}
}