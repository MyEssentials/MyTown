package mytown.cmd.sub.resident;

import java.util.List;
import java.util.logging.Level;

import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdTownLeave extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "leave";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.leave";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null)
			throw new CommandException(Term.ChatErrNotInTown.toString());
		if (res.rank() == Rank.Mayor)
			throw new CommandException(Term.TownErrMayorsCantLeaveTheTown.toString());
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Town t = res.town();
		t.sendNotification(Level.INFO, Term.TownPlayerLeft.toString(res.name()));
		t.removeResident(res);
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdLeaveDesc.toString();
	}
}