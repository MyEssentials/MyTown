package ee.lutsu.alpha.mc.mytown.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.EnumGameType;

import com.sperion.forgeperms.ForgePerms;

import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
//import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.ext.Mffs;

public class CmdWrk extends CommandBase {
    @Override
    public String getCommandName() {
        return "wrk";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionManager().canAccess(p.getCommandSenderName(), p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.wrk");
        }
        return false;
    }

    @Override
    public void processCommand(ICommandSender cs, String[] args) {
        EntityPlayerMP pl = (EntityPlayerMP) cs;
        EnumGameType mode = pl.theItemInWorldManager.getGameType();
        String name = pl.getCommandSenderName().toLowerCase();

        if (args.length > 0 && args[0].equalsIgnoreCase("clip")) {
            pl.noClip = !pl.noClip;
            MyTown.sendChatToPlayer(pl, "NoClip is now " + (pl.noClip ? "active" : "deactive"));

            return;
        }

        if (MinecraftServer.getServer().getConfigurationManager().getOps().contains(name)) // to
                                                                                           // normal
                                                                                           // mode
        {
            String grp = name.equals("alphaest") ? "fakedev" : name.equals("sp0nge") ? "fakeowner" : "fakeadmin";

            MinecraftServer.getServer().getCommandManager().executeCommand(cs, "/pex user " + name + " group set " + grp);
            MinecraftServer.getServer().getConfigurationManager().removeOp(name);
            if (Mffs.check()) {
                Mffs.removeAdminBypass(name);
                Log.info("User " + name + " removed from MFFS bypass");
                MyTown.sendChatToPlayer(pl, "Removed from MFFS bypass");
            }

            if (mode != EnumGameType.SURVIVAL) {
                pl.setGameType(EnumGameType.SURVIVAL);
            }
        } else {
            String grp = name.equals("alphaest") ? "dev" : name.equals("sp0nge") ? "owner" : "admin";

            MinecraftServer.getServer().getCommandManager().executeCommand(cs, "/pex user " + name + " group set " + grp);
            MinecraftServer.getServer().getConfigurationManager().addOp(name);

            if (Mffs.check()) {
                Mffs.grantAdminBypass(name);
                Log.info("User " + name + " added to MFFS bypass");
                MyTown.sendChatToPlayer(pl, "Granted MFFS bypass");
            }

            if (mode != EnumGameType.CREATIVE) {
                pl.setGameType(EnumGameType.CREATIVE);
            }
        }
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        // TODO Auto-generated method stub
        return null;
    }
}
