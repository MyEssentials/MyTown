package mytown.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mytown.CommandException;
import mytown.Formatter;
import mytown.Log;
import mytown.MyTown;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownCommandBase;
import mytown.cmd.api.MyTownSubCommand;
import mytown.cmd.sub.assistant.CmdSetSpawn;
import mytown.cmd.sub.assistant.CmdTownClaim;
import mytown.cmd.sub.assistant.CmdTownInvite;
import mytown.cmd.sub.assistant.CmdTownKick;
import mytown.cmd.sub.assistant.CmdTownPlot;
import mytown.cmd.sub.assistant.CmdTownUnclaim;
import mytown.cmd.sub.everyone.CmdFriend;
import mytown.cmd.sub.everyone.CmdHelp;
import mytown.cmd.sub.everyone.CmdSpawn;
import mytown.cmd.sub.everyone.CmdTownHere;
import mytown.cmd.sub.everyone.CmdTownInfo;
import mytown.cmd.sub.everyone.CmdTownList;
import mytown.cmd.sub.everyone.CmdTownMap;
import mytown.cmd.sub.everyone.CmdTownRes;
import mytown.cmd.sub.mayor.CmdSetAssistant;
import mytown.cmd.sub.mayor.CmdTownDelete;
import mytown.cmd.sub.mayor.CmdTownMayor;
import mytown.cmd.sub.mayor.CmdTownRename;
import mytown.cmd.sub.nation.CmdNation;
import mytown.cmd.sub.nonresident.CmdTownAccept;
import mytown.cmd.sub.nonresident.CmdTownDeny;
import mytown.cmd.sub.nonresident.CmdTownNew;
import mytown.cmd.sub.resident.CmdTownLeave;
import mytown.cmd.sub.resident.CmdTownOnline;
import mytown.cmd.sub.resident.CmdTownPerm;
import net.minecraft.command.ICommandSender;

public class CmdMyTown extends MyTownCommandBase {
	private Map<String, MyTownSubCommand> commands;

	public CmdMyTown() {
		commands = new HashMap<String, MyTownSubCommand>();

		// Assistant
		commands.put("setspawn", new CmdSetSpawn());
		commands.put("setspawn", new CmdTownClaim());
		commands.put("setspawn", new CmdTownInvite());
		commands.put("setspawn", new CmdTownKick());
		commands.put("setspawn", new CmdTownPlot());
		commands.put("setspawn", new CmdTownUnclaim());

		// Everyone
		commands.put("friend", new CmdFriend());
		commands.put("here", new CmdTownHere());
		commands.put("info", new CmdTownInfo());
		commands.put("list", new CmdTownList());
		commands.put("map", new CmdTownMap());
		commands.put("res", new CmdTownRes());
		commands.put("spawn", new CmdSpawn());
		commands.put("help", new CmdHelp());

		// Mayor
		commands.put("assistant", new CmdSetAssistant());
		commands.put("delete", new CmdTownDelete());
		commands.put("mayor", new CmdTownMayor());
		commands.put("rename", new CmdTownRename());

		// Nation
		commands.put("nation", new CmdNation());

		// Non-Resident
		commands.put("accept", new CmdTownAccept());
		commands.put("deny", new CmdTownDeny());
		commands.put("new", new CmdTownNew());

		// Resident
		commands.put("leave", new CmdTownLeave());
		commands.put("online", new CmdTownOnline());
		commands.put("perm", new CmdTownPerm());
	}

	@Override
	public String getCommandName() {
		return Term.TownCommand.toString();
	}

	@Override
	public List<?> getCommandAliases() {
		return Arrays.asList(Term.TownCommandAliases.toString().split(" "));
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/" + getCommandName();
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
		if (args.length == 0) {
			List<String> cmdList = new ArrayList<String>();
			cmdList.addAll(commands.keySet());
			return cmdList;
		}

		MyTownSubCommand cmd = commands.get(args[0]);
		if (cmd == null) {
			return null;
		}
		return cmd.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1)
			return;
		try {
			MyTownSubCommand cmd = commands.get(args[0]);
			if (cmd == null) {
				return; // TODO: Command doesn't exist
			}
			cmd.canUse(sender);
			cmd.process(sender, Arrays.copyOfRange(args, 1, args.length));
		} catch (NoAccessException ex) {
			MyTown.sendChatToPlayer(sender, ex.toString());
		} catch (NumberFormatException ex) {
			MyTown.sendChatToPlayer(sender, Formatter.commandError(Level.WARNING, Term.TownErrCmdNumberFormatException.toString()));
		} catch (CommandException ex) {
			MyTown.sendChatToPlayer(sender, Formatter.commandError(Level.WARNING, ex.errorCode.toString(ex.args)));
		} catch (Throwable ex) {
			Log.log(Level.WARNING, String.format("Command execution error by %s", sender), ex);
			MyTown.sendChatToPlayer(sender, Formatter.commandError(Level.SEVERE, ex.toString()));
		}
	}

	@Override
	public List<String> dumpCommands() {
		List<String> cmds = new ArrayList<String>();
		for (MyTownSubCommand c : commands.values()) {
			cmds.add("/" + getCommandName() + " " + c.getName() + ":" + c.getPermNode());
		}
		return cmds;
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd";
	}
}