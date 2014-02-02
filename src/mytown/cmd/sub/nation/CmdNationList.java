package mytown.cmd.sub.nation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mytown.CommandException;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Nation;
import net.minecraft.command.ICommandSender;

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
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		ArrayList<Nation> sorted = new ArrayList<Nation>(MyTownDatasource.instance.nations.values());

		Collections.sort(sorted, new Comparator<Nation>() {
			@Override
			public int compare(Nation arg0, Nation arg1) {
				return Integer.compare(arg1.towns().size(), arg0.towns().size());
			}
		});

		StringBuilder sb = new StringBuilder();
		sb.append(Term.TownCmdNationListStart.toString(sorted.size(), ""));
		int i = 0;

		for (Nation e : sorted) {
			String n = Term.TownCmdNationListEntry.toString(e.name(), e.towns().size());
			if (i > 0) {
				sb.append(", ");
			}
			i++;
			sb.append(n);
		}

		if (sb.length() > 0) {
			MyTown.sendChatToPlayer(sender, sb.toString());
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

}
