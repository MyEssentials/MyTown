package mytown.event.prot;

import java.lang.reflect.Field;

import mytown.event.ProtBase;
import net.minecraft.entity.Entity;

public class ArsMagica2 extends ProtBase {
	Class<?> clEntitySpellProjectile, clEntityThrownRock, clEntityThrownSickle;
	Field fThrowingEntity_EntityThrownRock, fThrowingEntity_EntityThrownSickle;
	
	@Override
	public void load() throws Exception {
		clEntitySpellProjectile = Class.forName("am2.entities.EntitySpellProjectile");
		clEntityThrownRock = Class.forName("am2.entities.EntityThrownRock");
		fThrowingEntity_EntityThrownRock = clEntityThrownRock.getDeclaredField("throwingEntity");
		clEntityThrownSickle = Class.forName("am2.entities.EntityThrownSickle");
		fThrowingEntity_EntityThrownSickle = clEntityThrownSickle.getDeclaredField("throwingEntity");
	}

	@Override
	public boolean loaded() {
		return clEntitySpellProjectile != null && clEntityThrownRock != null && clEntityThrownSickle != null;
	}
	
	@Override
	public boolean isEntityInstance(Entity e) {
		return clEntitySpellProjectile.isInstance(e) || clEntityThrownRock.isInstance(e) || clEntityThrownSickle.isInstance(e);
	}

	@Override
	public String update(Entity e) throws Exception {
		return null;
	}

	@Override
	public String getMod() {
		return "Ars Magics 2";
	}

	@Override
	public String getComment() {
		return "Protects against Ars Magica 2 Spells";
	}
}