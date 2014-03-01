package mytown.cmd.sub.admin;

import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.SavedHome;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;

/**
 * Sets the given users home to the location of the player, or the given location
 * /ta sethome player home [dim x y z]
 * @author Joe Goett
 */
public class CmdSetHome extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "sethome";
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.sethome";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2){
			throw new CommandException("Wrong number of arguments");
		}
		Resident res = MyTownDatasource.instance.getResident(args[0]);
		if (res == null){
			throw new CommandException(Term.TownErrPlayerNotFound.toString());
		}
		SavedHome home = res.home.get(args[1]);
		if (home == null){
			throw new CommandException(Term.HomeCmdNoHomeByName.toString());
		}
		int dim;
		double x, y, z;
		
		if (args.length == 6){
			dim = Integer.parseInt(args[2]);
			x = Integer.parseInt(args[3]);
			y = Integer.parseInt(args[4]);
			z = Integer.parseInt(args[5]);
		} else{
			if (!(sender instanceof EntityPlayer)){
				throw new CommandException("Must give dim, x, y, and z");
			}
			dim = sender.getEntityWorld().provider.dimensionId;
			ChunkCoordinates c = sender.getPlayerCoordinates();
			x = c.posX;
			y = c.posY;
			z = c.posZ;
		}
		
		home.dim = dim;
		home.x = x;
		home.y = y;
		home.z = z;
	}
}