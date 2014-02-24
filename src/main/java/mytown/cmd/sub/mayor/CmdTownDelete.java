package mytown.cmd.sub.mayor;

import java.util.List;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CmdTownDelete extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "delete";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.delete";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (res.town() == null)
			throw new CommandException(Term.ChatErrNotInTown.toString());
		if (res.rank().compareTo(Rank.Mayor) <= 0)
			throw new CommandException(Term.ErrPermRankNotEnough.toString());
	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (args.length == 1 && args[0].equalsIgnoreCase("ok")) {
			if (res.town() == null) {
				throw new CommandException(Term.ErrNotInTown.toString());
			}
			String name = res.town().name();
			res.town().deleteTown();

			// emulate that the player just entered it
			res.checkLocation();

			String msg = Term.TownBroadcastDeleted.toString(name);
			for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
				MyTown.sendChatToPlayer((EntityPlayer) obj, msg);
			}
		} else {
			MyTown.sendChatToPlayer(sender, Term.TownCmdDeleteAction.toString());
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdDeleteDesc.toString();
	}
}
