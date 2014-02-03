package mytown.cmd.sub.everyone;

import java.util.ArrayList;
import java.util.List;

import mytown.Assert;
import mytown.Cost;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.PayHandler;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class CmdSpawn extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "spawn";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.spawn";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		Town target = null;
		if (args.length < 1) {
			if (res.town() == null) {
				throw new CommandException(Term.ErrPermYouDontHaveTown.toString());
			}

			target = res.town();
		} else {
			Town t = MyTownDatasource.instance.getTown(args[0]);
			if (t == null) {
				throw new CommandException(Term.TownErrNotFound.toString(), args[0]);
			}

			target = t;
		}
		if (target.spawnBlock == null || target.getSpawn() == null) {
			throw new CommandException(Term.TownErrSpawnNotSet.toString());
		}

		ItemStack cost = null;
		if (target == res.town()) {
			Assert.Perm(sender, "mytown.cmd.spawn.own");
			cost = Cost.TownSpawnTeleportOwn.item;
		} else {
			Assert.Perm(sender, "mytown.cmd.spawn.other");
			cost = Cost.TownSpawnTeleportOther.item;
		}

		res.pay.requestPayment(target == res.town() ? "townspawntpown" : "townspawntpother", cost, new PayHandler.IDone() {
			@Override
			public void run(Resident player, Object[] args) {
				player.sendToTownSpawn((Town) args[0]);
			}
		}, target);
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		for (Town t : MyTownDatasource.instance.towns.values()) {
			list.add(t.name());
		}
		return list;
	}
}