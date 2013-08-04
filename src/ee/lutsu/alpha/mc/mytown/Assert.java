package ee.lutsu.alpha.mc.mytown;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.sperion.forgeperms.ForgePerms;

public class Assert {
    public static void Perm(ICommandSender cs, String node) throws NoAccessException {
        EntityPlayer p = (EntityPlayer) cs;
        ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), node);
        
        throw new NoAccessException(cs, node);
    }
}
