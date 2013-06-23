package ee.lutsu.alpha.mc.mytown.commands;

import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.ext.Mffs;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.EnumGameType;

public class CmdWrk extends CommandBase
{
	@Override
	public String getCommandName() 
	{
		return "wrk";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cs)
	{
		return cs instanceof EntityPlayer && Permissions.canAccess(cs, "mytown.adm.cmd.wrk");
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) 
	{
		EntityPlayerMP pl = (EntityPlayerMP)cs;
		EnumGameType mode = pl.theItemInWorldManager.getGameType();
		String name = pl.username.toLowerCase();
		
		if (args.length > 0 && args[0].equalsIgnoreCase("clip"))
		{
			pl.noClip = !pl.noClip;
			pl.sendChatToPlayer("NoClip is now " + (pl.noClip ? "active" : "deactive"));

			return;
		}
		
		if (MinecraftServer.getServer().getConfigurationManager().getOps().contains(name)) // to normal mode
		{
			String grp = name.equals("alphaest") ? "fakedev" : name.equals("sp0nge") ? "fakeowner" : "fakeadmin";
			
			MinecraftServer.getServer().getCommandManager().executeCommand(cs, "/pex user " + name + " group set " + grp);
			MinecraftServer.getServer().getConfigurationManager().removeOp(name);
			if (Mffs.check())
			{
				Mffs.removeAdminBypass(name);
				Log.info("User " + name + " removed from MFFS bypass");
				pl.sendChatToPlayer("Removed from MFFS bypass");
			}
			
			if (mode != EnumGameType.SURVIVAL)
				pl.setGameType(EnumGameType.SURVIVAL);
		}
		else
		{
			String grp = name.equals("alphaest") ? "dev" : name.equals("sp0nge") ? "owner" : "admin";
			
			MinecraftServer.getServer().getCommandManager().executeCommand(cs, "/pex user " + name + " group set " + grp);
			MinecraftServer.getServer().getConfigurationManager().addOp(name);
			
			if (Mffs.check())
			{
				Mffs.grantAdminBypass(name);
				Log.info("User " + name + " added to MFFS bypass");
				pl.sendChatToPlayer("Granted MFFS bypass");
			}
			
			if (mode != EnumGameType.CREATIVE)
				pl.setGameType(EnumGameType.CREATIVE);
		}
	}
}
