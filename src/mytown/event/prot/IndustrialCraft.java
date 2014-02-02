package mytown.event.prot;

import java.lang.reflect.Field;

import mytown.Utils;
import mytown.event.ProtBase;
import net.minecraft.entity.Entity;

public class IndustrialCraft extends ProtBase {
	public static IndustrialCraft instance = new IndustrialCraft();

	// tnts
	Class<?> clDynamite = null, clStickyDynamite, clEntityIC2Explosive;
	Field fFuse1, fFuse2, fExplosivePower;

	@Override
	public void load() throws Exception {

		clDynamite = Class.forName("ic2.core.block.EntityDynamite");
		clStickyDynamite = Class.forName("ic2.core.block.EntityStickyDynamite");
		clEntityIC2Explosive = Class.forName("ic2.core.block.EntityIC2Explosive");
		fExplosivePower = clEntityIC2Explosive.getDeclaredField("explosivePower");

		fFuse1 = clEntityIC2Explosive.getDeclaredField("fuse");
		fFuse2 = clDynamite.getDeclaredField("fuse");
	}

	@Override
	public boolean loaded() {
		return clDynamite != null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return /* clLaser.isInstance(e) || */clDynamite.isInstance(e) || clStickyDynamite.isInstance(e) || clEntityIC2Explosive.isInstance(e);
	}

	@Override
	public String update(Entity e) throws Exception {
		if (e.isDead) {
			return null;
		}
		int radius = 1;
		int fuse = 0;

		if (clDynamite.isInstance(e) || clStickyDynamite.isInstance(e)) {
			fuse = fFuse2.getInt(e);
		} else {
			fuse = fFuse1.getInt(e);
			radius = (int) Math.ceil(fExplosivePower.getFloat(e));
		}

		if (fuse > 1) {
			return null;
		}

		radius = radius + 2; // 2 for safety

		int x1 = (int) e.posX - radius >> 4;
		int z1 = (int) e.posZ - radius >> 4;
		int x2 = (int) e.posX + radius >> 4;
		int z2 = (int) e.posZ + radius >> 4;

		boolean canBlow = true;
		for (int x = x1; x <= x2 && canBlow; x++) {
			for (int z = z1; z <= z2 && canBlow; z++) {
				if (!Utils.canTNTBlow(e.dimension, x << 4, (int) e.posY - radius, (int) e.posY + radius, z << 4)) {
					canBlow = false;
				}
			}
		}

		return canBlow ? null : "TNT explosion disabled here";
	}

	@Override
	public String getMod() {
		return "IndustrialCraft2";
	}

	@Override
	public String getComment() {
		return "Town permission: disableTNT, Build & PVP check: EntityMiningLaser";
	}
}
