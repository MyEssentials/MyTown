package mytown.event.prot;

import java.lang.reflect.Method;

import mytown.event.ProtBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;

public class LycanitesMobs extends ProtBase {
	public static LycanitesMobs instance = new LycanitesMobs();
	Class<?> clEntityCreatureBase;
	Class<?> clEntityCreatureTameable;
	Class<?> clIGroupAnimal;
	Method mIsTamed;

	@Override
	public void load() throws Exception {
		clEntityCreatureBase = Class.forName("lycanite.lycanitesmobs.api.entity.EntityCreatureBase");
		clEntityCreatureTameable = Class.forName("lycanite.lycanitesmobs.api.entity.EntityCreatureTameable");
		clIGroupAnimal = Class.forName("lycanite.lycanitesmobs.api.IGroupAnimal");
		mIsTamed = clEntityCreatureTameable.getMethod("isTamed");
	}

	@Override
	public boolean loaded() {
		return clEntityCreatureBase != null;
	}

	@Override
	public boolean isHostileMob(Entity e) {
		if (clEntityCreatureBase.isInstance(e)) {
			try {
				if (clEntityCreatureTameable.isInstance(e)) {
					return !((Boolean) mIsTamed.invoke(e));
				}
				return !clIGroupAnimal.isInstance(e);
			} catch (Throwable t) {
			}
		}
		return false;
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