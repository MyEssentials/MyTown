package ee.lutsu.alpha.mc.mytown;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.sperion.forgeperms.ForgePerms;

public class Assert {
    public static void Perm(ICommandSender cs, String node) throws NoAccessException {
        EntityPlayer p = (EntityPlayer) cs;
        String[] nodes = node.split("\\|");
        for(String n : nodes){
            if (ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), n.trim())) {
                return;
            }
        }
        
        throw new NoAccessException(cs, node);
    }
}
