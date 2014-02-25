package mytown.cmd;

import java.util.List;

import mytown.MyTown;
import mytown.cmd.api.MyTownCommand;
import net.minecraft.command.CommandGameMode;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class CmdGamemode extends CommandGameMode implements MyTownCommand {
	@Override
	public String getCommandName() {
		return "gm";
	}

	@Override
	public boolean canConsoleUse() {
		return false;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cs) {
		if (cs instanceof EntityPlayerMP) {
			EntityPlayerMP p = (EntityPlayerMP) cs;
			return MyTown.instance.permManager.canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.gm");
		}
		return false;
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) {
		canCommandSenderUseCommand(cs);
		super.processCommand(cs, args);
	}

	@Override
	public List<String> dumpCommands() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.gm";
	}
	
	@Override
	public int compareTo(Object o) {
		return 0;
	}
}