package mytown.events;

import net.minecraft.world.World;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.world.WorldEvent;

@Cancelable
public class FireSpreadEvent extends WorldEvent {
	public int x, y, z;
	
	public FireSpreadEvent(World world, int x, int y, int z) {
		super(world);
		this.x = x;
		this.y = y;
		this.z = z;
	}
}