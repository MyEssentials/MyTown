package mytown.cmd.sub.nation;

import com.google.common.base.Joiner;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import net.minecraft.command.ICommandSender;

import java.util.List;

public class CmdNationList extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.nationlist";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		MyTown.sendChatToPlayer(sender, Joiner.on(", ").join(MyTownDatasource.instance.nations.keySet()));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdNationListDesc.toString();
	}
}
