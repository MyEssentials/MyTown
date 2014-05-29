package mytown.cmd.sub.mayor;

import java.util.List;
import java.util.logging.Level;

import forgeperms.api.ForgePermsAPI;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdTownMayor extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "mayor";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.mayor";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null)
			throw new CommandException(Term.ChatErrNotInTown.toString());
		if (res.rank().compareTo(Rank.Mayor) < 0)
			throw new CommandException(Term.ErrPermRankNotEnough.toString());
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (args.length != 1) {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdMayor.toString(), Term.TownCmdMayorArgs.toString(), Term.TownCmdMayorDesc.toString(), null));
		} else {
			String name = args[0];

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

			if (!ForgePermsAPI.permManager.canAccess(r.onlinePlayer.username, r.onlinePlayer.worldObj.provider.getDimensionName(), "mytown.cmd.new")) {
				throw new CommandException(Term.TownErrPlayerDoesntHaveAccessToTownManagement.toString());
			}

			res.town().setResidentRank(r, Rank.Mayor);
			res.town().setResidentRank(res, Rank.Assistant);

			res.town().sendNotification(Level.INFO, Term.TownPlayerPromotedToMayor.toString(r.name()));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdMayorDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdMayorArgs.toString();
	}
}
