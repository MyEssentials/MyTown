package mytown.event;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.TownSettingCollection.Permissions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.BlockEvent;

public class ExtraEvents {
	@ForgeSubscribe
	public void blockBroken(BlockEvent.BreakEvent event) {
		if (event.isCanceled())
			return;
		EntityPlayer player = event.getPlayer();
		if (player == null)
			return;
		Resident res = MyTownDatasource.instance.getOrMakeResident(player);
		if (res == null)
			return;

		if (!res.canInteract(event.world.provider.dimensionId, event.x, event.y, event.z, Permissions.Build)) {
			MyTown.instance.coreLog.severe("[BlockBreak]Player %s tried to bypass at dim %d, (%d,%d,%d) - Cannot destroy here", res.name(), player.dimension, (int) player.posX, (int) player.posY, (int) player.posZ);
			MyTown.sendChatToPlayer(res.onlinePlayer, "§4You cannot do that here - Cannot destroy here");
			event.setCanceled(true);
		}
	}
}