package mytown.cmd.sub.nation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mytown.Formatter;
import mytown.MyTown;
import mytown.cmd.api.MyTownSubCommand;
import mytown.cmd.api.MyTownSubCommandAdapter;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;

public class CmdNation extends MyTownSubCommandAdapter {
	private Map<String, MyTownSubCommand> commands;

	public CmdNation() {
		commands = new HashMap<String, MyTownSubCommand>();

		commands.put("accept", new CmdNationAccept());
		commands.put("delete", new CmdNationDelete());
		commands.put("deny", new CmdNationDeny());
		commands.put("info", new CmdNationInfo());
		commands.put("invite", new CmdNationInvite());
		commands.put("kick", new CmdNationKick());
		commands.put("leave", new CmdNationLeave());
		commands.put("list", new CmdNationList());
		commands.put("new", new CmdNationNew());
		commands.put("transfer", new CmdNationTransfer());
	}

	@Override
	public String getName() {
		return "nation";
	}

	@Override
	public String getPermNode() {
		return null;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1 || args[0].equalsIgnoreCase("help")){
			printHelp(sender);
			return;
		}
		MyTownSubCommand cmd = commands.get(args[0]);
		if (cmd == null)
			throw new CommandNotFoundException();
		cmd.canUse(sender);
		cmd.process(sender, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		if (args.length == 0) {
			List<String> cmdList = new ArrayList<String>();
			cmdList.addAll(commands.keySet());
			return cmdList;
		}

		MyTownSubCommand cmd = commands.get(args[0]);
		if (cmd == null)
			return null;
		return cmd.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
	}

	private void printHelp(ICommandSender sender){
		StringBuilder help = new StringBuilder();
		help.append("§Nation Commands");
		for (MyTownSubCommand cmd : commands.values()){
			try{
				cmd.canUse(sender);
				String desc = cmd.getDesc(sender);
				String args = cmd.getArgs(sender);
				help.append("\n");
				help.append(Formatter.formatAdminCommand(cmd.getName(), args == null ? "" : args, desc == null ? "" : desc, "b"));
			} catch(Exception e){}  // Ignore
		}
		MyTown.sendChatToPlayer(sender, Formatter.applyColorCodes(help.toString()));
	}
}
