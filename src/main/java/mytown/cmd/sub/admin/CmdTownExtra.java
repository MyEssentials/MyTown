package mytown.cmd.sub.admin;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.List;

public class CmdTownExtra extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "extra";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.extra";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2) {
			MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdExtra.toString(), Term.TownadmCmdExtraArgs.toString(), Term.TownadmCmdExtraDesc.toString(), null));
		} else {
			Town t = MyTownDatasource.instance.getTown(args[0]);
			int extraBlocks = Integer.parseInt(args[1]);
			if (t == null) {
				throw new CommandException(Term.TownErrNotFound.toString(), args[0]);
			}

			t.setExtraBlocks(extraBlocks);
			MyTown.sendChatToPlayer(sender, Term.TownadmExtraSet.toString());
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownadmCmdExtraDesc.toString();
	}
	
	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownadmCmdExtraArgs.toString();
	}
}
