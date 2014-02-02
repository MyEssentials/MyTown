package mytown.event.prot;

import mytown.Utils;
import mytown.entities.Resident;
import mytown.entities.TownSettingCollection.Permissions;
import mytown.event.ProtBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

public class TwilightForest extends ProtBase {
	public static TwilightForest instance = new TwilightForest();

	private Class<?> cTFCrumbleHorn;

	@Override
	public void load() throws Exception {
		cTFCrumbleHorn = Class.forName("twilightforest.item.ItemTFCrumbleHorn");
	}

	/**
	 * Check if a tool was used inside a town and sees if the user of the tool
	 * is allowed to use it
	 */
	@Override
	public String update(Resident res, Item tool, ItemStack item) throws Exception {
		if (cTFCrumbleHorn.isInstance(tool)) {
			MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);

			if (!res.canInteract(pos.blockX, pos.blockY, pos.blockZ, Permissions.Build)) {
				return "Cannot interact here";
			}

			for (int z = 1; z <= 5; z++) {
				for (int x = -2; x <= 2; x++) {
					for (int y = -2; y <= 2; y++) {
						if (!res.canInteract(pos.blockX + x, pos.blockY + y, pos.blockZ + z, Permissions.Build)) {
							return "Cannot interact here";
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean isEntityInstance(Item e) {
		return cTFCrumbleHorn.isInstance(e);
	}

	@Override
	public boolean loaded() {
		return cTFCrumbleHorn != null;
	}

	@Override
	public String getMod() {
		return "TwilightForest";
	}

	@Override
	public String getComment() {
		return "Blocks Twilight Forest items";
	}

}
