package mytown.event;

import mytown.MyTown;
import mytown.entities.TownSettingCollection;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent.Unload;

public class WorldEvents {
    public static WorldEvents instance = new WorldEvents();

    @ForgeSubscribe
    public void worldUnload(Unload ev) {
        // So we don't hold the reference and the CG can clean this up
        TownSettingCollection set = MyTown.instance.worldWildSettings.get(ev.world);
        if (set == null) {
            return;
        }

        set.setParent(null);
        MyTown.instance.worldWildSettings.remove(ev.world);
    }
}
