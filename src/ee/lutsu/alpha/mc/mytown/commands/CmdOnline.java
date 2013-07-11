package ee.lutsu.alpha.mc.mytown.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import com.sperion.forgeperms.ForgePerms;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
//import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

public class CmdOnline extends CommandBase {
    @Override
    public String getCommandName() {
        return Term.OnlineCommand.toString();
    }

    @Override
    public List getCommandAliases() {
        return Arrays.asList(Term.OnlineCommandAliases.toString().split(" "));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) sender;
            return ForgePerms.getPermissionsHandler().canAccess(p.username,
                    p.worldObj.provider.getDimensionName(),
                    "mytown.ecmd.online");
        }
        Log.log(Level.INFO, "%s failed to use node %s",
                sender.getCommandSenderName(), "mytown.ecmd.online");
        return false;
        // return MyTown.instance.perms.canAccess(par1ICommandSender,
        // "mytown.ecmd.online");
    }

    @Override
    public void processCommand(ICommandSender cs, String[] args) {
        ArrayList<Resident> sorted = new ArrayList<Resident>(
                MyTownDatasource.instance.getOnlineResidents());

        Collections.sort(sorted, new Comparator<Resident>() {
            @Override
            public int compare(Resident arg0, Resident arg1) {
                return arg0.name().compareToIgnoreCase(arg1.name());
            }
        });

        StringBuilder sb = new StringBuilder();
        sb.append(Term.OnlineCmdListStart.toString(sorted.size(), ""));
        int i = 0;

        for (Resident e : sorted) {
            String n = e.formattedName();
            if (i > 0) {
                sb.append("§f, ");
            }

            i++;
            sb.append(n);
        }

        cs.sendChatToPlayer(sb.toString());
    }
}
