package mytown.cmd;

import java.util.List;
import java.util.logging.Level;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownCommandBase;
import mytown.entities.Resident;
import mytown.entities.SavedHome;
import mytown.entities.SavedHomeList;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class CmdHomes extends MyTownCommandBase {
	@Override
	public String getCommandName() {
		return "homes";
	}

	@Override
	public String getCommandUsage(ICommandSender cs) {
		return getCommandName() + " [loc] - Shows the player homes [with location]";
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) {
		canCommandSenderUseCommand(cs);
		EntityPlayerMP pl = (EntityPlayerMP) cs;
		Resident res = MyTownDatasource.instance.getOrMakeResident(pl);

		try {
			if (!res.home.hasHomes()) {
				MyTown.sendChatToPlayer(cs, Term.HomeCmdNoHomes.toString());
			} else {
				if (args.length == 1 && args[0].equalsIgnoreCase("loc")) {
					MyTown.sendChatToPlayer(cs, Term.HomeCmdHomesTitle.toString(pl.getEntityName()));
					if (SavedHomeList.defaultIsBed && pl.getBedLocation(pl.worldObj.provider.getRespawnDimension(pl)) != null) {
						SavedHome s = SavedHome.fromBed(pl);
						MyTown.sendChatToPlayer(cs, Term.HomeCmdHomesUnaccessibleItem2.toString("default", s.dim, (int) s.x, (int) s.y, (int) s.z));
					}

					for (SavedHome h : res.home) {
						MyTown.sendChatToPlayer(cs, Term.HomeCmdHomesItem2.toString(h.name, h.dim, (int) h.x, (int) h.y, (int) h.z));
					}
				} else {
					List<String> items = Lists.newArrayList();
					if (SavedHomeList.defaultIsBed && pl.getBedLocation(pl.worldObj.provider.getRespawnDimension(pl)) != null) {
						items.add(Term.HomeCmdHomesUnaccessibleItem.toString("default"));
					}

					for (SavedHome h : res.home) {
						items.add(Term.HomeCmdHomesItem.toString(h.name));
					}

					MyTown.sendChatToPlayer(cs, Term.HomeCmdHomesTitle.toString(Joiner.on(", ").join(items)));
				}
			}
		} catch (Throwable ex) {
			MyTown.instance.coreLog.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
			MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.SEVERE, ex.toString()));
		}
	}

	@Override
	public List<String> dumpCommands() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.ecmd.homes";
	}
}
