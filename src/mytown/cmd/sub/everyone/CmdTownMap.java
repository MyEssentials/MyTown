package mytown.cmd.sub.everyone;

import java.util.ArrayList;
import java.util.List;

import mytown.CommandException;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommandAdapter;
import mytown.entities.Resident;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CmdTownMap extends MyTownSubCommandAdapter {
	private ArrayList<String> tabList;
	
	public CmdTownMap(){
		tabList = new ArrayList<String>();
		tabList.add("on");
		tabList.add("off");
	}
	
	@Override
	public String getName() {
		return "map";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.map";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException, NoAccessException {
        Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) sender);
        if (args.length == 1) {
            boolean modeOn = !res.mapMode;

            if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("activate")) {
                modeOn = true;
            } else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("deactivate")) {
                modeOn = false;
            }

            res.mapMode = modeOn;

            String msg = res.mapMode ? Term.PlayerMapModeOn.toString() : Term.PlayerMapModeOff.toString();
            MyTown.sendChatToPlayer(sender, msg);
        } else {
            res.sendLocationMap(res.onlinePlayer.dimension, res.onlinePlayer.chunkCoordX, res.onlinePlayer.chunkCoordZ);
        }
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return tabList;
	}
}