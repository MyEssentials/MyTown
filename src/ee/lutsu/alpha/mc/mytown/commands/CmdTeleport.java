package ee.lutsu.alpha.mc.mytown.commands;

import net.minecraft.command.CommandServerTp;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.sperion.forgeperms.ForgePerms;

public class CmdTeleport extends CommandServerTp {
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender cs) {
        if (cs instanceof EntityPlayerMP) {
            EntityPlayerMP p = (EntityPlayerMP) cs;
            return ForgePerms.getPermissionManager().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.adm.cmd.tp");
        }
        return false;
    }

    @Override
    public String getCommandUsage(ICommandSender par1iCommandSender) {
        return "/tp [player] <toplayer> | /tp [player] [dim] <x> <y> <z>";
    }

    @Override
    public void processCommand(ICommandSender cs, String[] arg) {
        if (arg.length < 1) {
            throw new WrongUsageException("/tp [player] <toplayer> | /tp [player] [dim] <x> <y> <z>");
        } else {
            EntityPlayerMP self;

            // /tp [self] <target player>
            // /tp [self] [dim] <x> <y> <z>

            // 1 name
            // 2 name name
            // 3 x y z
            // 4 dim x y z
            // 5 name dim z y z
            //
            if (arg.length != 2 && arg.length != 5) {
                self = getCommandSenderAsPlayer(cs);
            } else {
                self = func_82359_c(cs, arg[0]);
                if (self == null) {
                    throw new PlayerNotFoundException();
                }
            }

            if (arg.length == 1 || arg.length == 2) {
                EntityPlayerMP targetPlayer = func_82359_c(cs, arg[arg.length - 1]);

                if (targetPlayer == null) {
                    throw new PlayerNotFoundException();
                }

                if (targetPlayer.worldObj != self.worldObj) {
                    MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(self, targetPlayer.dimension);
                }

                self.playerNetServerHandler.setPlayerLocation(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, targetPlayer.rotationYaw, targetPlayer.rotationPitch);
                notifyAdmins(cs, "commands.tp.success", new Object[] { self.getEntityName(), targetPlayer.getEntityName() });
            } else if (self.worldObj != null) {
                int dim = arg.length > 3 ? Integer.parseInt(arg[arg.length - 4]) : self.dimension;

                int var4 = arg.length - 3;
                double var5 = func_82368_a(cs, self.posX, arg[var4++]);
                double var7 = func_82367_a(cs, self.posY, arg[var4++], 0, 0);
                double var9 = func_82368_a(cs, self.posZ, arg[var4++]);

                if (self.dimension != dim) {
                    MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(self, dim);
                }

                self.playerNetServerHandler.setPlayerLocation(var5, var7, var9, self.rotationYaw, self.rotationPitch);
                notifyAdmins(cs, "commands.tp.success.coordinates", new Object[] { self.getEntityName(), Double.valueOf(var5), Double.valueOf(var7), Double.valueOf(var9) });
            }
        }
    }

    private double func_82368_a(ICommandSender par1ICommandSender, double par2, String par4Str) {
        return this.func_82367_a(par1ICommandSender, par2, par4Str, -30000000, 30000000);
    }

    private double func_82367_a(ICommandSender par1ICommandSender, double par2, String par4Str, int par5, int par6) {
        boolean var7 = par4Str.startsWith("~");
        double var8 = var7 ? par2 : 0.0D;

        if (!var7 || par4Str.length() > 1) {
            boolean var10 = par4Str.contains(".");

            if (var7) {
                par4Str = par4Str.substring(1);
            }

            var8 += parseDouble(par1ICommandSender, par4Str);

            if (!var10 && !var7) {
                var8 += 0.5D;
            }
        }

        if (par5 != 0 || par6 != 0) {
            if (var8 < par5) {
                throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[] { Double.valueOf(var8), Integer.valueOf(par5) });
            }

            if (var8 > par6) {
                throw new NumberInvalidException("commands.generic.double.tooBig", new Object[] { Double.valueOf(var8), Integer.valueOf(par6) });
            }
        }

        return var8;
    }
}
