package ee.lutsu.alpha.mc.mytown;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum Cost 
{
	HomeTeleport(new ItemStack(Item.diamond, 1), "/home teleport cost"),
	HomeSetNew(new ItemStack(Item.diamond, 10), "/sethome cost for a new unset home"),
	HomeReplace(new ItemStack(Item.diamond, 5), "/sethome cost for an existing home"),
	
	TownSpawnTeleportOwn(null, "/t spawn - usage"),
	TownSpawnTeleportOther(new ItemStack(Item.diamond, 1), "/t spawn townname - usage"),
	TownNew(new ItemStack(Item.diamond, 5), "/t new townname - usage"),
	TownClaimBlock(new ItemStack(Item.diamond, 1), "/t claim - usage per block");

	public static int homeSetNewAdditional = 10;
	
	public ItemStack item;
	public String description;
	
	Cost(ItemStack defItem, String desc)
	{
		item = defItem;
		description = desc;
	}
	
	public static void disable()
	{
		for (Cost c : values())
			c.item = null;
	}
}
