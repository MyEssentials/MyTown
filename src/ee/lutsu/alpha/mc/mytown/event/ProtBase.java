package ee.lutsu.alpha.mc.mytown.event;

import java.util.List;

import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityMinecart;
//import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public abstract class ProtBase 
{
	public boolean enabled = false;
	
	public void reload() { }
	public void load() throws Exception { }
	public boolean loaded() { return true; }
	public boolean isEntityInstance(Item item) 
	{ 
		return false; 
	}
	
	public boolean isEntityInstance(Entity e) 
	{ 
		return false; 
	}
	
	public boolean isEntityInstance(TileEntity e) 
	{ 
		return false; 
	}
	
	public String update(Resident r, Item tool, ItemStack item) throws Exception 
	{
		throw new Exception("Protection doesn't support Players");
	}
	
	public String update(Entity e) throws Exception 
	{
		throw new Exception("Protection doesn't support Entity's");
	}
	
	public String update(TileEntity e) throws Exception 
	{
		throw new Exception("Protection doesn't support TileEntity's");
	}
	
	public abstract String getMod();
	public abstract String getComment();
	public boolean defaultEnabled() { return false; }
	
	public static Resident getActorFromLocation(int dim, int x, int y, int z, String defaultActor)
	{
		TownBlock block = MyTownDatasource.instance.getPermBlockAtCoord(dim, x, y, z);
		
		Resident actor = null;
		if (block != null && block.town() != null)
		{
			if (block.owner() != null)
				actor = block.owner();
			else
				actor = block.town().getFirstMayor();
		}
		
		if (actor == null) // zero resident town or in the wild
			actor = MyTownDatasource.instance.getOrMakeResident(defaultActor);
		
		return actor;
	}
	
	public static MovingObjectPosition getThrowableHitOnNextTick(EntityThrowable e)
	{
        Vec3 var16 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX, e.posY, e.posZ);
        Vec3 var2 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
        MovingObjectPosition var3 = e.worldObj.rayTraceBlocks(var16, var2);
        var16 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX, e.posY, e.posZ);
        var2 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);

        if (var3 != null)
        {
            var2 = e.worldObj.getWorldVec3Pool().getVecFromPool(var3.hitVec.xCoord, var3.hitVec.yCoord, var3.hitVec.zCoord);
        }


        Entity var4 = null;
        List<?> var5 = e.worldObj.getEntitiesWithinAABBExcludingEntity(e, e.boundingBox.addCoord(e.motionX, e.motionY, e.motionZ).expand(1.0D, 1.0D, 1.0D));
        double var6 = 0.0D;
        //EntityLiving var8 = e.getThrower();

        for (int var9 = 0; var9 < var5.size(); ++var9)
        {
            Entity var10 = (Entity)var5.get(var9);

            if (var10.canBeCollidedWith())
            {
                float var11 = 0.3F;
                AxisAlignedBB var12 = var10.boundingBox.expand((double)var11, (double)var11, (double)var11);
                MovingObjectPosition var13 = var12.calculateIntercept(var16, var2);

                if (var13 != null)
                {
                    double var14 = var16.distanceTo(var13.hitVec);

                    if (var14 < var6 || var6 == 0.0D)
                    {
                        var4 = var10;
                        var6 = var14;
                    }
                }
            }
        }

        if (var4 != null)
        {
            var3 = new MovingObjectPosition(var4);
        }


        if (var3 != null)
        {
            if (var3.typeOfHit == EnumMovingObjectType.TILE && e.worldObj.getBlockId(var3.blockX, var3.blockY, var3.blockZ) == Block.portal.blockID)
            {
                return null;
            }
            else
            {
                return var3;
            }
        }
        
		return null;
	}
	
	protected void dropMinecart(EntityMinecart e) 
	{
		try
		{
			e.killMinecart(DamageSource.generic); // drop cart as item, may get changed in the future
		}
		catch (Exception ex)
		{
			int times = 10;
			for (; times >= 0 && !e.isDead; times--)
			{
				try
				{
					e.attackEntityFrom(DamageSource.generic, 1000);
				}
				catch (Exception ex2) {}
			}
			
			if (times == 0)
				e.setDead(); // if nothing else works, just kill it
		}
	}
}
