package mytown.cmd.sub.nation;

import java.util.List;

import mytown.CommandException;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Nation;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CmdNationNew extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "new";
	}

	@Override
	public String getPermNode() {
		return "nation";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException, NoAccessException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null) {
			throw new CommandException(Term.ErrNotInTown);
		}
		if (res.rank() != Rank.Mayor) {
			throw new CommandException(Term.ErrNotMayor);
		}
		if (res.town().pendingNationInvitation == null) {
			throw new CommandException(Term.TownErrNationYouDontHavePendingInvitations);
		}
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Town town = res.town();

		if (args.length == 3) {
			String name = args[2];
			Nation n = new Nation(name, town);

			String msg = Term.NationBroadcastCreated.toString(town.name(), n.name());
			for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
				MyTown.sendChatToPlayer((EntityPlayer) obj, msg);
			}

			town.sendTownInfo(sender);
		} else {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationNew.toString(), Term.TownCmdNationNewArgs.toString(), Term.TownCmdNationNewDesc.toString(), null));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
