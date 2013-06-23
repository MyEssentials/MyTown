package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class RedPower extends ProtBase
{
	public static RedPower instance = new RedPower();
	public int explosionRadius = 6;
	
	private Class clTileFrameMoving = null, clTileMotor;
	private Field fMotorX, fMotorY, fMotorZ, fMoveDir;

	@Override
	public void load() throws Exception
	{
		clTileFrameMoving = Class.forName("com.eloraam.redpower.machine.TileFrameMoving");
		fMotorX = clTileFrameMoving.getDeclaredField("motorX");
		fMotorY = clTileFrameMoving.getDeclaredField("motorY");
		fMotorZ = clTileFrameMoving.getDeclaredField("motorZ");
		
		clTileMotor = Class.forName("com.eloraam.redpower.machine.TileMotor");
		fMoveDir = clTileMotor.getDeclaredField("MoveDir");
	}
	
	@Override
	public boolean loaded() { return clTileFrameMoving != null; }
	@Override
	public boolean isEntityInstance(TileEntity e) { return e.getClass() == clTileFrameMoving; }
	
	@Override
	public String update(TileEntity e) throws Exception
	{
		int mx = fMotorX.getInt(e);
		int my = fMotorY.getInt(e);
		int mz = fMotorZ.getInt(e);
		
		String s = updateSub(e, mx, my, mz);
		
		if (s == null)
			return null;
		
		Log.severe(String.format("Entity %s tried to bypass using %s", e.toString(), s));
		
		TileEntity motor = e.worldObj.getBlockTileEntity(mx, my, mz);
		
		if (!ProtectionEvents.instance.toRemoveTile.contains(motor))
			ProtectionEvents.instance.toRemoveTile.add(motor);
		
		return null;
	}
	
	private String updateSub(TileEntity e, int mx, int my, int mz) throws Exception
	{

		/*
		TileEntity motor = e.worldObj.getBlockTileEntity(mx, my, mz);
		if (motor == null)
			return "No motor";
		
		int dir = fMoveDir.getInt(motor);
		*/
		//Log.info(String.format("moving - %s,%s,%s dir %s motor %s", e.xCoord, e.yCoord, e.zCoord, dir, motor.toString()));
		// get plot owner
		TownBlock motorPlot = MyTownDatasource.instance.getBlock(e.worldObj.provider.dimensionId, ChunkCoord.getCoord(mx), ChunkCoord.getCoord(mz));
		if (motorPlot != null && motorPlot.settings.yCheckOn)
		{
			if (my < motorPlot.settings.yCheckFrom || my > motorPlot.settings.yCheckTo)
				motorPlot = motorPlot.getFirstFullSidingClockwise(motorPlot.town());
		}
		
		Resident actor = null;
		if (motorPlot != null && motorPlot.town() != null)
		{
			if (motorPlot.owner() != null)
				actor = motorPlot.owner();
			else
				actor = motorPlot.town().getFirstMayor();
		}
		
		if (actor == null) // zero resident town or in the wild
			actor = MyTownDatasource.instance.getOrMakeResident("#redpower#");
		
		if (!actor.canInteract(e.worldObj.provider.dimensionId, e.xCoord, e.yCoord, e.zCoord, Permissions.Build))
			return String.format("Redpower frame at Dim %s (%s,%s,%s), motor at Dim %s (%s,%s,%s) failed. Actor: %s", 
					e.worldObj.provider.dimensionId, e.xCoord, e.yCoord, e.zCoord, 
					e.worldObj.provider.dimensionId, mx, my, mz,
					actor.name());

		return null;
	}

	public String getMod() { return "RedPower"; }
	public String getComment() { return "Build check: TileFrameMoving & TileMotor"; }
}
