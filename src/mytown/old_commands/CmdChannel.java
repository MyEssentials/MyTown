package mytown.old_commands;

import java.util.Arrays;
import java.util.List;

import mytown.ChatChannel;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.CmdPrivateMsg;
import mytown.cmd.api.MyTownCommand;
import mytown.entities.Resident;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.sperion.forgeperms.ForgePerms;

public class CmdChannel extends CommandBase implements MyTownCommand {
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

			if (!ForgePerms.getPermissionManager().canAccess(res.onlinePlayer.username, res.onlinePlayer.worldObj.provider.getDimensionName(), "mytown.chat.focus." + ch.name.toLowerCase())) {
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

	
	@Override
	public List<String> dumpCommands() {
		return null;
	}
	

	@Override
	public String getPermNode() {
		return "mytown.chat";
	}

}
