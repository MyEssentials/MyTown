package mytown.cmd;

import java.util.List;

import mytown.Assert;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.cmd.api.MyTownCommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CmdNick extends MyTownCommandBase {

	@Override
	public List<String> dumpCommands() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.ecmd.nick";
	}

	@Override
	public String getCommandName() {
		return "nick";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/" + getCommandName();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		EntityPlayer pl = null;
		String username = "";
		String toNick = null;
		if (args.length == 0){  // Reset Nick to null
		} else if (args.length == 1){  // Change own nick
			username = sender.getCommandSenderName();
			pl = (EntityPlayer)sender;
			toNick = args[0];
		} else if (args.length == 2){  // Pass a username into nick (can be own)
			username = args[0];
			if (username.equals(sender.getCommandSenderName())){
				pl = (EntityPlayer)sender;
			} else{
				Assert.Perm(sender, "mytown.ecmd.nick.other");
				pl = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username);
			}
			toNick = args[1];
		}
		if (pl == null){ // Player doesn't exist
			MyTown.sendChatToPlayer(sender, String.format("Player %s doesn't exist", username));
			return;
		}
		MyTownDatasource.instance.getOrMakeResident(pl).setNick(toNick);
	}
}