package mytown.cmd.sub.everyone;

import java.util.List;

import mytown.CommandException;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdTownHere extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "here";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.here";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		TownBlock block = MyTownDatasource.instance.getPermBlockAtCoord(res.onlinePlayer.dimension, (int) res.onlinePlayer.posX, (int) res.onlinePlayer.posY, (int) res.onlinePlayer.posZ);
		if (block == null) {
			return;
		}

		Town t = block.town();
		if (t == null) {
			return;
		}

		t.sendTownInfo(sender);
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}