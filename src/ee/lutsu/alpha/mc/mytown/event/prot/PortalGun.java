package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;

import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;

public class PortalGun extends ProtBase
{
	public static PortalGun instance = new PortalGun();
	
	Class clPortalBall = null;
	List<String> systemOwnerNames = Lists.newArrayList("", "def", "coopA", "coopB");
	
	@Override
	public void load() throws Exception
	{
		clPortalBall = Class.forName("portalgun.common.entity.EntityPortalBall");
	}
	
	@Override
	public boolean loaded() { return clPortalBall != null; }
	@Override
	public boolean isEntityInstance(Entity e) { return e.getClass() == clPortalBall; }

	@Override
	public String update(Entity e) throws Exception
	{
		if ((int)e.posX == (int)e.prevPosX && (int)e.posY == (int)e.prevPosY && (int)e.posZ == (int)e.prevPosZ) // didn't move
			return null;
		
		String owner = e.getDataWatcher().getWatchableObjectString(18);
		
		if (owner != null && !systemOwnerNames.contains(owner)) // not default portal
		{
			if (owner.endsWith("_A") || owner.endsWith("_B"))
				owner = owner.substring(0, owner.length() - 2);
			
			Resident r = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident(owner);
		    if (r == null || !r.isOnline())
		    	return "Owner " + owner + " not found or offline";
		    
			for (int ii = 0; ii < 5; ii++)
			{
			    int x = (int)(e.motionX / 5.0D * ii + e.posX);
			    int y = (int)(e.motionY / 5.0D * ii + e.posY);
			    int z = (int)(e.motionZ / 5.0D * ii + e.posZ);

			    if (!r.canInteract(x, y, z, Permissions.Build))
			    	return "Cannot shoot portals in this town";
			}
		}
		else
		{
			for (int ii = 0; ii < 5; ii++)
			{
			    int x = (int)(e.motionX / 5.0D * ii + e.posX);
			    int y = (int)(e.motionY / 5.0D * ii + e.posY);
			    int z = (int)(e.motionZ / 5.0D * ii + e.posZ);

			    int cx = ChunkCoord.getCoord(x);
			    int cz = ChunkCoord.getCoord(z);
			    
				TownBlock b = MyTownDatasource.instance.getBlock(e.dimension, x, z);
				if (b != null && b.town() != null)
					return "Cannot use default portals in towns";
			}
		}

		return null;
	}
	
	public String getMod() { return "PortalgunMod"; }
	public String getComment() { return "Build check: EntityPortalBall. Disables non-owner balls completly in town"; }
}
