package mytown.cmd;

import java.util.List;

import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownCommandBase;
import mytown.entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class CmdSpawn extends MyTownCommandBase {
	@Override
	public String getCommandName() {
		return "spawn";
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) {
		if (!canCommandSenderUseCommand(cs))
			throw new CommandException(Term.ErrCannotAccessCommand.toString());
		EntityPlayerMP pl = (EntityPlayerMP) cs;
		Resident res = MyTownDatasource.instance.getOrMakeResident(pl);

		res.asyncStartSpawnTeleport(null);
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> dumpCommands() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.ecmd.spawn";
	}
}
