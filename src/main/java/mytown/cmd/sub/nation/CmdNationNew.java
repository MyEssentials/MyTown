package mytown.cmd.sub.nation;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Nation;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.List;

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
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null) {
			throw new CommandException(Term.ErrNotInTown.toString());
		}
		if (res.rank() != Rank.Mayor) {
			throw new CommandException(Term.ErrNotMayor.toString());
		}
		if (res.town().nation() != null) {
			throw new CommandException(Term.TownErrAlreadyInNation.toString());
		}
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Town town = res.town();

		if (args.length == 1) {
			String name = args[0];
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
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdNationNewDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdNationNewArgs.toString();
	}
}
