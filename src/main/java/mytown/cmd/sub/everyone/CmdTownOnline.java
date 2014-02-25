package mytown.cmd.sub.everyone;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class CmdTownOnline extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "online";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.online";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		if (MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender).town() == null){
			throw new CommandException(Term.ErrNotInTown.toString());
		}
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Town t = res.town();

		StringBuilder sb = new StringBuilder();
		for (Resident r : t.residents()) {
			if (!r.isOnline()) {
				continue;
			}

			if (sb.length() > 0) {
				sb.append("§2, ");
			}

			sb.append(Formatter.formatResidentName(r));
		}

		MyTown.sendChatToPlayer(sender, Term.TownPlayersOnlineStart.toString(sb.toString()));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdOnlineDesc.toString();
	}
}