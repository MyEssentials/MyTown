package ee.lutsu.alpha.mc.mytown.commands;

import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandServerMessage;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.StringTranslate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sperion.forgeperms.ForgePerms;

import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
//import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

public class CmdPrivateMsg extends CommandServerMessage {
    public static Map<EntityPlayer, EntityPlayer> lastMessages = Maps
            .newHashMap();
    public static Map<EntityPlayer, EntityPlayer> chatLock = Maps.newHashMap();
    public static List<ICommandSender> snoopers = Lists.newArrayList();

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionsHandler().canAccess(p.username,
                    p.worldObj.provider.getDimensionName(), "mytown.adm.cmd");
        }
        return false;
    }

    @Override
    public void processCommand(ICommandSender cs, String[] arg) {
        if (arg.length > 0) {
            EntityPlayerMP target = func_82359_c(cs, arg[0]);

            if (target == null) {
                throw new PlayerNotFoundException();
            }

            if (arg.length > 1){ // send chat
                String msg = func_82361_a(cs, arg, 1,
                        !(cs instanceof EntityPlayer));
                sendChat((EntityPlayer) cs, target, msg);
            } else{ // lock mode
                lockChatWithNotify((EntityPlayer) cs, target);
            }
        } else if (chatLock.get(cs) != null) {
            stopLockChatWithNotify((EntityPlayer) cs);
        } else {
            MyTown.sendChatToPlayer(cs, "§4Usage /tell [target] [msg]");
        }
    }

    public static void stopLockChatWithNotify(EntityPlayer sender) {
        EntityPlayer pl = chatLock.remove(sender);

        if (pl != null) {
            MyTown.sendChatToPlayer(pl, "§dStopped chatting with " + Resident.getOrMake(pl).formattedName());
        }
    }

    public static void lockChatWithNotify(EntityPlayer sender,
            EntityPlayer target) {
        chatLock.put(sender, target);
        MyTown.sendChatToPlayer(sender, "§dNow chatting with " + Resident.getOrMake(target).formattedName());
    }

    public static void sendChat(EntityPlayer sender, EntityPlayer target, String msg) {
        if (sender == null || target == null || msg == null) {
            return;
        }

        lastMessages.put(target, sender);

        if (ForgePerms.getPermissionsHandler().canAccess(sender.username, sender.worldObj.provider.getDimensionName(), "mytown.chat.allowcolors")) {
            msg = Formatter.dollarToColorPrefix(msg);
        }

        for (ICommandSender cs : snoopers) {
            Log.direct(String.format("§7[%s §7-> %s§7] %s", Resident.getOrMake(sender).formattedName(), Resident.getOrMake(target).formattedName(), msg));
        }

        if (!Formatter.formatChat) {
            StringTranslate strTranslate = new StringTranslate();
            MyTown.sendChatToPlayer(sender, "\u00a77\u00a7o"+ strTranslate.translateKeyFormat("commands.message.display.outgoing", new Object[] {target.getCommandSenderName(), msg }));
            MyTown.sendChatToPlayer(target, "\u00a77\u00a7o"+ strTranslate.translateKeyFormat("commands.message.display.incoming", new Object[] {sender.getCommandSenderName(), msg }));
        } else {
            MyTown.sendChatToPlayer(sender, Formatter.formatPrivMsg(Resident.getOrMake(sender), Resident.getOrMake(target), msg, true));
            MyTown.sendChatToPlayer(target, Formatter.formatPrivMsg(Resident.getOrMake(sender), Resident.getOrMake(target), msg, false));
        }
    }
}
