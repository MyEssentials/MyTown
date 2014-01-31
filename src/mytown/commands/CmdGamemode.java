package mytown.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.StatCollector;
import net.minecraft.world.EnumGameType;
import net.minecraft.world.WorldSettings;

import com.sperion.forgeperms.ForgePerms;

public class CmdGamemode extends CommandBase {
    @Override
    public String getCommandName() {
        return "gm";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionManager().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.gm");
        }
        return false;
    }

    @Override
    public void processCommand(ICommandSender cs, String[] args) {
        EntityPlayerMP pl = null;
        if (args.length > 1) {
            pl = getPlayer(cs, args[1]);
        } else {
            pl = getCommandSenderAsPlayer(cs);
        }

        EnumGameType mode = pl.theItemInWorldManager.getGameType() == EnumGameType.SURVIVAL ? EnumGameType.CREATIVE : EnumGameType.SURVIVAL;
        if (args.length > 0) {
            mode = getGameModeFromCommand(cs, args[0]);
        }

        pl.setGameType(mode);
        pl.fallDistance = 0.0F;
        String var5 = StatCollector.translateToLocal("gameMode." + mode.getName());

        if (pl != cs) {
            notifyAdmins(cs, 1, "commands.gamemode.success.other", new Object[] { pl.getEntityName(), var5 });
        } else {
            notifyAdmins(cs, 1, "commands.gamemode.success.self", new Object[] { var5 });
        }
    }

    /**
     * Gets the Game Mode specified in the command.
     */
    protected EnumGameType getGameModeFromCommand(ICommandSender par1ICommandSender, String par2Str) {
        return !par2Str.equalsIgnoreCase(EnumGameType.SURVIVAL.getName()) && !par2Str.equalsIgnoreCase("s") ? !par2Str.equalsIgnoreCase(EnumGameType.CREATIVE.getName()) && !par2Str.equalsIgnoreCase("c") ? !par2Str.equalsIgnoreCase(EnumGameType.ADVENTURE.getName()) && !par2Str.equalsIgnoreCase("a") ? WorldSettings
                .getGameTypeById(parseIntBounded(par1ICommandSender, par2Str, 0, EnumGameType.values().length - 2))
                : EnumGameType.ADVENTURE
                : EnumGameType.CREATIVE
                : EnumGameType.SURVIVAL;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        // TODO Auto-generated method stub
        return null;
    }
}
