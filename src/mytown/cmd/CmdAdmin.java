package mytown.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mytown.Formatter;
import mytown.Log;
import mytown.MyTown;
import mytown.Term;
import mytown.cmd.api.MyTownCommandBase;
import mytown.cmd.api.MyTownSubCommand;
import mytown.cmd.sub.admin.CmdClaim;
import mytown.cmd.sub.admin.CmdDumpDB;
import mytown.cmd.sub.admin.CmdExtraRes;
import mytown.cmd.sub.admin.CmdPerm;
import mytown.cmd.sub.admin.CmdReload;
import mytown.cmd.sub.admin.CmdResetFocusedChannels;
import mytown.cmd.sub.admin.CmdSnoopPM;
import mytown.cmd.sub.admin.CmdToggleGen;
import mytown.cmd.sub.admin.CmdTownBlocks;
import mytown.cmd.sub.admin.CmdTownDelete;
import mytown.cmd.sub.admin.CmdTownExtra;
import mytown.cmd.sub.admin.CmdTownNew;
import mytown.cmd.sub.admin.CmdTownRem;
import mytown.cmd.sub.admin.CmdTownSet;
import mytown.cmd.sub.admin.CmdUnclaim;
import mytown.cmd.sub.admin.CmdVersion;
import mytown.cmd.sub.admin.CmdWipeDim;
import mytown.cmd.sub.admin.CmdHome;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;

public class CmdAdmin extends MyTownCommandBase {
	Map<String, MyTownSubCommand> commands;

	public CmdAdmin() {
		commands = new HashMap<String, MyTownSubCommand>();
		commands.put("claim", new CmdClaim());
		commands.put("dump", new CmdDumpDB());
		commands.put("extrares", new CmdExtraRes());
		commands.put("home", new CmdHome());
		commands.put("perm", new CmdPerm());
		commands.put("reload", new CmdReload());
		commands.put("reschannels", new CmdResetFocusedChannels());
		commands.put("snoop", new CmdSnoopPM());
		commands.put("toggelgen", new CmdToggleGen());
		commands.put("blocks", new CmdTownBlocks());
		commands.put("delete", new CmdTownDelete());
		commands.put("extra", new CmdTownExtra());
		commands.put("new", new CmdTownNew());
		commands.put("rem", new CmdTownRem());
		commands.put("set", new CmdTownSet());
		commands.put("unclaim", new CmdUnclaim());
		commands.put("version", new CmdVersion());
		commands.put("wipedim", new CmdWipeDim());
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd";
	}

	@Override
	public String getCommandName() {
		return Term.TownAdmCommand.toString();
	}

	@Override
	public boolean canConsoleUse() {
		return true;
	}

	@Override
	public List<?> getCommandAliases() {
		return Arrays.asList(Term.TownAdmCommandAliases.toString().split(" "));
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/" + getCommandName();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		canCommandSenderUseCommand(sender);
		if (args.length < 1)
			return; // TODO: Print help command out?
		try {
			MyTownSubCommand cmd = commands.get(args[0]);
			if (cmd == null)
				throw new CommandNotFoundException();
			cmd.canUse(sender);
			cmd.process(sender, Arrays.copyOfRange(args, 1, args.length));
		} catch (NumberFormatException ex) {
			MyTown.sendChatToPlayer(sender, Formatter.commandError(Level.WARNING, Term.TownErrCmdNumberFormatException.toString()));
		} catch (CommandException ex) {
			throw ex;
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
}
