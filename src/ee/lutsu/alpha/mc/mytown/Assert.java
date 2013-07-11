package ee.lutsu.alpha.mc.mytown;

import com.sperion.forgeperms.ForgePerms;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class Assert {
    public static void Perm(ICommandSender cs, String node)
            throws NoAccessException {
        // if (!Permissions.canAccess(cs, node))
        EntityPlayer p = (EntityPlayer) cs;
        if (!ForgePerms.getPermissionsHandler().canAccess(p.username,
                p.worldObj.provider.getDimensionName(), node)) {
            throw new NoAccessException(cs, node);
        }
    }
}
