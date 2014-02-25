package mytown.event.prot;

import mytown.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.TownSettingCollection.Permissions;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import cpw.mods.fml.common.registry.IThrowableEntity;

public class TheMistsOfRioV extends ProtBase {
	public static TheMistsOfRioV instance = new TheMistsOfRioV();
	public int explosionRadius = 3 + 2;

	private Class<?> clDarkMatter = null, clEntityCustomArrow = null, clEntityPinkEssence = null;

	@Override
	public void load() throws Exception {
		clDarkMatter = Class.forName("sheenrox82.RioV.src.entity.projectile.EntityDarkMatter");
		clEntityCustomArrow = Class.forName("sheenrox82.RioV.src.entity.projectile.EntityCustomArrow");
		clEntityPinkEssence = Class.forName("sheenrox82.RioV.src.entity.projectile.EntityPinkEssence");
	}

	@Override
	public boolean loaded() {
		return clDarkMatter != null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return clDarkMatter.isInstance(e) || clEntityCustomArrow.isInstance(e) || clEntityPinkEssence.isInstance(e);
	}

	@Override
	public String update(Entity e) throws Exception {
		if (clDarkMatter.isInstance(e)) {
			EntityThrowable t = (EntityThrowable) e;
			EntityLivingBase owner = t.getThrower();

			if (owner == null || !(owner instanceof EntityPlayer)) {
				return "No owner or is not a player";
			}

			Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) owner);

			int x = (int) (t.posX + t.motionX);
			int y = (int) (t.posY + t.motionY);
			int z = (int) (t.posZ + t.motionZ);
			int dim = e.dimension;

			if (!thrower.canInteract(dim, x - explosionRadius, y, z - explosionRadius, Permissions.Build) || !thrower.canInteract(dim, x - explosionRadius, y, z + explosionRadius, Permissions.Build) || !thrower.canInteract(dim, x + explosionRadius, y, z - explosionRadius, Permissions.Build)
					|| !thrower.canInteract(dim, x + explosionRadius, y, z + explosionRadius, Permissions.Build)) {
				return "Explosion would hit a protected town";
			}
		} else if (clEntityPinkEssence.isInstance(e)) {
			EntityThrowable t = (EntityThrowable) e;
			EntityLivingBase owner = t.getThrower();

			if (owner == null || !(owner instanceof EntityPlayer)) {
				return "No owner or is not a player";
			}

			Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) owner);

			Vec3 var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);
			Vec3 var2 = Vec3.createVectorHelper(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
			MovingObjectPosition mop = e.worldObj.rayTraceBlocks_do_do(var1, var2, false, true);
			var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);

			if (mop != null) {
				if (mop.typeOfHit == EnumMovingObjectType.ENTITY && !thrower.canAttack(mop.entityHit)) {
					return "Target in MyTown protected area";
				}
			}
		} else if (clEntityCustomArrow.isInstance(e)) {
			IThrowableEntity t = (IThrowableEntity) e;
			EntityLivingBase owner = (EntityLivingBase) t.getThrower();

			if (owner == null || !(owner instanceof EntityPlayer)) {
				return null; // Allow NPCs to use the Custom Arrows
			}

			Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) owner);

			Vec3 var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);
			Vec3 var2 = Vec3.createVectorHelper(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
			MovingObjectPosition mop = e.worldObj.rayTraceBlocks_do_do(var1, var2, false, true);
			var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);

			if (mop != null) {
				if (mop.typeOfHit == EnumMovingObjectType.ENTITY && !thrower.canAttack(mop.entityHit)) {
					return "Target in MyTown protected area";
				}
			}
		}

		return null;
	}

	@Override
	public String getMod() {
		return "The Mists Of RioV";
	}

	@Override
	public String getComment() {
		return "Build check: EntityDarkMatter. Attack Check: Custom arrows, GraviWand";
	}
}
