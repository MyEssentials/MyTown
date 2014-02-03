package mytown.cmd;

import java.util.List;

import mytown.cmd.api.MyTownCommand;
import net.minecraft.command.CommandGameMode;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.sperion.forgeperms.ForgePerms;

public class CmdGamemode extends CommandGameMode implements MyTownCommand {
	@Override
	public String getCommandName() {
		return "gm";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cs) {
		if (cs instanceof EntityPlayerMP) {
			EntityPlayerMP p = (EntityPlayerMP) cs;
			return ForgePerms.getPermissionManager().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.gm");
		}
		return false;
	}
	
	@Override
    public void processCommand(ICommandSender cs, String[] args){
		if (!this.canCommandSenderUseCommand(cs)) return;
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
}