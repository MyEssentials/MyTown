package ee.lutsu.alpha.mc.mytown.events;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class MyTownEventBus {
	public static boolean fireSpread(World world, int x, int y, int z){
		return MinecraftForge.EVENT_BUS.post(new FireSpreadEvent(world, x, y, z));
	}
}