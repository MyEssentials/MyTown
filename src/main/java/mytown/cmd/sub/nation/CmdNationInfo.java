package mytown.cmd.sub.nation;

import java.util.ArrayList;
import java.util.List;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Nation;
import mytown.entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdNationInfo extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "info";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.nationinfo";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Nation n = null;

		if (args.length < 1 && sender instanceof EntityPlayer) {
			Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
			if (res.town() == null) {
				throw new CommandException(Term.ErrNotInTown.toString());
			}
			if (res.town().nation() == null) {
				throw new CommandException(Term.TownErrNationSelfNotPartOfNation.toString());
			}
			n = res.town().nation();
		} else if (args.length == 1) {
			n = MyTownDatasource.instance.getNation(args[0]);
		} else {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationInfo.toString(), Term.TownCmdNationInfoArgs.toString(), Term.TownCmdNationInfoDesc.toString(), null));
			return;
		}

		if (n == null) {
			throw new CommandException(Term.TownErrNationNotFound.toString(), args[0]);
		}
		n.sendNationInfo(sender);
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(MyTownDatasource.instance.towns.keySet());
		return list;
	}

	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdNationInfoDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdNationInfoArgs.toString();
	}
}
