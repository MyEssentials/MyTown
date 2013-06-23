package ee.lutsu.alpha.mc.mytown.entities;

import java.security.acl.Owner;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes.Name;

import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class TownBlock
{
	private int world_dimension;
	private int chunkX;
	private int chunkZ;
	private Town town;
	private Resident owner;
	public String owner_name; // only for sql loading. Don't use.

	public int x() { return chunkX; }
	public int z() { return chunkZ; }
	public int worldDimension() { return world_dimension; }
	
	public Town town() { return town; }
	public Resident owner() { return owner; }
	public String ownerDisplay() { return owner == null ? "-" : owner.name(); }
	public void setTown(Town val) 
	{ 
		town = val;
		settings.setParent(town == null ? null : owner != null ? owner.settings : town.settings);
	}
	public void setOwner(Resident val) 
	{ 
		sqlSetOwner(val);
		save(); 
	}
	public void sqlSetOwner(Resident val) 
	{
		owner = val; 
		settings.setParent(town == null ? null : owner != null ? owner.settings : town.settings);
	}
	
	// extra
	public TownSettingCollection settings = new TownSettingCollection();
	
	public TownBlock(int pWorld, int x, int z)
	{
		world_dimension = pWorld;
		chunkX = x;
		chunkZ = z;

		settings.tag = this;
		settings.saveHandler = new TownSettingCollection.ISettingsSaveHandler() 
		{
			public void save(TownSettingCollection sender, Object tag) 
			{
				((TownBlock)tag).save();
			}
		};
	}
	
	public static TownBlock deserialize(String info)
	{
		String[] splits = info.split(";");
		if (splits.length < 3)
			throw new RuntimeException("Error in block info : " + info);

		TownBlock t = new TownBlock(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), Integer.parseInt(splits[2]));
		
		if (splits.length > 3)
			t.owner_name = splits[3];
		if (splits.length > 4)
			t.settings.deserializeNorefresh(splits[4]);
		
		return t;
	}
	
	public String serialize() // don't use space
	{
		return worldDimension() + ";" +
			String.valueOf(x()) + ";" +
			String.valueOf(z()) + ";" +
			(owner == null ? "" : owner.name()) + ";" +
			settings.serialize();
	}
	
	public boolean equals(TownBlock block)
	{
		return chunkX == block.chunkX && 
			   chunkZ == block.chunkZ && 
			   world_dimension == block.world_dimension;
	}
	
	public boolean equals(int dim, int x, int z)
	{
		return chunkX == x && 
			   chunkZ == z && 
			   world_dimension == dim;
	}
	
	public int squaredDistanceTo(TownBlock b)
	{
		if (world_dimension != b.world_dimension)
			throw new RuntimeException("Cannot measure distance to ");
		
		return Math.abs((chunkX - b.chunkX) * (chunkX - b.chunkX) + (chunkZ - b.chunkZ) * (chunkZ - b.chunkZ));
	}
	
	public void save()
	{
		if (town != null)
			town.save();
	}
	
	public TownBlock getFirstFullSidingClockwise(Town notForTown)
	{
		TownBlock b;
		
		b = MyTownDatasource.instance.getBlock(world_dimension, chunkX, chunkZ - 1);
		if (b != null && b.town != null && b.town != notForTown && !b.settings.yCheckOn)
			return b;
		
		b = MyTownDatasource.instance.getBlock(world_dimension, chunkX + 1, chunkZ);
		if (b != null && b.town != null && b.town != notForTown && !b.settings.yCheckOn)
			return b;
		
		b = MyTownDatasource.instance.getBlock(world_dimension, chunkX, chunkZ + 1);
		if (b != null && b.town != null && b.town != notForTown && !b.settings.yCheckOn)
			return b;
		
		b = MyTownDatasource.instance.getBlock(world_dimension, chunkX - 1, chunkZ);
		if (b != null && b.town != null && b.town != notForTown && !b.settings.yCheckOn)
			return b;
		
		return null;
	}
}
