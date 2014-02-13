package mytown.event.prot;

import java.lang.reflect.Method;

import mytown.event.ProtBase;
import net.minecraft.entity.Entity;

public class LycanitesMobs extends ProtBase {
	public static LycanitesMobs instance = new LycanitesMobs();
	Class<?> clEntityCreatureBase;
	Class<?> clEntityCreatureTameable;
	Method mIsTamed;

	@Override
	public void load() throws Exception {
		clEntityCreatureBase = Class.forName("lycanite.lycanitesmobs.entity.EntityCreatureBase");
		clEntityCreatureTameable = Class.forName("lycanite.lycanitesmobs.entity.EntityCreatureTameable");
		mIsTamed = clEntityCreatureTameable.getMethod("isTamed");
	}

	@Override
	public boolean loaded() {
		return clEntityCreatureBase != null;
	}

	@Override
	public boolean isHostileMob(Entity e) {
		if (clEntityCreatureTameable.isInstance(e)) {
			try {
				if ((Boolean) mIsTamed.invoke(e))
					return false;
			} catch (Throwable t) {
			}
		}
		return clEntityCreatureBase.isInstance(e);
	}

	@Override
	public String getMod() {
		return "Lycanites Mobs";
	}

	@Override
	public String getComment() {
		return "Stops Lycanite mobs from spawning in towns";
	}
}