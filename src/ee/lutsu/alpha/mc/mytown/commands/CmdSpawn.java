package ee.lutsu.alpha.mc.mytown.commands;

import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.ext.Mffs;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.EnumGameType;

public class CmdSpawn extends CommandBase
{
	@Override
	public String getCommandName() 
	{
		return "spawn";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender cs)
	{
		return cs instanceof EntityPlayerMP && Permissions.canAccess(cs, "mytown.ecmd.spawn");
	}

	@Override
	public void processCommand(ICommandSender cs, String[] args) 
	{
		EntityPlayerMP pl = (EntityPlayerMP)cs;
		Resident res = MyTownDatasource.instance.getOrMakeResident(pl);
		
		res.asyncStartSpawnTeleport(null);
	}
}
