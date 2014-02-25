package mytown.event.prot;

import java.util.List;

import mytown.MyTown;
import mytown.event.ProtBase;
import net.minecraft.entity.Entity;

public class CustomNPCs extends ProtBase {
	public static CustomNPCs instance = new CustomNPCs();
	Class<?> clEntityNPCInterface;
	public static boolean debug = false;

	@Override
	public void load() throws Exception {
		clEntityNPCInterface = Class.forName("noppes.npcs.EntityNPCInterface");
	}

	@Override
	public boolean loaded() {
		return clEntityNPCInterface != null;
	}

	@Override
	public boolean canAttackMob(Entity e) {
		return clEntityNPCInterface.isInstance(e);
	}

	@Override
	public String getMod() {
		return "Custom NPCs";
	}

	@Override
	public String getComment() {
		return "Identifies special cases for Custom NPCs";
	}

	@SuppressWarnings("rawtypes")
	public static void addNPCClasses(List<Class> list) throws Exception {
		addSub(list, "org.millenaire.common.MillVillager");
		addSub(list, "noppes.npcs.EntityNPCInterface");
	}

	@SuppressWarnings("rawtypes")
	private static void addSub(List<Class> list, String name) {
		try {
			list.add(Class.forName(name));
		} catch (Throwable t) {
			if (debug) {
				MyTown.instance.coreLog.warning(String.format("Cannot load %s for Custom NPCs", name));
			}
		}
	}
}