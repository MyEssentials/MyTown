package mytown.cmd;

import java.util.List;

import mytown.Log;
import mytown.MyTown;
import mytown.Term;
import mytown.cmd.api.MyTownCommandBase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class CmdSetSpawn extends MyTownCommandBase {
	@Override
	public String getCommandName() {
		return "setspawn";
	}

	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender) {
		return "/setspawn [dim] [<x> <y> <z>]";
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) {
		if (!canCommandSenderUseCommand(cs))
			throw new CommandException(Term.ErrCannotAccessCommand.toString());
		EntityPlayerMP pl = (EntityPlayerMP) cs;

		int dim = 0;
		if (args.length == 1 || args.length == 4) {
			dim = Integer.parseInt(args[0]);
		} else {
			dim = pl.dimension;
			if (dim != 0) {
				MyTown.sendChatToPlayer(pl, "Cannot set spawn to this dimension without explicitly setting it using /setspawn <dim> [<x> <y> <z>]");
				return;
			}
		}

		int x, y, z;
		if (args.length > 1) {
			int i = args.length - 3;
			x = (int) getDouble(cs, pl.posX, args[i++]);
			y = (int) getDoubleLimited(cs, pl.posY, args[i++], 0, 0);
			z = (int) getDouble(cs, pl.posZ, args[i++]);
		} else {
			x = (int) pl.posX;
			y = (int) pl.posY;
			z = (int) pl.posZ;
		}

		WorldServer w = MinecraftServer.getServer().worldServerForDimension(dim);
		w.provider.setSpawnPoint(x, y, z);

		Log.warning(String.format("Server spawn for dimension %s set to %s,%s,%s by %s", dim, x, y, z, cs.getCommandSenderName()));
		MyTown.sendChatToPlayer(pl, String.format("Server spawn for dimension %s set to %s,%s,%s", dim, x, y, z));
	}

	private double getDouble(ICommandSender par1ICommandSender, double par2, String par4Str) {
		return getDoubleLimited(par1ICommandSender, par2, par4Str, -30000000, 30000000);
	}

	private double getDoubleLimited(ICommandSender par1ICommandSender, double par2, String par4Str, int par5, int par6) {
		boolean var7 = par4Str.startsWith("~");
		double var8 = var7 ? par2 : 0.0D;

		if (!var7 || par4Str.length() > 1) {
			boolean var10 = par4Str.contains(".");

			if (var7) {
				par4Str = par4Str.substring(1);
			}

			var8 += CommandBase.parseDouble(par1ICommandSender, par4Str);

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

	@Override
	public List<String> dumpCommands() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.setspawn";
	}
}
