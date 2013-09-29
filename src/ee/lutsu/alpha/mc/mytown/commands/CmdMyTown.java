package ee.lutsu.alpha.mc.mytown.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.sperion.forgeperms.ForgePerms;

import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.NoAccessException;
//import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;

public class CmdMyTown extends CommandBase {
    @Override
    public String getCommandName() {
        return Term.TownCommand.toString();
    }

    @Override
    public List getCommandAliases() {
        return Arrays.asList(Term.TownCommandAliases.toString().split(" "));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionsHandler().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.cmd");
        }
        return false;
        // return MyTown.instance.perms.canAccess(cs, "mytown.cmd");
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "/" + getCommandName();
    }

    @Override
    public void processCommand(ICommandSender cs, String[] var2) {
        boolean handled = false;
        try {
            // all
            handled = MyTownEveryone.handleCommand(cs, var2) || handled;

            // in town
            handled = MyTownResident.handleCommand(cs, var2) || handled;
            handled = MyTownAssistant.handleCommand(cs, var2) || handled;
            handled = MyTownMayor.handleCommand(cs, var2) || handled;

            // not in town
            handled = MyTownNonResident.handleCommand(cs, var2) || handled;

            // all - nations
            handled = MyTownNation.handleCommand(cs, var2) || handled;

            if (!handled) {
                throw new CommandException(Term.ErrUnknowCommand);
            }
        } catch (NoAccessException ex) {
            MyTown.sendChatToPlayer(cs, ex.toString());
        } catch (NumberFormatException ex) {
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.WARNING, Term.TownErrCmdNumberFormatException.toString()));
        } catch (CommandException ex) {
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.WARNING, ex.errorCode.toString(ex.args)));
        } catch (Throwable ex) {
            Log.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.SEVERE, ex.toString()));
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab
     * completion options.
     */
    @Override
    public List addTabCompletionOptions(ICommandSender cs, String[] args) {
        if (args.length < 1) {
            return null;
        }

        List<String> ret = new ArrayList<String>();
        List<String> list = null;

        try {
            list = MyTownEveryone.getAutoComplete(cs, args);
            if (list != null) {
                ret.addAll(list);
            }

            list = MyTownResident.getAutoComplete(cs, args);
            if (list != null) {
                ret.addAll(list);
            }

            list = MyTownAssistant.getAutoComplete(cs, args);
            if (list != null) {
                ret.addAll(list);
            }

            list = MyTownMayor.getAutoComplete(cs, args);
            if (list != null) {
                ret.addAll(list);
            }

            list = MyTownNonResident.getAutoComplete(cs, args);
            if (list != null) {
                ret.addAll(list);
            }

            list = MyTownNation.getAutoComplete(cs, args);
            if (list != null) {
                ret.addAll(list);
            }
        } catch (Throwable ex) {
            Log.log(Level.WARNING, String.format("Command execution error by %s", cs), ex);
            MyTown.sendChatToPlayer(cs, Formatter.commandError(Level.SEVERE, ex.toString()));
        }

        if (ret.size() > 0) {
            return CommandBase.getListOfStringsFromIterableMatchingLastWord(args, ret);
        }

        return null;
    }
}
