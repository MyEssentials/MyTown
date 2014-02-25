package mytown.cmd.sub.nation;

import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Nation;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.logging.Level;

public class CmdNationLeave extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "leave";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.nationleave";
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null || res.rank() != Rank.Mayor) {
			return;
		}

		Town town = res.town();
		Nation nation = town.nation();

		nation.removeTown(town);
		town.sendNotification(Level.INFO, Term.NationLeft.toString(nation.name()));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdNationLeaveDesc.toString();
	}
}
