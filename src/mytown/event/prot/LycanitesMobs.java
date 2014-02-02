package mytown.event.prot;

import java.lang.reflect.Method;

import mytown.ChunkCoord;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.entities.TownBlock;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

public class LycanitesMobs extends ProtBase {
	Class<?> clEntityCreatureTameable;
	Method mIsTamed;

	@Override
	public void load() throws Exception {
		clEntityCreatureTameable = Class.forName("lycanite.lycanitesmobs.entity.EntityCreatureTameable");
		mIsTamed = clEntityCreatureTameable.getMethod("isTamed");
	}

	@Override
	public boolean loaded() {
		return clEntityCreatureTameable != null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return clEntityCreatureTameable.isInstance(e);
	}

	@Override
	public String update(Entity e) throws Exception {
		if ((boolean) mIsTamed.invoke(e))
			return null;
		if ((int) e.posX == (int) e.prevPosX && (int) e.posY == (int) e.prevPosY && (int) e.posZ == (int) e.prevPosZ) {
			return null;
		}

		EntityLiving mob = (EntityLiving) e;

		if (e.isEntityAlive()) {
			if (!canBe(mob.dimension, mob.posX, mob.posY, mob.posY + 1, mob.posZ)) {
				// silent removal of the mob
				ProtectionEvents.instance.toRemove.add(e);
			}
		}

		return null;
	}

	private boolean canBe(int dim, double x, double yFrom, double yTo, double z) {
		TownBlock b = MyTownDatasource.instance.getBlock(dim, ChunkCoord.getCoord(x), ChunkCoord.getCoord(z));
		if (b != null && b.settings.yCheckOn) {
			if (yTo < b.settings.yCheckFrom || yFrom > b.settings.yCheckTo) {
				b = b.getFirstFullSidingClockwise(b.town());
			}
		}

		if (b == null || b.town() == null) {
			return !MyTown.instance.getWorldWildSettings(dim).disableMobs;
		}

		return !b.settings.disableMobs;
	}

	@Override
	public String getMod() {
		return "Lycanites Mobs";
	}

	@Override
	public String getComment() {
		return "Stops non-tamed mobs from entering towns";
	}
}