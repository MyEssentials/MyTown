package ee.lutsu.alpha.mc.mytown.commands;

import java.util.Arrays;
import java.util.List;

import com.sperion.forgeperms.ForgePerms;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import ee.lutsu.alpha.mc.mytown.MyTown;

public class CmdReplyPrivateMsg extends CommandBase {
    @Override
    public List getCommandAliases() {
        return Arrays.asList(new String[] { "r" });
    }

    @Override
    public String getCommandName() {
        return "reply";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionsHandler().canAccess(p.username,
                    p.worldObj.provider.getDimensionName(), "mytown.adm.cmd");
        }
        return false;
        // return cs instanceof EntityPlayer &&
        // MyTown.instance.perms.canAccess(cs, "mytown.ecmd.reply");
    }

    @Override
    public void processCommand(ICommandSender cs, String[] arg) {
        EntityPlayer pl = CmdPrivateMsg.lastMessages.get(cs);

        if (pl == null) {
            cs.sendChatToPlayer("§4Noone to reply to");
        } else {
            if (arg.length > 0) {
                CmdPrivateMsg.sendChat((EntityPlayer) cs, pl,
                        func_82360_a(cs, arg, 0));
            } else {
                CmdPrivateMsg.lockChatWithNotify((EntityPlayer) cs, pl);
            }
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab
     * completion options.
     */
    @Override
    public List addTabCompletionOptions(ICommandSender par1ICommandSender,
            String[] par2ArrayOfStr) {
        return getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer
                .getServer().getAllUsernames());
    }
}
