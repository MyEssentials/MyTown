package ee.lutsu.alpha.mc.mytown.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.BlockEvent;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;

public class ExtraEvents {
	@ForgeSubscribe(priority=EventPriority.HIGHEST)
    public void blockBroken(BlockEvent.BreakEvent event){
	    if (event.isCanceled()) return;
	    EntityPlayer player = event.getPlayer();
	    if (player == null) return;
	    Resident res = MyTownDatasource.instance.getOrMakeResident(player);
	    if (res == null) return;
	    
	    if (!res.canInteract(event.x, event.y, event.z, Permissions.Build)){
	        Log.severe("[BlockBreak]Player %s tried to bypass at dim %d, (%s,%s,%s) - Cannot destroy here", res.name(), player.dimension, (int)player.posX, (int)player.posY, (int)player.posZ);
	        MyTown.sendChatToPlayer(res.onlinePlayer, "§4You cannot do that here - Cannot destroy here");
	        event.setCanceled(true);
	    }
	}
}