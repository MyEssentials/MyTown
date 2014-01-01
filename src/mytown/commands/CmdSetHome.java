package mytown.commands;

import java.util.logging.Level;

import mytown.CommandException;
import mytown.Cost;
import mytown.Formatter;
import mytown.Log;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.NoAccessException;
import mytown.Term;
import mytown.entities.PayHandler;
import mytown.entities.Resident;
import mytown.entities.SavedHome;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.sperion.forgeperms.ForgePerms;

//import ee.lutsu.alpha.mc.mytown.Permissions;

public class CmdSetHome extends CommandBase {
    @Override
    public String getCommandName() {
        return "sethome";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionManager().canAccess(p.getCommandSenderName(), p.worldObj.provider.getDimensionName(), "mytown.ecmd.sethome");
        }
        return false;
    }

    @Override
    public String getCommandUsage(ICommandSender cs) {
        return getCommandName() + " [name] - Sets a new home location";
    }

    @Override
    public void processCommand(ICommandSender cs, String[] args) {
        EntityPlayerMP pl = (EntityPlayerMP) cs;
        Resident res = MyTownDatasource.instance.getOrMakeResident(pl);

        try {
            if (!res.canInteract(pl.dimension, (int) pl.posX, (int) pl.posY, (int) pl.posZ, "build")) {
                throw new CommandException(Term.HomeCmdCannotSetHere);
            }

            res.home.assertSetHome(args.length == 0 ? null : args[0], pl);

            ItemStack request = null;
            SavedHome h = res.home.get(args.length == 0 ? null : args[0]);
            if (h == null) {
                if (Cost.HomeSetNew.item != null) {
                    request = Cost.HomeSetNew.item.copy();
                    request.stackSize += Cost.homeSetNewAdditional * res.home.size();
                }
            } else {
                if (Cost.HomeReplace.item != null) {
                    request = Cost.HomeReplace.item;
                }
            }

            res.pay.requestPayment(h == null ? "homenew" : "homereplace", request, new PayHandler.IDone() {
                @Override
                public void run(Resident res, Object[] args) {
                    setHome(res, (EntityPlayerMP) res.onlinePlayer, (String[]) args[0]);
                }
            }, (Object) args);
        } catch (NoAccessException ex) {
            MyTown.sendChatToPlayer(cs, ex.toString());
        } catch (CommandException ex) {
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.WARNING, ex.errorCode.toString(ex.args)));
        } catch (Throwable ex) {
            Log.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.SEVERE, ex.toString()));
        }
    }

    public static void setHome(Resident res, EntityPlayerMP pl, String[] args) {
        res.home.set(args.length == 0 ? null : args[0], pl);
        MyTown.sendChatToPlayer(pl, args.length == 0 ? Term.HomeCmdHomeSet.toString() : Term.HomeCmdHome2Set.toString(args[0]));
    }
}
