package mytown.cmd.sub.admin;

import mytown.*;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.entities.TownSettingCollection;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class CmdPerm extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "perm";
	}

	@Override
	public String getPermNode() {
		return null;
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1) {
			MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdPerm.toString(), Term.TownadmCmdPermArgs.toString(), Term.TownadmCmdPermDesc.toString(), null));
			return;
		}

		String node = args[0];
		if (!node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) && !node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString()) && !node.equalsIgnoreCase(Term.TownadmCmdPermArgsServer.toString()) && !node.equalsIgnoreCase(Term.TownadmCmdPermArgsWild.toString())
				&& !node.toLowerCase().startsWith(Term.TownadmCmdPermArgsWild2.toString().toLowerCase())) {
			MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdPerm.toString(), Term.TownadmCmdPermArgs.toString(), Term.TownadmCmdPermDesc.toString(), null));
			return;
		}

		if (args.length < 2) { // Show
			Assert.Perm(sender, "mytown.adm.cmd.perm.show." + node);
			showPermissions(sender, node);
		} else {
			String action = args[1];
			if (action.equalsIgnoreCase(Term.TownadmCmdPermArgs2Set.toString()) && args.length > 2) {
				Assert.Perm(sender, "mytown.adm.cmd.perm.set." + node);
				setPermissions(sender, node, args[2], args.length > 3 ? args[3] : null);
			} else if (action.equalsIgnoreCase(Term.TownadmCmdPermArgs2Force.toString())) {
				Assert.Perm(sender, "mytown.adm.cmd.perm.force." + node);
				flushPermissions(sender, node, args.length > 2 ? args[2] : null);
			} else {
				MyTown.sendChatToPlayer(sender, Formatter.formatAdminCommand(Term.TownadmCmdPerm.toString(), Term.TownadmCmdPermArgs.toString(), Term.TownadmCmdPermDesc.toString(), null));
			}
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	private TownSettingCollection getPermNode(ICommandSender cs, String node) throws CommandException {
		Resident res = cs instanceof EntityPlayer ? MyTownDatasource.instance.getOrMakeResident((EntityPlayer) cs) : null;
		TownSettingCollection set = null;
		if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString())) {
			if (res == null) {
				throw new CommandException(Term.ErrNotUsableByConsole.toString());
			}

			TownBlock block = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
			if (block == null || block.town() == null) {
				throw new CommandException(Term.ErrPermPlotNotInTown.toString());
			}

			set = block.town().settings;
		} else if (node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString())) {
			if (res == null) {
				throw new CommandException(Term.ErrNotUsableByConsole.toString());
			}

			TownBlock block = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
			if (block == null || block.town() == null) {
				throw new CommandException(Term.ErrPermPlotNotInTown.toString());
			}

			set = block.settings;
		} else if (node.equalsIgnoreCase(Term.TownadmCmdPermArgsServer.toString())) {
			set = MyTown.instance.serverSettings;
		} else if (node.equalsIgnoreCase(Term.TownadmCmdPermArgsWild.toString())) {
			set = MyTown.instance.serverWildSettings;
		} else if (node.toLowerCase().startsWith(Term.TownadmCmdPermArgsWild2.toString().toLowerCase())) {
			int dim = Integer.parseInt(node.substring(Term.TownadmCmdPermArgsWild2.toString().length()));
			set = MyTown.instance.getWorldWildSettings(dim);
		} else {
			throw new CommandException(Term.ErrPermSettingCollectionNotFound.toString(), node);
		}

		return set;
	}

	private void showPermissions(ICommandSender sender, String node) throws CommandException {
		TownSettingCollection set = getPermNode(sender, node);
		String title = "";
		if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString())) {
			title = "town '" + ((Town) set.tag).name() + "' (default for residents)";
		} else if (node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString())) {
			TownBlock block = (TownBlock) set.tag;
			title = String.format("the plot @ dim %s, %s,%s owned by '%s'", block.worldDimension(), block.x(), block.z(), block.ownerDisplay());
		} else if (node.equalsIgnoreCase(Term.TownadmCmdPermArgsServer.toString())) {
			title = "the server (default for towns)";
		} else if (node.equalsIgnoreCase(Term.TownadmCmdPermArgsWild.toString())) {
			title = "the wild (default for world wilds)";
		} else if (node.toLowerCase().startsWith(Term.TownadmCmdPermArgsWild2.toString().toLowerCase())) {
			String dim = node.substring(Term.TownadmCmdPermArgsWild2.toString().length());
			title = "the wild in dimension " + dim;
		}

		set.show(sender, title, node, true);
	}

	private void flushPermissions(ICommandSender sender, String node, String perm) throws CommandException {
		TownSettingCollection set = getPermNode(sender, node);
		if (set.childs.size() < 1) {
			throw new CommandException(Term.ErrPermNoChilds.toString());
		}

		set.forceChildsToInherit(perm);
		MyTown.sendChatToPlayer(sender, Term.PermForced.toString(node, perm == null || perm.equals("") ? "all" : perm));
	}

	private void setPermissions(ICommandSender sender, String node, String key, String val) throws CommandException {
		TownSettingCollection set = getPermNode(sender, node);

		set.setValue(key, val);

		showPermissions(sender, node);
		MyTown.sendChatToPlayer(sender, Term.PermSetDone.toString(key, node));
	}

	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownadmCmdPermDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownadmCmdPermArgs.toString();
	}
}
