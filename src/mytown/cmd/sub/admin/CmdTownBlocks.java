package mytown.cmd.sub.admin;

import java.util.ArrayList;
import java.util.List;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CmdTownBlocks extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "blocks";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.blocks";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		canUse(sender);
		if (args.length < 1) {
			MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdTownBlocks.toString(), Term.TownadmCmdTownBlocksArgs.toString(), Term.TownadmCmdTownBlocksDesc.toString(), null));
		}
		Town t = MyTownDatasource.instance.getTown(args[0]);
		if (t == null) {
			throw new CommandException(Term.TownErrNotFound.toString(), args[0]);
		}

		MyTown.sendChatToPlayer(sender, "" + t.getBlocks());
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		List<String> townList = new ArrayList<String>();
		townList.addAll(MyTownDatasource.instance.towns.keySet());
		return townList;
	}

}
