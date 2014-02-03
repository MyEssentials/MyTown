package mytown.cmd.sub.everyone;

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
import mytown.entities.Town;
import net.minecraft.command.ICommandSender;

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
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		ArrayList<Town> sorted = new ArrayList<Town>(MyTownDatasource.instance.towns.values());

		Collections.sort(sorted, new Comparator<Town>() {
			@Override
			public int compare(Town arg0, Town arg1) {
				return Integer.compare(arg1.residents().size(), arg0.residents().size());
			}
		});

		StringBuilder sb = new StringBuilder();
		sb.append(Term.TownCmdListStart.toString(sorted.size(), ""));
		int i = 0;

		for (Town e : sorted) {
			String n = Term.TownCmdListEntry.toString(e.name(), e.residents().size());
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