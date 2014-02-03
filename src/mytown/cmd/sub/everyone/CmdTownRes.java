package mytown.cmd.sub.everyone;

import java.util.List;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdTownRes extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "res";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.res";
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		if (args.length == 0 && sender instanceof EntityPlayer) {
			Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
			res.sendInfoTo(sender, res.shouldShowPlayerLocation());
		} else if (args.length == 1) {
			Resident r = MyTownDatasource.instance.getResident(args[0]);
			if (r == null) {
				MyTown.sendChatToPlayer(sender, Term.TownErrPlayerNotFound.toString());
			} else {
				r.sendInfoTo(sender, sender instanceof EntityPlayer ? MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender).shouldShowPlayerLocation() : true);
			}
		} else {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdRes.toString(), Term.TownCmdResArgs.toString(), Term.TownCmdResDesc.toString(), null));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}