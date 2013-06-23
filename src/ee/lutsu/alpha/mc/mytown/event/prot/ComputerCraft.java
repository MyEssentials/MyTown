package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import railcraft.common.api.carts.IExplosiveCart;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;

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

public class ComputerCraft extends ProtBase
{
	public static ComputerCraft instance = new ComputerCraft();
	
	Class clTurtle = null;
	Method mTerminate, mIsOn;
	Field fMoved, fClientState;
	
	public HashMap<Object, Object> turtles = new HashMap<Object, Object>();
	public HashMap<ChunkCoordinates, Long> anti_spam = new HashMap<ChunkCoordinates, Long>();
	public int anti_spam_counter = 0;
	
	@Override
	public void reload()
	{
		anti_spam_counter = 0;
		turtles = new HashMap<Object, Object>();
		anti_spam = new HashMap<ChunkCoordinates, Long>();
	}
	
	@Override
	public void load() throws Exception
	{
		clTurtle = Class.forName("dan200.turtle.shared.TileEntityTurtle");
		mTerminate = clTurtle.getDeclaredMethod("terminate");
		mIsOn = clTurtle.getDeclaredMethod("isOn");
		fMoved = clTurtle.getDeclaredField("m_moved");
		fClientState = clTurtle.getDeclaredField("m_clientState");
		fClientState.setAccessible(true);
	}
	
	@Override
	public boolean loaded() { return clTurtle != null; }
	@Override
	public boolean isEntityInstance(TileEntity e) { return clTurtle.isInstance(e); }
	
	@Override
	public String update(TileEntity e) throws Exception
	{
		cleanAntiSpam();
		
		Object state = fClientState.get(e);
		Object prev_turtle = turtles.get(state);

		if (prev_turtle == e || !(Boolean)mIsOn.invoke(e))
			return null;
		
		turtles.put(state, e);
		
		int radius = 1;
		int dim = e.worldObj.provider.dimensionId;
		if (canRoam(dim, e.xCoord - radius, e.yCoord, e.yCoord, e.zCoord) &&
			canRoam(dim, e.xCoord + radius, e.yCoord, e.yCoord, e.zCoord) &&
			canRoam(dim, e.xCoord, e.yCoord, e.yCoord, e.zCoord - radius) &&
			canRoam(dim, e.xCoord, e.yCoord, e.yCoord, e.zCoord + radius) &&
			canRoam(dim, e.xCoord, e.yCoord - radius, e.yCoord + radius, e.zCoord))
			return null;
		
		turtles.put(state, null);
		blockAction(e);
		return null;
	}

	private void blockAction(TileEntity e) throws Exception
	{
		mTerminate.invoke(e);

		ChunkCoordinates c = new ChunkCoordinates(e.xCoord, e.yCoord, e.zCoord);
		if (canScream(c))
		{
			Log.severe(String.format("§4Stopped a computercraft turtle found @ dim %s, %s,%s,%s",
					e.worldObj.provider.dimensionId, e.xCoord, e.yCoord, e.zCoord));
			
			String msg = String.format("A turtle stopped @ %s,%s,%s because it wasn't allowed there", e.xCoord, e.yCoord, e.zCoord);
			String formatted = Formatter.formatChatSystem(msg, ChatChannel.Local);
			CmdChat.sendChatToAround(e.worldObj.provider.dimensionId, e.xCoord, e.yCoord, e.zCoord, formatted, null);
			
			anti_spam.put(c, System.currentTimeMillis() + 60000);
		}
	}

	private boolean canRoam(int dim, int x, int y, int y2, int z)
	{
		TownBlock b = MyTownDatasource.instance.getPermBlockAtCoord(dim, x, y, y2, z);

		if (b == null || b.town() == null)
			return MyTown.instance.getWorldWildSettings(dim).allowCCTurtles;

		return b.settings.allowCCTurtles;
	}
	
	private boolean canScream(ChunkCoordinates c)
	{
		return anti_spam.get(c) == null;
	}
	
	private void cleanAntiSpam()
	{
		anti_spam_counter++;
		
		if (anti_spam_counter > 1000)
		{
			anti_spam_counter = 0;
			long time = System.currentTimeMillis();
			
			for (Iterator<Entry<ChunkCoordinates, Long>> it = anti_spam.entrySet().iterator(); it.hasNext(); )
			{
				Entry<ChunkCoordinates, Long> kv = it.next();
				if (kv.getValue() < time)
					it.remove();
			}
		}
	}

	public String getMod() { return "ComputerCraft"; }
	public String getComment() { return "Town permission: ccturtles "; }
}
