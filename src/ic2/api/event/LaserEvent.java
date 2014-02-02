package ic2.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.world.WorldEvent;

@Cancelable
public class LaserEvent extends WorldEvent {
	public final Entity lasershot;
	public EntityLivingBase owner;
	public float range;
	public float power;
	public int blockBreaks;
	public boolean explosive;
	public boolean smelt;

	public LaserEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt) {
		super(world);
		this.lasershot = lasershot;
		this.owner = owner;
		this.range = range;
		this.power = power;
		this.blockBreaks = blockBreaks;
		this.explosive = explosive;
		this.smelt = smelt;
	}

	public static class LaserHitsEntityEvent extends LaserEvent {
		public Entity hitentity;

		public LaserHitsEntityEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt, Entity hitentity) {
			super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
			this.hitentity = hitentity;
		}
	}

	public static class LaserHitsBlockEvent extends LaserEvent {
		public int x;
		public int y;
		public int z;
		public int side;
		public boolean removeBlock;
		public boolean dropBlock;
		public float dropChance;

		public LaserHitsBlockEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt, int x, int y, int z, int side, float dropChance, boolean removeBlock, boolean dropBlock) {
			super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
			this.x = x;
			this.y = y;
			this.z = z;
			this.side = side;
			this.removeBlock = removeBlock;
			this.dropBlock = dropBlock;
			this.dropChance = dropChance;
		}
	}

	public static class LaserExplodesEvent extends LaserEvent {
		public float explosionpower;
		public float explosiondroprate;
		public float explosionentitydamage;

		public LaserExplodesEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt, float explosionpower, float explosiondroprate, float explosionentitydamage) {
			super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
			this.explosionpower = explosionpower;
			this.explosiondroprate = explosiondroprate;
			this.explosionentitydamage = explosionentitydamage;
		}
	}

	public static class LaserShootEvent extends LaserEvent {
		ItemStack laseritem;

		public LaserShootEvent(World world, Entity lasershot, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt, ItemStack laseritem) {
			super(world, lasershot, owner, range, power, blockBreaks, explosive, smelt);
			this.laseritem = laseritem;
		}
	}
}