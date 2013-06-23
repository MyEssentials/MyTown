package ee.lutsu.alpha.mc.mytown.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;

public class SavedHome
{
	public String name;
	
	public int dim;
	
	public double x;
	public double y;
	public double z;
	
	public float look1;
	public float look2;
	
	protected SavedHome()
	{
	}
	
	public SavedHome(String name, Entity entityFrom)
	{
		this.name = name;
		reset(entityFrom);
	}
	
	public void reset(Entity entityFrom)
	{
		dim = entityFrom.dimension;
		x = entityFrom.posX;
		y = entityFrom.posY;
		z = entityFrom.posZ;
		look1 = entityFrom.rotationYaw;
		look2 = entityFrom.rotationPitch;
	}
	
	public static SavedHome deserialize(String str)
	{
		SavedHome h = new SavedHome();
		
		String[] a = str.split("/");
		
		h.name = a[0];
		h.dim = Integer.parseInt(a[1]);
		
		h.x = Double.parseDouble(a[2]);
		h.y = Double.parseDouble(a[3]);
		h.z = Double.parseDouble(a[4]);
		
		h.look1 = Float.parseFloat(a[5]);
		h.look2 = Float.parseFloat(a[6]);
		
		return h;
	}
	
	public static SavedHome fromBed(EntityPlayerMP entityFrom)
	{
		ChunkCoordinates c = entityFrom.getBedLocation();
		if (c == null)
			return null;
		
		SavedHome h = new SavedHome();
		h.dim = entityFrom.worldObj.provider.getRespawnDimension(entityFrom);
		
		h.x = c.posX;
		h.y = c.posY;
		h.z = c.posZ;
		h.look1 = 0;
		h.look2 = 0;
		
		return h;
	}
	
	public String serialize()
	{
		return String.format("%s/%s/%s/%s/%s/%s/%s", name, dim, x, y, z, look1, look2);
	}
}
