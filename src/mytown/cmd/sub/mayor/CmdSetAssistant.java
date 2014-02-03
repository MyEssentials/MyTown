package mytown.cmd.sub.mayor;

import java.util.List;
import java.util.logging.Level;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdSetAssistant extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "assistant";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.assistant";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (args.length != 2) {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdAssistant.toString(), Term.TownCmdAssistantArgs.toString(), Term.TownCmdAssistantDesc.toString(), null));
		} else {
			String cmd = args[0];
			String name = args[1];

			Resident r = MyTownDatasource.instance.getResident(name);
			if (r == null) {
				throw new CommandException(Term.TownErrPlayerNotFound.toString());
			}
			if (r == res) {
				throw new CommandException(Term.TownErrCannotDoWithYourself.toString());
			}
			if (r.town() != res.town()) {
				throw new CommandException(Term.TownErrPlayerNotInYourTown.toString());
			}

			if (cmd.equalsIgnoreCase(Term.TownCmdAssistantArgs1.toString())) // add
			{
				if (r.rank() == Rank.Mayor) {
					throw new CommandException(Term.TownErrCannotUseThisDemoteMayor.toString());
				}
				if (r.rank() == Rank.Assistant) {
					throw new CommandException(Term.TownErrPlayerIsAlreadyAssistant.toString());
				}

				res.town().setResidentRank(r, Rank.Assistant);
				res.town().sendNotification(Level.INFO, Term.TownPlayerPromotedToAssistant.toString(r.name()));
			} else if (cmd.equalsIgnoreCase(Term.TownCmdAssistantArgs2.toString())) // remove
			{
				if (r.rank() != Rank.Assistant) {
					throw new CommandException(Term.TownErrPlayerIsNotAssistant.toString());
				}

				res.town().setResidentRank(r, Rank.Resident);
				res.town().sendNotification(Level.INFO, Term.TownPlayerDemotedFromAssistant.toString(r.name()));
			} else {
				MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdAssistant.toString(), Term.TownCmdAssistantArgs.toString(), Term.TownCmdAssistantDesc.toString(), null));
			}
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}