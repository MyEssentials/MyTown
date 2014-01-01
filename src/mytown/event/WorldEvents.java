package mytown.event;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.entities.SettingCollection;
import mytown.entities.TownBlock;
import mytown.events.FireSpreadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent.Unload;

import com.sperion.forgeperms.Log;

public class WorldEvents {
    public static WorldEvents instance = new WorldEvents();
    
    @ForgeSubscribe
    public void fireSpread(FireSpreadEvent ev){
    	if (ev.isCanceled() || !ev.isCancelable()) return;
		TownBlock b = MyTownDatasource.instance.getPermBlockAtCoord(ev.world.provider.dimensionId, ev.x, ev.y, ev.z);
	
		if (b == null || b.town() == null) {
			if (MyTown.instance.getWorldWildSettings(ev.world.provider.dimensionId).getSetting("fireballoff").getValue(Boolean.class))
				ev.setCanceled(true);
			return;
		} else {
			if (b.coreSettings.getSetting("fireballoff").getValue(Boolean.class))
				ev.setCanceled(true);
		}
		Log.info("%s : (%s, %s, %s) : %s", ev.world, ev.x, ev.y, ev.z, ev.isCanceled());
    }

    @ForgeSubscribe
    public void worldUnload(Unload ev) {
        // So we don't hold the reference and the CG can clean this up
        SettingCollection set = MyTown.instance.worldWildSettings.get(ev.world);
        if (set == null) {
            return;
        }

        set.setParent(null);
        MyTown.instance.worldWildSettings.remove(ev.world);
    }
}
