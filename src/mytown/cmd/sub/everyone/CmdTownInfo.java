package mytown.cmd.sub.everyone;

import java.util.ArrayList;
import java.util.List;

import mytown.Assert;
import mytown.CommandException;
import mytown.Formatter;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.cmd.api.MyTownSubCommand;
import mytown.entities.Town;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;

public class CmdTownInfo implements MyTownSubCommand {
	@Override
	public String getName() {
		return "info";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.info";
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException, NoAccessException {
        if (sender instanceof MinecraftServer || sender instanceof RConConsoleSource) return;
        Assert.Perm(sender, getPermNode());
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1) {
            Town t = MyTownDatasource.instance.getTown(args[0]);
            if (t == null) {
                throw new CommandException(Term.TownErrNotFound, args[0]);
            }

            t.sendTownInfo(sender);
        } else if (args.length < 1 && !(sender instanceof EntityPlayer)){
        	MyTown.sendChatToPlayer(sender, "Please include a town name");
        	return;
        } else {
            MyTown.sendChatToPlayer(sender, Formatter.formatCommand(Term.TownCmdInfo.toString(), Term.TownCmdInfoArgs.toString(), Term.TownCmdInfoDesc.toString(), "f"));
        }
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<String>();
        list.clear();
        for (Town t : MyTownDatasource.instance.towns.values()) {
            list.add(t.name());
        }
		return list;
	}
}