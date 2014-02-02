package mytown.event.prot;

import java.lang.reflect.Method;
import java.util.List;

import mytown.entities.Resident;
import mytown.entities.TownSettingCollection.Permissions;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import cpw.mods.fml.common.registry.IThrowableEntity;

public class ProjectileProtection extends ProtBase {
	public static ProjectileProtection instance = new ProjectileProtection();

	@Override
	public boolean loaded() {
		return true;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		Method m = null;
		try {
			m = e.getClass().getDeclaredMethod("getThrower");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return e instanceof IThrowableEntity || e instanceof EntityThrowable || e instanceof EntityArrow || m != null;
	}

	@Override
	public String update(Entity e) throws Exception {
		Entity thrower = null;
		if (e instanceof IThrowableEntity) {
			thrower = ((IThrowableEntity) e).getThrower();
		} else if (e instanceof EntityThrowable) {
			thrower = ((EntityThrowable) e).getThrower();
		} else if (e instanceof EntityArrow) {
			thrower = ((EntityArrow) e).shootingEntity;
		} else {
			thrower = (Entity) e.getClass().getDeclaredMethod("getThrower").invoke(e);
		}

		if (thrower == null || !(thrower instanceof EntityPlayer))
			return "Thrower is null or not a player";
		Resident res = ProtectionEvents.instance.lastOwner = Resident.getOrMake((EntityPlayer) thrower);
		if (res == null)
			return "Resident is null";

		Vec3 vec3 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX, e.posY, e.posZ);
		Vec3 vec31 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
		MovingObjectPosition mop = e.worldObj.clip(vec3, vec31);
		vec3 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX, e.posY, e.posZ);
		vec31 = e.worldObj.getWorldVec3Pool().getVecFromPool(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);

		if (mop != null) {
			vec31 = e.worldObj.getWorldVec3Pool().getVecFromPool(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
		}

		if (!e.worldObj.isRemote) {
			Entity entity = null;
			List<?> list = e.worldObj.getEntitiesWithinAABBExcludingEntity(e, e.boundingBox.addCoord(e.motionX, e.motionY, e.motionZ).expand(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;

			for (int j = 0; j < list.size(); ++j) {
				Entity entity1 = (Entity) list.get(j);

				if (entity1.canBeCollidedWith() && (entity1 != thrower)) {
					float f = 0.3F;
					AxisAlignedBB axisalignedbb = entity1.boundingBox.expand(f, f, f);
					MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

					if (movingobjectposition1 != null) {
						double d1 = vec3.distanceTo(movingobjectposition1.hitVec);

						if (d1 < d0 || d0 == 0.0D) {
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null) {
				mop = new MovingObjectPosition(entity);
			}

			if (mop == null)
				return null;

			if (mop.typeOfHit.equals(EnumMovingObjectType.ENTITY) && !res.canAttack(mop.entityHit) || mop.typeOfHit.equals(EnumMovingObjectType.TILE) && !res.canInteract(mop.blockX, mop.blockY, mop.blockZ, Permissions.Build)) {
				return "Target in MyTown protected area";
			}
		}
		return null;
	}

	@Override
	public String getMod() {
		return "Projectile Protection";
	}

	@Override
	public String getComment() {
		return "Stops projectiles from bypassing";
	}
}