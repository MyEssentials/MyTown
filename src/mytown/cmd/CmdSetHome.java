package mytown.cmd;

import java.util.List;
import java.util.logging.Level;

import mytown.CommandException;
import mytown.Cost;
import mytown.Formatter;
import mytown.Log;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownCommandBase;
import mytown.entities.PayHandler;
import mytown.entities.Resident;
import mytown.entities.SavedHome;
import mytown.entities.TownSettingCollection;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class CmdSetHome extends MyTownCommandBase {
	@Override
	public String getCommandName() {
		return "sethome";
	}

	@Override
	public String getCommandUsage(ICommandSender cs) {
		return getCommandName() + " [name] - Sets a new home location";
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) {
		if (!this.canCommandSenderUseCommand(cs)) return;
		EntityPlayerMP pl = (EntityPlayerMP) cs;
		Resident res = MyTownDatasource.instance.getOrMakeResident(pl);

		try {
			if (!res.canInteract(pl.dimension, (int) pl.posX, (int) pl.posY, (int) pl.posZ, TownSettingCollection.Permissions.Build)) {
				throw new CommandException(Term.HomeCmdCannotSetHere);
			}

			res.home.assertSetHome(args.length == 0 ? null : args[0], pl);

			ItemStack request = null;
			SavedHome h = res.home.get(args.length == 0 ? null : args[0]);
			if (h == null) {
				if (Cost.HomeSetNew.item != null) {
					request = Cost.HomeSetNew.item.copy();
					request.stackSize += Cost.homeSetNewAdditional * res.home.size();
				}
			} else {
				if (Cost.HomeReplace.item != null) {
					request = Cost.HomeReplace.item;
				}
			}

			res.pay.requestPayment(h == null ? "homenew" : "homereplace", request, new PayHandler.IDone() {
				@Override
				public void run(Resident res, Object[] args) {
					setHome(res, (EntityPlayerMP) res.onlinePlayer, (String[]) args[0]);
				}
			}, (Object) args);
		} catch (NoAccessException ex) {
			MyTown.sendChatToPlayer(cs, ex.toString());
		} catch (CommandException ex) {
			MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.WARNING, ex.errorCode.toString(ex.args)));
		} catch (Throwable ex) {
			Log.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
			MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.SEVERE, ex.toString()));
		}
	}

	public static void setHome(Resident res, EntityPlayerMP pl, String[] args) {
		res.home.set(args.length == 0 ? null : args[0], pl);
		MyTown.sendChatToPlayer(pl, args.length == 0 ? Term.HomeCmdHomeSet.toString() : Term.HomeCmdHome2Set.toString(args[0]));
	}

	
	@Override
	public List<String> dumpCommands() {
		return null;
	}
	

	@Override
	public String getPermNode() {
		return "mytown.ecmd.sethome";
	}
}
