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
import mytown.cmd.sub.everyone.CmdFriend;
import mytown.cmd.sub.everyone.CmdHelp;
import mytown.cmd.sub.everyone.CmdSpawn;
import mytown.cmd.sub.everyone.CmdTownHere;
import mytown.cmd.sub.everyone.CmdTownInfo;
import mytown.cmd.sub.everyone.CmdTownList;
import mytown.cmd.sub.everyone.CmdTownMap;
import mytown.cmd.sub.everyone.CmdTownRes;
import mytown.cmd.sub.nonresident.CmdTownAccept;
import mytown.cmd.sub.nonresident.CmdTownDeny;
import mytown.cmd.sub.nonresident.CmdTownNew;
import net.minecraft.command.ICommandSender;

public class CmdMyTown extends MyTownCommandBase {
	private Map<String, MyTownSubCommand> commands;
	
	public CmdMyTown(){
		commands = new HashMap<String, MyTownSubCommand>();
		
		// Everyone
		commands.put("friend", new CmdFriend());
		commands.put("here", new CmdTownHere());
		commands.put("info", new CmdTownInfo());
		commands.put("list", new CmdTownList());
		commands.put("map", new CmdTownMap());
		commands.put("res", new CmdTownRes());
		commands.put("spawn", new CmdSpawn());
		commands.put("help", new CmdHelp());
		
		// Non-Residents
		commands.put("accept", new CmdTownAccept());
		commands.put("deny", new CmdTownDeny());
		commands.put("new", new CmdTownNew());
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

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args){
    	if (args.length == 0){
    		List<String> cmdList = new ArrayList<String>();
    		for (MyTownSubCommand command : commands.values()){
    			cmdList.add(command.getName());
    		}
    		return cmdList;
    	}
    	
    	MyTownSubCommand cmd = commands.get(args[0]);
		if (cmd == null){
			return null;
		}
        return cmd.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
    }
    
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1) return;
		try{
			MyTownSubCommand cmd = commands.get(args[0]);
			if (cmd == null){
				return; //TODO: Command doesn't exist
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
	public List<String> dumpCommands(){
		List<String> cmds = new ArrayList<String>();
		for (MyTownSubCommand c : commands.values()){
			cmds.add("/" + getCommandName() + " " + c.getName() + ":" + c.getPermNode());
		}
		return cmds;
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd";
	}
}