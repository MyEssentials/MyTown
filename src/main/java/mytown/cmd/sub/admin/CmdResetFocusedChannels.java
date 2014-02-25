package mytown.cmd.sub.admin;

import java.util.List;

import mytown.ChatChannel;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CmdResetFocusedChannels extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "reschannels";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.reschannels";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		int i = 0;
		for (Resident r : MyTownDatasource.instance.residents.values()) {
			if (r.activeChannel != ChatChannel.defaultChannel) {
				i++;
				r.setActiveChannel(ChatChannel.defaultChannel);
			}
		}
		MyTown.sendChatToPlayer(sender, String.format("§2Done. Resetted %s player channels to %s", i, ChatChannel.defaultChannel.name));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownadmCmdResetFocusedChannelsDesc.toString();
	}
}