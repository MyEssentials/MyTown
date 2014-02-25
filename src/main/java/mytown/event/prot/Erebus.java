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

public class Erebus extends ProtBase {
	public static Erebus instance = new Erebus();
	Class<?> clEntityWebSling;

	@Override
	public void load() throws Exception {
		clEntityWebSling = Class.forName("erebus.entity.EntityWebSling");
	}

	@Override
	public boolean loaded() {
		return clEntityWebSling != null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return clEntityWebSling.isInstance(e);
	}

	@Override
	public String update(Entity e) throws Exception {
		EntityThrowable t = (EntityThrowable) e;
		EntityLivingBase owner = t.getThrower();

		if (owner == null || !(owner instanceof EntityPlayer)) {
			return null;
		}

		Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) owner);

		int radius = 2;
		int x = (int) (t.posX + t.motionX);
		int y = (int) (t.posY + t.motionY);
		int z = (int) (t.posZ + t.motionZ);
		int dim = thrower.onlinePlayer.dimension;

		if (!thrower.canInteract(dim, x - radius, y, z - radius, Permissions.Build) || !thrower.canInteract(dim, x - radius, y, z + radius, Permissions.Build) ||
				!thrower.canInteract(dim, x + radius, y, z - radius, Permissions.Build)	|| !thrower.canInteract(dim, x + radius, y, z + radius, Permissions.Build)) {
			return "Web would hit a protected town";
		}
		return null;
	}

	@Override
	public String getMod() {
		return "Erebus";
	}

	@Override
	public String getComment() {
		return "Prevents Erebus Web Slinger from placing web in protected areas.";
	}
}