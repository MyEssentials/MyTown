package mytown.cmd.sub.nation;

import java.util.List;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Nation;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CmdNationDelete extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "delete";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.nationdelete";
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null || res.rank() != Rank.Mayor) {
			return;
		}

		Town town = res.town();
		Nation nation = town.nation();

		if (args.length == 3 && args[2].equalsIgnoreCase("yes")) {
			nation.delete();

			String msg = Term.NationBroadcastDeleted.toString(nation.name());
			for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
				MyTown.sendChatToPlayer((EntityPlayer) obj, msg);
			}

			town.sendTownInfo(sender);
		} else {
			MyTown.sendChatToPlayer(sender, Term.NationDeleteConfirmation.toString());
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
