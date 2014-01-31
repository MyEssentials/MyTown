package mytown;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;

import com.sperion.forgeperms.ForgePerms;

public class Assert {
    public static void Perm(ICommandSender cs, String node) throws NoAccessException, CommandException {
        if (cs instanceof MinecraftServer || cs instanceof RConConsoleSource) throw new CommandException(Term.ErrNotUsableByConsole);
        EntityPlayer p = (EntityPlayer) cs;
        if (ForgePerms.getPermissionManager().canAccess(p.username, p.worldObj.provider.getDimensionName(), node)) {
            return;
        }
        throw new NoAccessException(cs, node);
    }
}
