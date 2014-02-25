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

	private Class<?> clEntityNeedle;
	private Field f_owner;

	@Override
	public void load() throws Exception {
		clEntityNeedle = Class.forName("powercrystals.minefactoryreloaded.entity.EntityNeedle");
		f_owner = clEntityNeedle.getDeclaredField("_owner");
		f_owner.setAccessible(true);
	}

	@Override
	public String update(Entity e) throws Exception {
		if (clEntityNeedle.isInstance(e)) {
			Entity needle = e;
			String owner = (String) f_owner.get(e);
			if (owner == null || owner.trim().isEmpty() || owner.trim() == "") {
				return "No owner";
			}

			Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident(owner);

			int x = (int) (needle.posX + needle.motionX);
			int y = (int) (needle.posY + needle.motionY);
			int z = (int) (needle.posZ + needle.motionZ);
			int dim = e.dimension;

			if (!thrower.canInteract(dim, x, y, z, Permissions.Build)) {
				return "Needle would land in a town";
			}
		}

		return null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return clEntityNeedle.isInstance(e);
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
