package mytown.cmd.sub.everyone;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class CmdTownInfo extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "info";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.info";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Town t = null;

		if (args.length == 1) { // Get town by name given
			t = MyTownDatasource.instance.getTown(args[0]);
		} else if (args.length < 1 && sender instanceof EntityPlayer) {
			Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
			t = res.town();
			if (t == null)
				throw new CommandException(Term.ErrNotInTown.toString());
		} else {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdInfo.toString(), Term.TownCmdInfoArgs.toString(), Term.TownCmdInfoDesc.toString(), null));
			return;
		}

		if (t == null) {
			throw new CommandException(Term.TownErrNotFound.toString(), args[0]);
		}

		t.sendTownInfo(sender);
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(MyTownDatasource.instance.towns.keySet());
		return list;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdInfoDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdInfoArgs.toString();
	}
}