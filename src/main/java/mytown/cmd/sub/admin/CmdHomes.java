package mytown.cmd.sub.admin;

import java.util.List;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.SavedHome;
import mytown.entities.SavedHomeList;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Lists given user's homes
 * /ta homes player
 * @author Joe Goett
 */
public class CmdHomes extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "homes";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.homes";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1){
			throw new CommandException("Wrong number of arguments");
		}
		Resident res = MyTownDatasource.instance.getResident(args[0]);
		if (res == null){
			throw new CommandException(Term.TownErrPlayerNotFound.toString());
		}

		List<String> items = Lists.newArrayList();
		items.add(Term.HomeCmdHomesTitle.toString(res.name()));
		
		if (SavedHomeList.defaultIsBed && res.isOnline() && res.onlinePlayer!=null){
			if (res.onlinePlayer.getBedLocation(res.onlinePlayer.worldObj.provider.getRespawnDimension((EntityPlayerMP) res.onlinePlayer)) != null) {
				SavedHome s = SavedHome.fromBed((EntityPlayerMP) res.onlinePlayer);
				items.add(Term.HomeCmdHomesUnaccessibleItem2.toString("default", s.dim, (int) s.x, (int) s.y, (int) s.z));
			}
		}
		
		for (SavedHome h : res.home) {
			items.add(Term.HomeCmdHomesItem2.toString(h.name, h.dim, (int) h.x, (int) h.y, (int) h.z));
		}
		MyTown.sendChatToPlayer(sender, Joiner.on("\n").join(items));
	}
}