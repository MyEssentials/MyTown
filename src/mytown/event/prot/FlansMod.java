package mytown.event.prot;

import java.lang.reflect.Field;

import mytown.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.TownSettingCollection.Permissions;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class FlansMod extends ProtBase {
	Class<?> clEntityBullet, clBulletType, clEntityGrenade;
	Field fEntityBullet_owner, fEntityBullet_type, fEntityGrenade_thrower, fBulletType_explodeOnImpact, fBulletType_explosion;

	@Override
	public void load() throws Exception {
		clEntityBullet = Class.forName("co.uk.flansmods.common.guns.EntityBullet");
		fEntityBullet_owner = clEntityBullet.getField("owner");
		fEntityBullet_type = clEntityBullet.getField("type");

		clBulletType = Class.forName("co.uk.flansmods.common.guns.BulletType");
		fBulletType_explodeOnImpact = clBulletType.getField("explodeOnImpact");
		fBulletType_explosion = clBulletType.getField("explosion");

		clEntityGrenade = Class.forName("co.uk.flansmods.common.guns.EntityGrenade");
		fEntityGrenade_thrower = clEntityGrenade.getField("thrower");
	}

	@Override
	public boolean loaded() {
		return clEntityBullet != null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return clEntityBullet.isInstance(e) || clEntityGrenade.isInstance(e);
	}

	@Override
	public String update(Entity e) throws Exception {
		if (clEntityBullet.isInstance(e)) {
			Entity owner = (Entity) fEntityBullet_owner.get(e);

			if (!(owner instanceof EntityPlayer)) {
				if (ProtectionEvents.instance.getNPCClasses().contains(owner)) {
					return null;
				}
				return "Owner not a player";
			}

			Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) owner);

			Vec3 posVec = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);
			Vec3 nextPosVec = Vec3.createVectorHelper(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
			MovingObjectPosition hit = e.worldObj.rayTraceBlocks_do_do(posVec, nextPosVec, false, true);

			if (hit.typeOfHit == EnumMovingObjectType.TILE && !res.canInteract(hit.blockX, hit.blockY, hit.blockZ, Permissions.Build) || hit.typeOfHit == EnumMovingObjectType.ENTITY && !res.canAttack(hit.entityHit)) {
				return "Target protected by MyTown";
			}

			Object bulletType = fEntityBullet_type.get(e);
			boolean explode = fBulletType_explodeOnImpact.getBoolean(bulletType);

			if (explode) {
				int radius = fBulletType_explosion.getInt(bulletType);
				if (hit.typeOfHit == EnumMovingObjectType.TILE
						&& (!res.canInteract(e.dimension, hit.blockX - radius, hit.blockY - radius, hit.blockY - radius, hit.blockZ + radius, Permissions.Build) || !res.canInteract(e.dimension, hit.blockX - radius, hit.blockY - radius, hit.blockZ + radius, hit.blockZ + radius, Permissions.Build)
								|| !res.canInteract(e.dimension, hit.blockX + radius, hit.blockY - radius, hit.blockZ + radius, hit.blockZ - radius, Permissions.Build) || !res.canInteract(e.dimension, hit.blockX + radius, hit.blockY - radius, hit.blockZ + radius, hit.blockZ + radius,
								Permissions.Build))) {
					return "Target protected by MyTown";
				} else if (hit.typeOfHit == EnumMovingObjectType.ENTITY && !res.canAttack(hit.entityHit)) {
				}
			}
		}

		return null;
	}

	@Override
	public String getMod() {
		return "Flan's Mod";
	}

	@Override
	public String getComment() {
		return "Stops Flan's Mod Stuff";
	}

}
