package ee.lutsu.alpha.mc.mytown.commands;

import com.sperion.forgeperms.ForgePerms;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

public class CmdSpawn extends CommandBase {
    @Override
    public String getCommandName() {
        return "spawn";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionsHandler().canAccess(p.username,
                    p.worldObj.provider.getDimensionName(), "mytown.adm.cmd");
        }
        return false;
        // return cs instanceof EntityPlayerMP &&
        // MyTown.instance.perms.canAccess(cs, "mytown.ecmd.spawn");
    }

    @Override
    public void processCommand(ICommandSender cs, String[] args) {
        EntityPlayerMP pl = (EntityPlayerMP) cs;
        Resident res = MyTownDatasource.instance.getOrMakeResident(pl);

        res.asyncStartSpawnTeleport(null);
    }
}
