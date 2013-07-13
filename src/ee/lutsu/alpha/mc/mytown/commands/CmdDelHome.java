package ee.lutsu.alpha.mc.mytown.commands;

import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.sperion.forgeperms.ForgePerms;

import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
//import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;

public class CmdDelHome extends CommandBase {
    @Override
    public String getCommandName() {
        return "delhome";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionsHandler().canAccess(p.username,
                    p.worldObj.provider.getDimensionName(),
                    "mytown.ecmd.delhome");
        }
        return false;
    }

    @Override
    public void processCommand(ICommandSender cs, String[] args) {
        EntityPlayerMP pl = (EntityPlayerMP) cs;
        Resident res = MyTownDatasource.instance.getOrMakeResident(pl);

        try {
            if (!res.home.hasHomes()) {
                throw new CommandException(Term.HomeCmdNoHomes);
            }

            res.home.delete(args.length == 0 ? null : args[0]);
            MyTown.sendChatToPlayer(cs, args.length == 0 ? Term.HomeCmdHomeDeleted.toString() : Term.HomeCmdHome2Deleted.toString(args[0]));
        } catch (CommandException ex) {
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.WARNING, ex.errorCode.toString(ex.args)));
        } catch (Throwable ex) {
            Log.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.SEVERE, ex.toString()));
        }
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        // TODO Auto-generated method stub
        return null;
    }
}
