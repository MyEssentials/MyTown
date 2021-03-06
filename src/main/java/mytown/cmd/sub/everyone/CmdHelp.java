package mytown.cmd.sub.everyone;

import java.util.List;

import mytown.Formatter;
import mytown.MyTown;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommand;
import net.minecraft.command.ICommandSender;

public class CmdHelp implements MyTownSubCommand {
	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String getPermNode() {
		return "";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}

	@Override
	public void canUse(ICommandSender sender) {
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdMap.toString(), Term.TownCmdMapArgs.toString(), Term.TownCmdMapDesc.toString(), null));
		MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdInfo.toString(), Term.TownCmdInfoArgs.toString(), Term.TownCmdInfoDesc.toString(), null));
		MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdList.toString(), "", Term.TownCmdListDesc.toString(), null));
		MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdRes.toString(), Term.TownCmdResArgs.toString(), Term.TownCmdResDesc.toString(), null));
		MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdFriend.toString(), Term.TownCmdFriendArgs.toString(), Term.TownCmdFriendDesc.toString(), null));
		MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdSpawn.toString(), Term.TownCmdSpawnArgs.toString(), Term.TownCmdSpawnDesc.toString(), null));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return null;
	}

	@Override
	public String getDesc(ICommandSender sender) {
		return null;
	}
}