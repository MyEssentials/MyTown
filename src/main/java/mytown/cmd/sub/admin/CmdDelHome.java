package mytown.cmd.sub.admin;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.logging.Level;

import mytown.Formatter;

/**
 * Allows admins to delete other players homes /ta delhome [playername]
 * [homename]
 * 
 * @author Ken Woodland
 */
public class CmdDelHome extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "delhome";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.home";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		canUse(sender);
		if (args.length < 1) {
			throw new CommandException(Term.TownadmCmdHomeArgs.toString());
		}
		Resident res = MyTownDatasource.instance.getResident(args[0]);
		if (res == null) {
			throw new CommandException(Term.TownErrPlayerNotFound.toString());
		}

		try {
			if (!res.home.hasHomes()) {
				throw new CommandException(Term.HomeCmdNoHomes.toString());
			}

			res.home.delete(args.length == 1 ? null : args[1]);
			MyTown.sendChatToPlayer(sender, args.length == 1 ? Term.HomeCmdHomeDeleted.toString() : Term.HomeCmdHome2Deleted.toString(args[1]));
		} catch (Throwable ex) {
			MyTown.instance.coreLog.log(Level.WARNING, String.format("Command execution error by %s", sender), ex);
			MyTown.sendChatToPlayer(sender, Formatter.commandError(Level.SEVERE, ex.toString()));
		}
	}
}
