package mytown.cmd.sub.nation;

import java.util.ArrayList;
import java.util.List;

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

public class CmdNationInvite extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "invite";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.nationinvite";
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
		if (res.town().nation() == null){
			throw new CommandException(Term.TownErrNationSelfNotPartOfNation.toString());
		}
		if (res.town().nation().capital() != res.town()){
			throw new CommandException(Term.TownErrNationNotCapital.toString(res.town().nation().name()));
		}
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Town town = res.town();
		Nation nation = town.nation();

		if (args.length == 2) {
			Town t = MyTownDatasource.instance.getTown(args[0]);
			if (t == null) {
				throw new CommandException(Term.TownErrNotFound.toString(), args[1]);
			}
			if (t == town) {
				throw new CommandException(Term.TownErrNationInvitingSelf.toString());
			}

			boolean mayorOnline = false;
			for (Resident r : t.residents()) {
				if (r.rank() == Rank.Mayor && r.isOnline()) {
					mayorOnline = true;
					MyTown.sendChatToPlayer(r.onlinePlayer, Term.NationInvitation.toString(res.name(), nation.name()));
					MyTown.sendChatToPlayer(sender, Term.NationInvitedPlayer.toString(r.name(), t.name()));
				}
			}
			if (mayorOnline) {
				t.pendingNationInvitation = nation;
			} else {
				MyTown.sendChatToPlayer(sender, Term.TownErrNationNoMayorOnline.toString(t.name()));
			}
		} else {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdNation.toString() + " " + Term.TownCmdNationKick.toString(), Term.TownCmdNationKickArgs.toString(), Term.TownCmdNationKickDesc.toString(), null));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(MyTownDatasource.instance.towns.keySet());
		return list;
	}

}
