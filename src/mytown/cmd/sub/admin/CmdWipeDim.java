package mytown.cmd.sub.admin;

import java.util.List;

import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CmdWipeDim extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "wipedim";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.wipedim";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1) {
			MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdWipeDim.toString(), Term.TownadmCmdWipeDimArgs.toString(), Term.TownadmCmdWipeDimDesc.toString(), null));
		} else if (args.length != 2 || !args[1].equalsIgnoreCase("ok")) {
			MyTown.sendChatToPlayer(sender, "Add ' ok' to the end of the command if you are absolutely sure. §4There is no going back.");
		} else {
			int dim = Integer.parseInt(args[0]);
			int i = MyTownDatasource.instance.deleteAllTownBlocksInDimension(dim);
			MyTown.sendChatToPlayer(sender, String.format("§2Done. Deleted %s town blocks", i));
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

}
