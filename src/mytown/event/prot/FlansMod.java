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
	public static FlansMod instance = new FlansMod();
	
	Class<?> clEntityBullet, clBulletType, clEntityGrenade, clGrenadeType;
	Field fEntityBullet_owner, fEntityBullet_type, fEntityGrenade_thrower, fEntityGrenade_type, fBulletType_explodeOnImpact, fBulletType_explosion, fGrenadeType_fireRadius, fGrenadeType_explosionRadius;

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
		fEntityGrenade_type = clEntityGrenade.getField("type");
		
		clGrenadeType = Class.forName("co.uk.flansmods.common.guns.GrenadeType");
		fGrenadeType_fireRadius = clGrenadeType.getField("fireRadius");
		fGrenadeType_explosionRadius = clGrenadeType.getField("explosionRadius");
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

			if (hit.typeOfHit == EnumMovingObjectType.TILE && !res.canInteract(e.dimension, hit.blockX, hit.blockY, hit.blockZ, Permissions.Build) || hit.typeOfHit == EnumMovingObjectType.ENTITY && !res.canAttack(hit.entityHit)) {
				return "Target protected by MyTown";
			}

			Object bulletType = fEntityBullet_type.get(e);
			boolean explode = fBulletType_explodeOnImpact.getBoolean(bulletType);

			if (explode) {
				int x = (int)e.posX;
				int y = (int)e.posY;
				int z = (int)e.posZ;
				int radius = fBulletType_explosion.getInt(bulletType);
				if (!res.canInteract(e.dimension, x - radius, y - radius, y + radius, z + radius, Permissions.Build) ||
						!res.canInteract(e.dimension, x - radius, y - radius, y + radius, z + radius, Permissions.Build) ||
						!res.canInteract(e.dimension, x + radius, y - radius, y + radius, z - radius, Permissions.Build) ||
						!res.canInteract(e.dimension, x + radius, y - radius, y + radius, z + radius, Permissions.Build)) {
					return "Target protected by MyTown";
				}
			}
		} else if (clEntityGrenade.isInstance(e)){
			Entity owner = (Entity) fEntityGrenade_thrower.get(e);

			if (!(owner instanceof EntityPlayer)) {
				if (ProtectionEvents.instance.getNPCClasses().contains(owner)) {
					return null;
				}
				return "Owner not a player";
			}

			Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) owner);
			int radius = 0;
			Object grenadeType = fEntityGrenade_type.get(e);
			if (fGrenadeType_fireRadius.getFloat(grenadeType) > 0.1f){
				radius = (int)fGrenadeType_fireRadius.getFloat(grenadeType);
			} else if (fGrenadeType_explosionRadius.getFloat(grenadeType) > 0.1f){
				radius = (int)fGrenadeType_explosionRadius.getFloat(grenadeType);
			}
			int x = (int)e.posX;
			int y = (int)e.posY;
			int z = (int)e.posZ;
			if (!res.canInteract(e.dimension, x - radius, y - radius, y + radius, z + radius, Permissions.Build) ||
					!res.canInteract(e.dimension, x - radius, y - radius, y + radius, z + radius, Permissions.Build) ||
					!res.canInteract(e.dimension, x + radius, y - radius, y + radius, z - radius, Permissions.Build) ||
					!res.canInteract(e.dimension, x + radius, y - radius, y + radius, z + radius, Permissions.Build)) {
				return "Target protected by MyTown";
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
