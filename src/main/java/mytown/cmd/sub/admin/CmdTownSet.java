package mytown.cmd.sub.admin;

import java.util.List;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CmdTownSet extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "set";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.set";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 3) {
			MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdSet.toString(), Term.TownadmCmdSetArgs.toString(), Term.TownadmCmdSetDesc.toString(), null));
		} else {
			Town t = MyTownDatasource.instance.getTown(args[0]);
			if (t == null) {
				throw new CommandException(Term.TownErrNotFound.toString(), args[0]);
			}

			Rank rank = Rank.parse(args[1]);

			for (int i = 2; i < args.length; i++) {
				Resident r = MyTownDatasource.instance.getOrMakeResident(args[i]);
				if (r.town() != null) {
					if (r.town() != t) {
						r.town().removeResident(r); // unloads the
													// resident
						r = MyTownDatasource.instance.getOrMakeResident(args[i]);
					}
				}

				t.addResident(r);
				t.setResidentRank(r, rank);
			}
			MyTown.sendChatToPlayer(sender, Term.TownadmResidentsSet.toString());
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownadmCmdSetDesc.toString();
	}
	
	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownadmCmdSetArgs.toString();
	}
}
