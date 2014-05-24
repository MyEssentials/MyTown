package mytown.cmd;

import java.util.List;
import java.util.Map;

import forgeperms.api.ForgePermsAPI;
import mytown.Assert;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.cmd.api.MyTownCommand;
import net.minecraft.command.CommandServerMessage;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.StringTranslate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CmdPrivateMsg extends CommandServerMessage implements MyTownCommand {
	public static boolean snoop = true;
	public static Map<ICommandSender, ICommandSender> lastMessages = Maps.newHashMap();
	public static Map<ICommandSender, ICommandSender> chatLock = Maps.newHashMap();
	public static List<ICommandSender> snoopers = Lists.newArrayList();

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cs) {
		Assert.Perm(cs, getPermNode(), canConsoleUse());
		return true;
	}

	@Override
	public boolean canConsoleUse() {
		return true;
	}

	@Override
	public void processCommand(ICommandSender cs, String[] arg) {
		canCommandSenderUseCommand(cs);
		if (arg.length > 0) {
			EntityPlayerMP target = getPlayer(cs, arg[0]);

			if (target == null) {
				throw new PlayerNotFoundException();
			}

			if (arg.length > 1) { // send chat
				String msg = func_82361_a(cs, arg, 1, !(cs instanceof EntityPlayer));
				sendChat(cs, target, msg);
			} else { // lock mode
				lockChatWithNotify((EntityPlayer) cs, target);
			}
		} else if (chatLock.get(cs) != null) {
			stopLockChatWithNotify((EntityPlayer) cs);
		} else {
			MyTown.sendChatToPlayer(cs, "§4Usage /tell [target] [msg]");
		}
	}

	public static void stopLockChatWithNotify(ICommandSender sender) {
		if (sender != null) {
			MyTown.sendChatToPlayer(sender, "§dStopped chatting with " + MyTownDatasource.instance.getOrMakeResident(sender.getCommandSenderName()).formattedName());
		}
	}

	public static void lockChatWithNotify(ICommandSender sender, ICommandSender target) {
		chatLock.put(sender, target);
		MyTown.sendChatToPlayer(sender, "§dNow chatting with " + MyTownDatasource.instance.getOrMakeResident(target.getCommandSenderName()).formattedName());
	}

	public static void sendChat(ICommandSender sender, ICommandSender target, String msg) {
		if (sender == null || target == null || msg == null) {
			return;
		}

		lastMessages.put(target, sender);

		if (ForgePermsAPI.permManager.canAccess(sender.getCommandSenderName(), sender.getEntityWorld().provider.getDimensionName(), "mytown.chat.allowcolors")) {
			msg = Formatter.applyColorCodes(msg);
		}

		if (snoop) {
			MyTown.instance.chatLog.info("§7[%s §7-> %s§7] %s", MyTownDatasource.instance.getOrMakeResident(sender.getCommandSenderName()), MyTownDatasource.instance.getOrMakeResident(target.getCommandSenderName()), msg);
		}

		if (!Formatter.formatChat) {
			StringTranslate strTranslate = new StringTranslate();
			MyTown.sendChatToPlayer(sender, "\u00a77\u00a7o" + strTranslate.translateKeyFormat("commands.message.display.outgoing", new Object[] { target.getCommandSenderName(), msg }));
			MyTown.sendChatToPlayer(target, "\u00a77\u00a7o" + strTranslate.translateKeyFormat("commands.message.display.incoming", new Object[] { sender.getCommandSenderName(), msg }));
		} else {
			MyTown.sendChatToPlayer(sender, Formatter.formatPrivMsg(MyTownDatasource.instance.getOrMakeResident(sender.getCommandSenderName()), MyTownDatasource.instance.getOrMakeResident(target.getCommandSenderName()), msg, true));
			MyTown.sendChatToPlayer(target, Formatter.formatPrivMsg(MyTownDatasource.instance.getOrMakeResident(sender.getCommandSenderName()), MyTownDatasource.instance.getOrMakeResident(target.getCommandSenderName()), msg, false));
		}
	}

	@Override
	public List<String> dumpCommands() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.ecmd.msg";
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}
}
