package mytown.cmd.sub.nation;

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

/**
 * Command to delete nations
 * /t nation delete yes
 * @author Joe Goett
 */
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
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null) {
			throw new CommandException(Term.ErrNotInTown.toString());
		}
		if (res.rank() != Rank.Mayor) {
			throw new CommandException(Term.ErrNotMayor.toString());
		}
		if (res.town().nation() == null) {
			throw new CommandException(Term.TownErrNationSelfNotPartOfNation.toString());
		}
		if (res.town().nation().capital() != res.town()) {
			throw new CommandException(Term.TownErrNationNotCapital.toString(res.town().nation().name()));
		}
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);

		Town town = res.town();
		Nation nation = town.nation();

		if (args.length == 1 && args[0].equalsIgnoreCase("yes")) {
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
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdNationDelDesc.toString();
	}
}
