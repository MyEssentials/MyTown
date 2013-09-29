package ee.lutsu.alpha.mc.mytown.commands;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.sperion.forgeperms.ForgePerms;

import ee.lutsu.alpha.mc.mytown.ChatChannel;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

public class CmdChannel extends CommandBase {
    @Override
    public String getCommandName() {
        return Term.ChannelCommand.toString();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getCommandAliases() {
        return Arrays.asList(Term.ChannelCommandAliases.toString().split(" "));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
        return true;
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "/" + getCommandName() + " channel";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] var2) {
        if (!(sender instanceof EntityPlayer)) {
            return;
        }

        Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
        if (var2.length != 1) {
            MyTown.sendChatToPlayer(sender, Term.ChatListStart.toString());
            for (ChatChannel c : ChatChannel.values()) {
                if (c.enabled) {
                    MyTown.sendChatToPlayer(sender, Term.ChatListEntry.toString(c.color, c.name, c.color, c.abbrevation));
                }
            }
        } else {
            ChatChannel ch = ChatChannel.parse(var2[0]);
            if (!ch.enabled) {
                ch = ChatChannel.defaultChannel;
            }

            if (!ForgePerms.getPermissionsHandler().canAccess(res.onlinePlayer.username, res.onlinePlayer.worldObj.provider.getDimensionName(), "mytown.chat.focus." + ch.name.toLowerCase())) {
                MyTown.sendChatToPlayer(sender, "§4You cannot focus to " + ch.name + " channel");
                return;
            }

            CmdPrivateMsg.stopLockChatWithNotify(res.onlinePlayer);

            if (ch != res.activeChannel) {
                res.setActiveChannel(ch);
                MyTown.sendChatToPlayer(sender, Term.ChatSwitch.toString(ch.color, ch.abbrevation, ch.color, ch.name));
            } else {
                MyTown.sendChatToPlayer(sender, Term.ChatSwitchAlreadyIn.toString(ch.color, ch.abbrevation, ch.color, ch.name));
            }
        }
    }

}
