package mytown.commands;

import java.util.Arrays;
import java.util.List;

import mytown.MyTown;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.sperion.forgeperms.ForgePerms;

public class CmdReplyPrivateMsg extends CommandBase {
    @Override
    public List<?> getCommandAliases() {
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
            return ForgePerms.getPermissionManager().canAccess(p.getCommandSenderName(), p.worldObj.provider.getDimensionName(), "mytown.ecmd.reply");
        }
        return false;
    }

    @Override
    public void processCommand(ICommandSender cs, String[] arg) {
        EntityPlayer pl = CmdPrivateMsg.lastMessages.get(cs);

        if (pl == null) {
            MyTown.sendChatToPlayer(cs, "§4Noone to reply to");
        } else {
            if (arg.length > 0) {
                CmdPrivateMsg.sendChat((EntityPlayer) cs, pl, func_82360_a(cs, arg, 0));
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
    public List<?> addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
        return getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames());
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        // TODO Auto-generated method stub
        return null;
    }
}
