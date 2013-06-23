package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;

import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class Mobs extends ProtBase
{
	public static Mobs instance = new Mobs();

	private boolean loaded = false;

	@Override
	public void load() throws Exception
	{
		MinecraftForge.EVENT_BUS.register(this);
		loaded = true;
	}
	
	@Override
	public boolean loaded() { return loaded; }
	@Override
	public boolean isEntityInstance(Entity e) { return e instanceof EntityMob; }
	
	@Override
	public String update(Entity e) throws Exception
	{
		if ((int)e.posX == (int)e.prevPosX && (int)e.posY == (int)e.prevPosY && (int)e.posZ == (int)e.prevPosZ) // didn't move
			return null;

		EntityMob mob = (EntityMob)e;
        if (e.isEntityAlive())
        {
        	if (!canBe(mob))
        	{
        		// silent removal of the mob
        		ProtectionEvents.instance.toRemove.add(e);
        		return null;
        	}
        }
        
		return null;
	}
	
	@ForgeSubscribe
	public void entityJoinWorld(EntityJoinWorldEvent ev)
	{
		if (!isEntityInstance(ev.entity))
			return;
		
		if (!canBe((EntityMob)ev.entity))
			ev.setCanceled(true);
	}
	
	private boolean canBe(EntityMob mob)
	{
		return canBe2(mob.dimension, mob.posX, mob.posY, mob.posY + 1, mob.posZ);
	}
	
	private boolean canBe2(int dim, double x, double yFrom, double yTo, double z)
	{
		TownBlock b = MyTownDatasource.instance.getBlock(dim, ChunkCoord.getCoord(x), ChunkCoord.getCoord(z));
		if (b != null && b.settings.yCheckOn)
		{
			if (yTo < b.settings.yCheckFrom || yFrom > b.settings.yCheckTo)
				b = b.getFirstFullSidingClockwise(b.town());
		}
		
		if (b == null || b.town() == null)
			return !MyTown.instance.getWorldWildSettings(dim).disableMobs;

		return !b.settings.disableMobs;
	}
	
	
	public String getMod() { return "VanillaMobLocation"; }
	public String getComment() { return "Town permission: disableMobs"; }
	public boolean defaultEnabled() { return true; }
}
