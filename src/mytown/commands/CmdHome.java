package mytown.commands;

import java.util.logging.Level;

import mytown.CommandException;
import mytown.Cost;
import mytown.Formatter;
import mytown.Log;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Term;
import mytown.entities.PayHandler;
import mytown.entities.Resident;
import mytown.entities.SavedHome;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.sperion.forgeperms.ForgePerms;

public class CmdHome extends CommandBase {
    @Override
    public String getCommandName() {
        return "home";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionManager().canAccess(p.getCommandSenderName(), p.worldObj.provider.getDimensionName(), "mytown.ecmd.home");
        }
        return false;
    }

    @Override
    public void processCommand(ICommandSender cs, String[] args) {
        EntityPlayerMP pl = (EntityPlayerMP) cs;
        Resident res = MyTownDatasource.instance.getOrMakeResident(pl);

        try {
            SavedHome h = res.home.get(args.length == 0 ? null : args[0]);

            if (!res.home.hasHomes()) {
                throw new CommandException(Term.HomeCmdNoHomes);
            }

            if (h == null) {
                throw new CommandException(Term.HomeCmdNoHomeByName);
            }

            res.pay.requestPayment("hometeleport", Cost.HomeTeleport.item, new PayHandler.IDone() {
                @Override
                public void run(Resident player, Object[] args) {
                    teleport(player, (SavedHome) args[0]);
                }
            }, h);

        } catch (CommandException ex) {
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.WARNING, ex.errorCode.toString(ex.args)));
        } catch (Throwable ex) {
            Log.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.SEVERE, ex.toString()));
        }
    }

    public static void teleport(Resident res, SavedHome h) {
        if (Cost.HomeTeleport.item != null && Resident.teleportToHomeWaitSeconds > 0) {
            MyTown.sendChatToPlayer(res.onlinePlayer, Term.HomeCmdDontMove.toString());
        }

        res.asyncStartSpawnTeleport(h);
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        // TODO Auto-generated method stub
        return null;
    }
}
