package mytown.cmd.sub.admin;

import mytown.MyTownDatasource;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import mytown.entities.SavedHome;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * Allows admins to teleport to other players homes
 * /ta home [playername] [homename]
 * @author Joe Goett
 */
public class CmdHome extends MyTownSubCommandAdapter {
	@Override
	public String getName() {
		return "home";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.home";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		canUse(sender);
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
		if (args.length<1){
			throw new CommandException(Term.TownadmCmdHomeArgs.toString());
		}
		if (MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(args[0]) == null){
			throw new CommandException(Term.TownErrPlayerNotFound.toString());
		}
		Resident target = MyTownDatasource.instance.getOrMakeResident(args[0]);
		SavedHome targetHome;
		if (args.length > 1){
			targetHome = target.home.get(args[1]);
		} else{
			if (!target.isOnline() || target.onlinePlayer == null){
				throw new CommandException(Term.TownErrPlayerNotFoundOrOnline.toString());
			}
			targetHome = SavedHome.fromBed((EntityPlayerMP) target.onlinePlayer);
		}
		
		if (targetHome == null){
			throw new CommandException(Term.HomeCmdNoHomeByName.toString());
		}
		
		res.respawnPlayer(targetHome);
	}
}