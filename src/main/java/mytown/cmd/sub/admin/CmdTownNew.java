package mytown.cmd.sub.admin;

import java.util.List;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CmdTownNew extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "new";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.new";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 2) {
			MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdNew.toString(), Term.TownadmCmdNewArgs.toString(), Term.TownadmCmdNewDesc.toString(), null));
		} else {
			Resident r = MyTownDatasource.instance.getOrMakeResident(args[1]);
			Town t = new Town(args[0], r, null);
			MyTown.sendChatToPlayer(sender, Term.TownadmCreatedNewTown.toString(t.name(), r.name()));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownadmCmdNewDesc.toString();
	}
	
	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownadmCmdNewArgs.toString();
	}
}
