package mytown.cmd.api;

import java.util.List;

import com.sperion.forgeperms.ForgePerms;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class MyTownCommandBase extends CommandBase {
	public abstract List<String> dumpCommands();
	public abstract String getPermNode();

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender){
        if (sender instanceof EntityPlayerMP) {
        	return ForgePerms.getPermissionManager().canAccess(sender.getCommandSenderName(), ((EntityPlayerMP) sender).worldObj.provider.getDimensionName(), getPermNode());
        }
        return false;
    }
}