package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;

import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;

public class Mekanism extends ProtBase
{
	public static Mekanism instance = new Mekanism();

    public float explosionRadius = 6;
    
    Class clEntityObsidianTNT;
    Field fEntityObsidianTNT_Fuse, fMekanism_ObsidianTNTBlastRadius;
    
	@Override
	public void load() throws Exception
	{
		clEntityObsidianTNT = Class.forName("mekanism.common.EntityObsidianTNT");
		fEntityObsidianTNT_Fuse = clEntityObsidianTNT.getDeclaredField("fuse");
		fMekanism_ObsidianTNTBlastRadius = Class.forName("mekanism.common.Mekanism").getDeclaredField("ObsidianTNTBlastRadius");
		
		explosionRadius = fMekanism_ObsidianTNTBlastRadius.getFloat(null);
	}
	
	@Override
	public boolean loaded() { return clEntityObsidianTNT != null; }
	@Override
	public boolean isEntityInstance(Entity e) { return clEntityObsidianTNT.isInstance(e); }
	
	@Override
	public String update(Entity e) throws Exception
	{
		if (e.isDead || fEntityObsidianTNT_Fuse.getInt(e) > 1)
			return null;
		
        int radius = (int)Math.ceil(explosionRadius) + 2; // 2 for safety

        int x1 = ((int)e.posX - radius) >> 4;
        int z1 = ((int)e.posZ - radius) >> 4;
        int x2 = ((int)e.posX + radius) >> 4;
        int z2 = ((int)e.posZ + radius) >> 4;
        
        boolean canBlow = true;
        for (int x = x1; x <= x2 && canBlow; x++)
        {
	        for (int z = z1; z <= z2 && canBlow; z++)
	        {
		        if (!canBlow(e.dimension, x << 4, (int)e.posY - radius, (int)e.posY + radius, z << 4))
			        canBlow = false;
	        }
        }

        return canBlow ? null : "TNT explosion disabled here";
	}
	
	private boolean canBlow(int dim, int x, int yFrom, int yTo, int z)
	{
		TownBlock b = MyTownDatasource.instance.getPermBlockAtCoord(dim, x, yFrom, yTo, z);

		if (b == null || b.town() == null)
			return !MyTown.instance.getWorldWildSettings(dim).disableTNT;

		return !b.settings.disableTNT;
	}
	
	public String getMod() { return "Mekanism"; }
	public String getComment() { return "Town permission: disableTNT"; }
	public boolean defaultEnabled() { return false; }
}
