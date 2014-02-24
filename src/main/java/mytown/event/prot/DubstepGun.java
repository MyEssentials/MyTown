package mytown.event.prot;

import java.lang.reflect.Field;

import mytown.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.TownSettingCollection.Permissions;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Dubstep Gun protection - Stops explosions caused by the Dubstep Gun
 * 
 * @author Joe Goett
 */
public class DubstepGun extends ProtBase {
	public static DubstepGun instance = new DubstepGun();
	Class<?> clEntitySoundCube;
	Field fExplosionRadius, fShootingEntity;

	@Override
	public void load() throws Exception {
		clEntitySoundCube = Class.forName("net.minecraft.saintspack.EntitySoundCube");
		fExplosionRadius = clEntitySoundCube.getDeclaredField("explosionRadius");
		fShootingEntity = clEntitySoundCube.getField("shootingEntity");
	}

	@Override
	public boolean loaded() {
		return clEntitySoundCube != null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return clEntitySoundCube.isInstance(e);
	}

	@Override
	public String update(Entity e) throws Exception {
		EntityLivingBase shooter = (EntityLivingBase) fShootingEntity.get(e);
		if (ProtectionEvents.instance.getNPCClasses().contains(shooter.getClass()))
			return null; // Ignore NPC's
		if (!(shooter instanceof EntityPlayer)) {
			return "shooter not player";
		}
		Resident res = MyTownDatasource.instance.getOrMakeResident((EntityPlayer) shooter);
		float explosionRadius = fExplosionRadius.getFloat(e);
		// 4-corner check
		if (!res.canInteract(e.dimension, (int) e.posX, (int) e.posY, (int) e.posZ, Permissions.Build) || !res.canInteract(e.dimension, (int) (e.posX - explosionRadius), (int) (e.posY - explosionRadius), (int) (e.posY + explosionRadius), (int) (e.posZ - explosionRadius), Permissions.Build)
				|| !res.canInteract(e.dimension, (int) (e.posX - explosionRadius), (int) (e.posY - explosionRadius), (int) (e.posY + explosionRadius), (int) (e.posZ + explosionRadius), Permissions.Build)
				|| !res.canInteract(e.dimension, (int) (e.posX + explosionRadius), (int) (e.posY - explosionRadius), (int) (e.posY + explosionRadius), (int) (e.posZ + explosionRadius), Permissions.Build)
				|| !res.canInteract(e.dimension, (int) (e.posX + explosionRadius), (int) (e.posY - explosionRadius), (int) (e.posY + explosionRadius), (int) (e.posZ - explosionRadius), Permissions.Build)) {
			return "Explosion would hit a protected town";
		}
		return null;
	}

	@Override
	public String getMod() {
		return "Dubstep Gun";
	}

	@Override
	public String getComment() {
		return "Stop explosions from Dubstep Gun";
	}

}
