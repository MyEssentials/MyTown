package mytown.event.prot;

import java.lang.reflect.Field;
import java.util.List;

import mytown.entities.Resident;
import mytown.entities.TownSettingCollection.Permissions;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class ArsMagica2 extends ProtBase {
	Class<?> clEntitySpellProjectile, clEntityThrownRock, clEntityThrownSickle, clSpellBase;
	Field fThrowingEntity_EntityThrownRock, fThrowingEntity_EntityThrownSickle;

	@Override
	public void load() throws Exception {
		clSpellBase = Class.forName("am2.items.SpellBase");
		
		clEntitySpellProjectile = Class.forName("am2.entities.EntitySpellProjectile");
		
		clEntityThrownRock = Class.forName("am2.entities.EntityThrownRock");
		fThrowingEntity_EntityThrownRock = clEntityThrownRock.getDeclaredField("throwingEntity");
		
		clEntityThrownSickle = Class.forName("am2.entities.EntityThrownSickle");
		fThrowingEntity_EntityThrownSickle = clEntityThrownSickle.getDeclaredField("throwingEntity");
	}

	@Override
	public boolean loaded() {
		return clSpellBase != null && clEntitySpellProjectile != null && clEntityThrownRock != null && clEntityThrownSickle != null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return clEntitySpellProjectile.isInstance(e) || clEntityThrownRock.isInstance(e) || clEntityThrownSickle.isInstance(e);
	}

	@Override
	public boolean isEntityInstance(Item item) {
		return clSpellBase.isInstance(item);
	}
	

	@Override
	public String update(Entity e) throws Exception {
		if (clEntitySpellProjectile.isInstance(e)){
		} else if (clEntityThrownRock.isInstance(e)){
		} else if (clEntityThrownSickle.isInstance(e)){
			EntityLivingBase thrower = (EntityLivingBase) fThrowingEntity_EntityThrownSickle.get(e);
			if (thrower == null || !(thrower instanceof EntityPlayer))
				return null;
			Resident res = ProtectionEvents.instance.lastOwner = Resident.getOrMake((EntityPlayer) thrower);
			if (res == null)
				return null;

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

				if (mop.typeOfHit.equals(EnumMovingObjectType.ENTITY) && !res.canAttack(mop.entityHit) || mop.typeOfHit.equals(EnumMovingObjectType.TILE) && !res.canInteract(e.dimension, mop.blockX, mop.blockY, mop.blockZ, Permissions.Build)) {
					return "Target in MyTown protected area";
				}
			}
		}
		return null;
	}

	@Override
	public String update(Resident r, Item tool, ItemStack item) throws Exception {
		return null;
	}
	
	@Override
	public String getMod() {
		return "Ars Magics 2";
	}

	@Override
	public String getComment() {
		return "Protects against Ars Magica 2 Spells";
	}
}