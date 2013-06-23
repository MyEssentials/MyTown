package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;

import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;

public class ModularPowersuits extends ProtBase
{
	public static ModularPowersuits instance = new ModularPowersuits();

    Class clEntityPlasmaBolt;
    Field fEntityPlasmaBolt_shootingEntity, fEntityPlasmaBolt_explosiveness, fEntityPlasmaBolt_size;
    
	@Override
	public void load() throws Exception
	{
		clEntityPlasmaBolt = Class.forName("net.machinemuse.powersuits.entity.EntityPlasmaBolt");
		fEntityPlasmaBolt_shootingEntity = clEntityPlasmaBolt.getDeclaredField("shootingEntity");
		fEntityPlasmaBolt_explosiveness = clEntityPlasmaBolt.getDeclaredField("explosiveness");
		fEntityPlasmaBolt_size = clEntityPlasmaBolt.getDeclaredField("size");
	}
	
	@Override
	public boolean loaded() { return clEntityPlasmaBolt != null; }
	@Override
	public boolean isEntityInstance(Entity e) { return clEntityPlasmaBolt.isInstance(e); }
	
	@Override
	public String update(Entity e) throws Exception
	{
		if (e.isDead)
			return null;
		
		MovingObjectPosition pos = getThrowableHitOnNextTick((EntityThrowable)e);
		
		if (pos == null)
			return null;
		
		Entity shooter = (Entity)fEntityPlasmaBolt_shootingEntity.get(e);
		if (!(shooter instanceof EntityPlayer))
			return "Allowed for players only";
		
        int radius = (int)Math.ceil(fEntityPlasmaBolt_size.getDouble(e) / 50.0D * 3.0D * fEntityPlasmaBolt_explosiveness.getDouble(e)) + 2; // 2 for safety
        Resident res = ProtectionEvents.instance.lastOwner = Resident.getOrMake((EntityPlayer)shooter);
        
        int x1 = ((int)e.posX - radius) >> 4;
        int z1 = ((int)e.posZ - radius) >> 4;
        int x2 = ((int)e.posX + radius) >> 4;
        int z2 = ((int)e.posZ + radius) >> 4;
        
        boolean canBlow = true;
        for (int x = x1; x <= x2 && canBlow; x++)
        {
	        for (int z = z1; z <= z2 && canBlow; z++)
	        {
		        if (!res.canInteract(e.dimension, x << 4, (int)e.posY - radius, (int)e.posY + radius, z << 4, Permissions.Build))
			        canBlow = false;
	        }
        }

        return canBlow ? null : "No build rights here";
	}

	public String getMod() { return "ModularPowersuits"; }
	public String getComment() { return "Permissions check: Build"; }
	public boolean defaultEnabled() { return false; }
}
