package mytown.cmd.sub.assistant;

import java.util.List;
import java.util.logging.Level;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import mytown.CommandException;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;

public class CmdTownKick extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "kick";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.kick";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
        Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
        if (args.length != 1) {
            MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdKick.toString(), Term.TownCmdKickArgs.toString(), Term.TownCmdKickDesc.toString(), null));
        } else {
            Resident target = MyTownDatasource.instance.getResident(args[0]);

            if (target == null) {
                throw new CommandException(Term.TownErrPlayerNotFound);
            }

            if (target == res) {
                throw new CommandException(Term.TownErrCannotKickYourself);
            }
            if (target.town() != res.town()) {
                throw new CommandException(Term.TownErrPlayerNotInYourTown);
            }
            if (target.rank() == Rank.Mayor && res.rank() == Rank.Assistant) {
                throw new CommandException(Term.TownErrCannotKickMayor);
            }
            if (target.rank() == Rank.Assistant && res.rank() == Rank.Assistant) {
                throw new CommandException(Term.TownErrCannotKickAssistants);
            }

            res.town().removeResident(target);

            res.town().sendNotification(Level.INFO, Term.TownKickedPlayer.toString(res.name(), target.name()));
        }
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}