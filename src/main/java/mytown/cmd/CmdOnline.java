package mytown.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownCommandBase;
import mytown.entities.Resident;
import net.minecraft.command.ICommandSender;

public class CmdOnline extends MyTownCommandBase {
	@Override
	public String getCommandName() {
		return Term.OnlineCommand.toString();
	}

	@Override
	public List<?> getCommandAliases() {
		return Arrays.asList(Term.OnlineCommandAliases.toString().split(" "));
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) {
		canCommandSenderUseCommand(cs);
		ArrayList<Resident> sorted = new ArrayList<Resident>(MyTownDatasource.instance.getOnlineResidents());

		Collections.sort(sorted, new Comparator<Resident>() {
			@Override
			public int compare(Resident arg0, Resident arg1) {
				return arg0.name().compareToIgnoreCase(arg1.name());
			}
		});

		StringBuilder sb = new StringBuilder();
		sb.append(Term.OnlineCmdListStart.toString(sorted.size(), ""));
		int i = 0;

		for (Resident e : sorted) {
			String n = e.formattedName();
			if (i > 0) {
				sb.append("§f, ");
			}

			i++;
			sb.append(n);
		}

		MyTown.sendChatToPlayer(cs, sb.toString());
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return null;
	}

	@Override
	public List<String> dumpCommands() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.ecmd.online";
	}
}
