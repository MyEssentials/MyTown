package mytown.event.prot;

import java.lang.reflect.Field;

import mytown.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.TownSettingCollection.Permissions;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;

public class MFR extends ProtBase {
	public static MFR instance = new MFR();

	private Class<?> clEntityNeedle, clEntityRocket;
	private Field fEntityNeedle_owner, fEntityRocket_owner, fEntityRocket_target;

	@Override
	public void load() throws Exception {
		clEntityNeedle = Class.forName("powercrystals.minefactoryreloaded.entity.EntityNeedle");
		fEntityNeedle_owner = clEntityNeedle.getDeclaredField("_owner");
		fEntityNeedle_owner.setAccessible(true);
		
		clEntityRocket = Class.forName("powercrystals.minefactoryreloaded.entity.EntityRocket");
		fEntityRocket_owner = clEntityRocket.getDeclaredField("_owner");
		fEntityRocket_owner.setAccessible(true);
		fEntityRocket_target = clEntityRocket.getDeclaredField("_target");
		fEntityRocket_target.setAccessible(true);
	}

	@Override
	public String update(Entity e) throws Exception {
		if (clEntityNeedle.isInstance(e)) {
			String owner = (String) fEntityNeedle_owner.get(e);
			if (owner == null || owner.trim().isEmpty() || owner.trim() == "") {
				return "No owner";
			}

			Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getOrMakeResident(owner);

			int x = (int) (e.posX + e.motionX);
			int y = (int) (e.posY + e.motionY);
			int z = (int) (e.posZ + e.motionZ);
			int dim = e.dimension;

			if (!thrower.canInteract(dim, x, y, z, Permissions.Build)) {
				return "Needle would land in a town";
			}
		} else if (clEntityRocket.isInstance(e)){
			String owner = (String) fEntityRocket_owner.get(e);
			Entity target = (Entity) fEntityRocket_target.get(e);
			
			Resident res = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getOrMakeResident(owner);
			if (target != null && !res.canAttack(target)){
				return "Can't attack target";
			} else {
				
				int explosionRadius = 4;
				int x = (int) e.posX, y = (int) e.posY, z = (int) e.posZ;
				if (!res.canInteract(e.dimension, x - explosionRadius, y - explosionRadius, y + explosionRadius, z - explosionRadius, Permissions.Build) || !res.canInteract(e.dimension, x - explosionRadius, y - explosionRadius, y + explosionRadius, z + explosionRadius, Permissions.Build)
						|| !res.canInteract(e.dimension, x + explosionRadius, y - explosionRadius, y + explosionRadius, z - explosionRadius, Permissions.Build) || !res.canInteract(e.dimension, x + explosionRadius, y - explosionRadius, y + explosionRadius, z + explosionRadius, Permissions.Build)) {
					return "Explosion would hit a protected town";
				}
			}
		}

		return null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return clEntityNeedle.isInstance(e) || clEntityRocket.isInstance(e);
	}

	@Override
	public boolean loaded() {
		return clEntityNeedle != null;
	}

	@Override
	public String getMod() {
		return "MineFactory Reloaded";
	}

	@Override
	public String getComment() {
		return "Stops MineFactory Reloaded items";
	}
}
