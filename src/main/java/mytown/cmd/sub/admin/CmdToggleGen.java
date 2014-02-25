package mytown.cmd.sub.admin;

import java.util.List;

import mytown.MyTown;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.event.tick.WorldBorder;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CmdToggleGen extends MyTownSubCommandAdapter {

	@Override
	public String getName() {
		return "togglegen";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.togglegen";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		WorldBorder.instance.genenabled = !WorldBorder.instance.genenabled;
		MyTown.instance.config.get("worldborder", "chunk-generator-enabled", WorldBorder.instance.genenabled, "Generate blocks?").set(WorldBorder.instance.genenabled);
		MyTown.instance.config.save();
		MyTown.sendChatToPlayer(sender, String.format("§aWorld gen is now %s", WorldBorder.instance.genenabled ? "§2ENABLED" : "§4DISABLED"));
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}