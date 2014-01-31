package mytown.commands;

import mytown.MyTownDatasource;
import mytown.entities.Resident;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.sperion.forgeperms.ForgePerms;

public class CmdSpawn extends CommandBase {
    @Override
    public String getCommandName() {
        return "spawn";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionManager().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.ecmd.spawn");
        }
        return false;
    }

    @Override
    public void processCommand(ICommandSender cs, String[] args) {
        EntityPlayerMP pl = (EntityPlayerMP) cs;
        Resident res = MyTownDatasource.instance.getOrMakeResident(pl);

        res.asyncStartSpawnTeleport(null);
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        // TODO Auto-generated method stub
        return null;
    }
}
