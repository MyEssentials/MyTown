package mytown.event.prot;

import mytown.Utils;
import mytown.entities.Resident;
import mytown.entities.TownSettingCollection.Permissions;
import mytown.event.ProtBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

public class Reliquary extends ProtBase {
	public static Reliquary instance = new Reliquary();

	Class<?> clDestructionCatalyst;

	@Override
	public void load() throws Exception {
		clDestructionCatalyst = Class.forName("xreliquary.items.ItemDestructionCatalyst");
	}

	@Override
	public boolean loaded() {
		return clDestructionCatalyst != null;
	}

	@Override
	public boolean isEntityInstance(Item item) {
		return clDestructionCatalyst.isInstance(item);
	}

	@Override
	public String update(Resident res, Item tool, ItemStack item) throws Exception {
		MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);
		if (pos == null)
			return null;

		for (int z = -1; z <= 1; z++) {
			for (int x = -1; x <= 1; x++) {
				if (!res.canInteract(pos.blockX + x, pos.blockY - 1, pos.blockY + 1, pos.blockZ + z, Permissions.Build)) {
					return "Cannot interact here";
				}
			}
		}
		return null;
	}

	@Override
	public String getMod() {
		return "Xeno's Reliquary";
	}

	@Override
	public String getComment() {
		return "Stops Destruction Catalyst";
	}
}