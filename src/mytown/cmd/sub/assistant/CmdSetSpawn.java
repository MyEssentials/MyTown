package mytown.cmd.sub.assistant;

import java.util.List;

import mytown.CommandException;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

public class CmdSetSpawn extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "setspawn";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.setspawn";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		TownBlock b = MyTownDatasource.instance.getBlock(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);

		if (b == null || b.town() == null) {
			throw new CommandException(Term.ErrPermPlotNotInTown);
		}
		if (b.town() != res.town()) {
			throw new CommandException(Term.ErrPermPlotNotInYourTown);
		}

		Vec3 vec = Vec3.createVectorHelper(res.onlinePlayer.posX, res.onlinePlayer.posY, res.onlinePlayer.posZ);
		res.town().setSpawn(b, vec, res.onlinePlayer.rotationPitch, res.onlinePlayer.rotationYaw);

		MyTown.sendChatToPlayer(sender, Term.TownSpawnSet.toString());
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}
}