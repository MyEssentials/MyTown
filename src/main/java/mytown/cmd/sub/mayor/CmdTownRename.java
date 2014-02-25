package mytown.cmd.sub.mayor;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.logging.Level;

public class CmdTownRename extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "rename";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.rename";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null)
			throw new CommandException(Term.ChatErrNotInTown.toString());
		if (res.rank().compareTo(Rank.Mayor) <= 0)
			throw new CommandException(Term.ErrPermRankNotEnough.toString());
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (args.length == 1) {
			res.town().setTownName(args[0]);
			res.town().sendNotification(Level.INFO, Term.TownRenamed.toString(res.town().name()));
		} else {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdRename.toString(), Term.TownCmdRenameArgs.toString(), Term.TownCmdRenameDesc.toString(), null));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdRenameDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdRenameArgs.toString();
	}
}
