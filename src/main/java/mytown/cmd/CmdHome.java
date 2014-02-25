package mytown.cmd;

import java.util.List;
import java.util.logging.Level;

import mytown.Cost;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownCommandBase;
import mytown.entities.PayHandler;
import mytown.entities.Resident;
import mytown.entities.SavedHome;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class CmdHome extends MyTownCommandBase {
	@Override
	public String getCommandName() {
		return "home";
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) {
		canCommandSenderUseCommand(cs);
		EntityPlayerMP pl = (EntityPlayerMP) cs;
		Resident res = MyTownDatasource.instance.getOrMakeResident(pl);

		try {
			SavedHome h = res.home.get(args.length == 0 ? null : args[0]);

			if (!res.home.hasHomes()) {
				throw new CommandException(Term.HomeCmdNoHomes.toString());
			}

			if (h == null) {
				throw new CommandException(Term.HomeCmdNoHomeByName.toString());
			}

			res.pay.requestPayment("hometeleport", Cost.HomeTeleport.item, new PayHandler.IDone() {
				@Override
				public void run(Resident player, Object[] args) {
					teleport(player, (SavedHome) args[0]);
				}
			}, h);

		} catch (CommandException ex) {
			throw ex;
		} catch (Throwable ex) {
			MyTown.instance.coreLog.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
			MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.SEVERE, ex.toString()));
		}
	}

	public static void teleport(Resident res, SavedHome h) {
		if (Cost.HomeTeleport.item != null && Resident.teleportToHomeWaitSeconds > 0) {
			MyTown.sendChatToPlayer(res.onlinePlayer, Term.HomeCmdDontMove.toString());
		}

		res.asyncStartSpawnTeleport(h);
	}

	@Override
	public String getCommandUsage(ICommandSender s) {
		return "/" + getCommandName();
	}

	@Override
	public List<String> dumpCommands() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.ecmd.home";
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayerMP) sender);
		return res.home;
	}
}
