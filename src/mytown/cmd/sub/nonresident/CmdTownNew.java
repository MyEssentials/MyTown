package mytown.cmd.sub.nonresident;

import java.util.List;

import mytown.Assert;
import mytown.Cost;
import mytown.Formatter;
import mytown.Log;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.PayHandler;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CmdTownNew extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "new";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.new.dim";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException, NoAccessException {
		if (MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender).town() != null)
			return;
		super.canUse(sender);
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Assert.Perm(sender, "mytown.cmd.new.dim" + res.onlinePlayer.dimension);

		if (args.length < 1 || args.length > 1) {
			MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdNew.toString(), Term.TownCmdNewArgs.toString(), Term.TownCmdNewDesc.toString(), null));
		} else {
			TownBlock home = MyTownDatasource.instance.getOrMakeBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
			try {
				Town.assertNewTownParams(args[0], res, home);
			} catch (CommandException e) {
				if (home != null && home.town() == null) {
					MyTownDatasource.instance.unloadBlock(home);
				}

				throw e;
			}

			res.pay.requestPayment("townnew", Cost.TownNew.item, new PayHandler.IDone() {
				@Override
				public void run(Resident res, Object[] ar2) {
					String[] args = (String[]) ar2[0];

					Town t = null;
					try { // should never crash because we're doing the same
							// checks before
						t = new Town(args[0], res, (TownBlock) ar2[1]);
					} catch (CommandException e) {
						Log.severe("Town creating failed after taking payment", e);
					}

					// emulate that the player just entered it
					res.checkLocation();

					String msg = Term.TownBroadcastCreated.toString(res.name(), t.name());
					for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
						MyTown.sendChatToPlayer((EntityPlayer) obj, msg);
					}

					t.sendTownInfo(res.onlinePlayer);
				}
			}, args, home);
		}
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}