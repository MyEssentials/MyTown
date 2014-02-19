package mytown.cmd.sub.resident;

import java.util.List;

import mytown.Assert;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.Resident.Rank;
import mytown.entities.TownBlock;
import mytown.entities.TownSettingCollection;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 * The /mytown perm town|res|plot [force [key]|set key [val]] command Shows,
 * sets or forces the permissions
 * 
 * @author Joe Goett
 */
public class CmdTownPerm extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "perm";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.perm";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (args.length < 1) {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), null));
			return;
		}

		String node = args[0];
		if (!node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) && !node.equalsIgnoreCase(Term.TownCmdPermArgsResident.toString()) && !node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString())) {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), null));
		}

		if (args.length < 2) { // show
			Assert.Perm(sender, "mytown.cmd.perm.show." + node);
			showPermissions(sender, res, node);
		} else {
			String action = args[1];
			if (action.equalsIgnoreCase(Term.TownCmdPermArgs2Set.toString()) && args.length > 2) { // Set
				Assert.Perm(sender, "mytown.cmd.perm.set." + node + "." + args[2]);

				setPermissions(sender, res, node, args[2], args.length > 3 ? args[3] : null);
			} else if (action.equalsIgnoreCase(Term.TownCmdPermArgs2Force.toString())) { // Force
				Assert.Perm(sender, "mytown.cmd.perm.force." + node + "." + (args.length > 2 ? args[2] : "all"));

				flushPermissions(sender, res, node, args.length > 2 ? args[2] : null);
			} else {
				MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdPerm.toString(), Term.TownCmdPermArgs.toString(), Term.TownCmdPermDesc.toString(), null));
			}
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	private static TownSettingCollection getPermNode(String node, Resident res) throws CommandException {
		TownSettingCollection set = null;
		if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString())) {
			if (res.town() == null) {
				throw new CommandException(Term.ErrPermYouDontHaveTown.toString());
			}

			set = res.town().settings;
		} else if (node.equalsIgnoreCase(Term.TownCmdPermArgsResident.toString())) {
			set = res.settings;
		} else if (node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString())) {
			TownBlock block = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
			if (block == null || block.town() == null) {
				throw new CommandException(Term.ErrPermPlotNotInTown.toString());
			}

			if (block.town() != res.town()) {
				throw new CommandException(Term.ErrPermPlotNotInYourTown.toString());
			}

			set = block.settings;
		} else {
			throw new CommandException(Term.ErrPermSettingCollectionNotFound.toString(), node);
		}

		return set;
	}

	private static void showPermissions(ICommandSender sender, Resident res, String node) throws CommandException {
		TownSettingCollection set = getPermNode(node, res);

		String title = "";
		if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString())) {
			title = "your town '" + res.town().name() + "' (default for residents)";
		} else if (node.equalsIgnoreCase(Term.TownCmdPermArgsResident.toString())) {
			title = "you '" + res.name() + "'";
		} else if (node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString())) {
			TownBlock block = (TownBlock) set.tag;
			title = String.format("the plot @ dim %s, %s,%s owned by '%s'", block.worldDimension(), block.x(), block.z(), block.ownerDisplay());
		}

		set.show(sender, title, node, false);
	}

	private static void flushPermissions(ICommandSender sender, Resident res, String node, String perm) throws CommandException {
		TownSettingCollection set = getPermNode(node, res);

		if (set.childs.size() < 1) {
			throw new CommandException(Term.ErrPermNoChilds.toString());
		}

		if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) && res.rank() == Rank.Resident) {
			throw new CommandException(Term.ErrPermRankNotEnough.toString());
		}

		set.forceChildsToInherit(perm);
		MyTown.sendChatToPlayer(sender, Term.PermForced.toString(node, perm == null || perm.equals("") ? "all" : perm));
	}

	private static void setPermissions(ICommandSender sender, Resident res, String node, String key, String val) throws CommandException {
		TownSettingCollection set = getPermNode(node, res);

		if (node.equalsIgnoreCase(Term.TownCmdPermArgsTown.toString()) && res.rank() == Rank.Resident) {
			throw new CommandException(Term.ErrPermRankNotEnough.toString());
		}

		if (node.equalsIgnoreCase(Term.TownCmdPermArgsPlot.toString()) && res.rank() == Rank.Resident) {
			TownBlock b = (TownBlock) set.tag;
			if (b.owner() != res) {
				throw new CommandException(Term.ErrPermRankNotEnough.toString());
			}
		}

		set.setValue(key, val);

		showPermissions(sender, res, node);
		MyTown.sendChatToPlayer(sender, Term.PermSetDone.toString(key, node));
	}
	
	@Override
	public String getDesc(ICommandSender sender) {
		return Term.TownCmdPermDesc.toString();
	}

	@Override
	public String getArgs(ICommandSender sender) {
		return Term.TownCmdPermArgs.toString();
	}
}
