package mytown.cmd.sub.everyone;

import java.util.List;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import net.minecraft.command.ICommandSender;

import com.google.common.base.Joiner;

public class CmdTownList extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.list";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		String townList = Joiner.on(", ").join(MyTownDatasource.instance.towns.values());
		MyTown.sendChatToPlayer(sender, Term.TownCmdListStart.toString(MyTownDatasource.instance.towns.size(), townList));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdListDesc.toString();
	}
}