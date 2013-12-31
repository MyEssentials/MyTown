package ee.lutsu.alpha.mc.mytown;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import com.sperion.forgeperms.ForgePerms;

public class Assert {
    public static void Perm(ICommandSender cs, String node) throws NoAccessException {
        if (cs instanceof MinecraftServer){
            return;
        }
        EntityPlayer p = (EntityPlayer) cs;
        if (ForgePerms.getPermissionManager().canAccess(p.getCommandSenderName(), p.worldObj.provider.getDimensionName(), node)) {
            return;
        }
        throw new NoAccessException(cs, node);
    }
}
