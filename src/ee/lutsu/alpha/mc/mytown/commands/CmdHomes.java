package ee.lutsu.alpha.mc.mytown.commands;

import java.util.List;
import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sperion.forgeperms.ForgePerms;

import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
//import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.SavedHome;
import ee.lutsu.alpha.mc.mytown.entities.SavedHomeList;

public class CmdHomes extends CommandBase {
    @Override
    public String getCommandName() {
        return "homes";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.ecmd.homes");
        }
        return false;
    }

    @Override
    public String getCommandUsage(ICommandSender cs) {
        return getCommandName() + " [loc] - Shows the player homes [with location]";
    }

    @Override
    public void processCommand(ICommandSender cs, String[] args) {
        EntityPlayerMP pl = (EntityPlayerMP) cs;
        Resident res = MyTownDatasource.instance.getOrMakeResident(pl);

        try {
            if (!res.home.hasHomes()) {
                MyTown.sendChatToPlayer(cs, Term.HomeCmdNoHomes.toString());
            } else {
                if (args.length == 1 && args[0].equalsIgnoreCase("loc")) {
                    MyTown.sendChatToPlayer(cs, Term.HomeCmdHomesTitle.toString());
                    if (SavedHomeList.defaultIsBed && pl.getBedLocation() != null) {
                        SavedHome s = SavedHome.fromBed(pl);
                        MyTown.sendChatToPlayer(cs, Term.HomeCmdHomesUnaccessibleItem2.toString("default", s.dim, (int) s.x, (int) s.y, (int) s.z));
                    }

                    for (SavedHome h : res.home) {
                        MyTown.sendChatToPlayer(cs, Term.HomeCmdHomesItem2.toString(h.name, h.dim, (int) h.x, (int) h.y, (int) h.z));
                    }
                } else {
                    List<String> items = Lists.newArrayList();
                    if (SavedHomeList.defaultIsBed && pl.getBedLocation() != null) {
                        items.add(Term.HomeCmdHomesUnaccessibleItem.toString("default"));
                    }

                    for (SavedHome h : res.home) {
                        items.add(Term.HomeCmdHomesItem.toString(h.name));
                    }

                    MyTown.sendChatToPlayer(cs, Term.HomeCmdHomesTitle.toString(Joiner.on(", ").join(items)));
                }
            }
        } catch (Throwable ex) {
            Log.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.SEVERE, ex.toString()));
        }
    }
}
