package mytown.event.prot;

import mytown.Utils;
import mytown.event.ProtBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartTNT;

public class MinecartProtection extends ProtBase {
	public static MinecartProtection instance = new MinecartProtection();

	@Override
	public boolean loaded() {
		return true;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return e instanceof EntityMinecart;
	}

	@Override
	public String update(Entity e) throws Exception {
		if (e instanceof EntityMinecartTNT) {
			EntityMinecartTNT cart = (EntityMinecartTNT) e;
			if (e.isDead || !cart.isIgnited()) {
				return null;
			}

			int radius = 13; // 11 + 2 for safety

			if (Utils.canTNTBlow(e.dimension, e.posX - radius, e.posY - radius, e.posY + radius, e.posZ - radius) && Utils.canTNTBlow(e.dimension, e.posX - radius, e.posY - radius, e.posY + radius, e.posZ + radius)
					&& Utils.canTNTBlow(e.dimension, e.posX + radius, e.posY - radius, e.posY + radius, e.posZ - radius) && Utils.canTNTBlow(e.dimension, e.posX + radius, e.posY - radius, e.posY + radius, e.posZ + radius)) {
				return null;
			}

			return "TNT cart explosion disabled here";
		}
		return null;
	}

	@Override
	public String getMod() {
		return "Minecarts";
	}

	@Override
	public String getComment() {
		return "Stops TNT Minecarts from breaking blocks in plots";
	}
}