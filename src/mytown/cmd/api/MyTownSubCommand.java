package mytown.cmd.api;

import java.util.List;

import mytown.CommandException;
import mytown.NoAccessException;
import net.minecraft.command.ICommandSender;

public interface MyTownSubCommand {
	public String getName();
	public String getPermNode();
	public void canUse(ICommandSender sender) throws CommandException, NoAccessException;
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException;
	public List<String> tabComplete(ICommandSender sender, String[] args);
}