package mytown.cmd.sub.mayor;

import java.util.List;
import java.util.logging.Level;

import mytown.CommandException;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

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
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
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

}
