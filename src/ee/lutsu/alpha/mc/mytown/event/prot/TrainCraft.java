package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;

import ee.lutsu.alpha.mc.mytown.ChatChannel;
import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.commands.CmdChat;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

public class TrainCraft extends ProtBase
{
	public static TrainCraft instance = new TrainCraft();
	
	Class clEntityTracksBuilder;
	
	@Override
	public void load() throws Exception
	{
		clEntityTracksBuilder = Class.forName("src.train.common.entity.rollingStock.EntityTracksBuilder");
	}
	
	@Override
	public boolean loaded() { return clEntityTracksBuilder != null; }
	@Override
	public boolean isEntityInstance(Entity e) { return clEntityTracksBuilder.isInstance(e); }
	
	@Override
	public String update(Entity e) throws Exception
	{
		if ((int)e.posX == (int)e.prevPosX && (int)e.posY == (int)e.prevPosY && (int)e.posZ == (int)e.prevPosZ) // didn't move
			return null;
		
		int radius = 3 + 1;
		int y = (int)e.posY + 2;
		
		if (!canRoam(e.dimension, e.posX - radius, y - radius, y + radius, e.posZ - radius) ||
			!canRoam(e.dimension, e.posX - radius, y - radius, y + radius, e.posZ + radius) ||
			!canRoam(e.dimension, e.posX + radius, y - radius, y + radius, e.posZ - radius) ||
			!canRoam(e.dimension, e.posX + radius, y - radius, y + radius, e.posZ + radius))
		{
			blockAction((EntityMinecart)e);
			return null;
		}
		
		return null;
	}
	
	private boolean canRoam(int dim, double x, double yFrom, double yTo, double z)
	{
		TownBlock b = MyTownDatasource.instance.getPermBlockAtCoord(dim, (int)x, (int)yFrom, (int)yTo, (int)z);

		if (b == null || b.town() == null)
			return MyTown.instance.getWorldWildSettings(dim).allowStevecartsMiners && MyTown.instance.getWorldWildSettings(dim).allowStevecartsRailers;

		return b.settings.allowStevecartsMiners && b.settings.allowStevecartsRailers;
	}
	
	private void blockAction(EntityMinecart e) throws IllegalArgumentException, IllegalAccessException
	{
		dropMinecart(e);
		
		Log.severe(String.format("§4Stopped a train found in %s @ dim %s, %s,%s,%s",
				e.dimension, (int)e.posX, (int)e.posY, (int)e.posZ));
		
		String msg = String.format("A train broke @ %s,%s,%s because it wasn't allowed there", (int)e.posX, (int)e.posY, (int)e.posZ);
		String formatted = Formatter.formatChatSystem(msg, ChatChannel.Local);
		CmdChat.sendChatToAround(e.dimension, e.posX, e.posY, e.posZ, formatted, null);
	}

	public String getMod() { return "TrainCraft"; }
	public String getComment() { return "Town permission: allowStevecartsMiners & allowStevecartsRailers (yes, same)"; }
}
