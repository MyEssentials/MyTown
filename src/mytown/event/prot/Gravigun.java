package mytown.event.prot;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Utils;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.event.ProtBase;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;

public class Gravigun extends ProtBase {
	public static Gravigun instance = new Gravigun();
	
	Class<?> clEntityBlock, clItemGraviGun;
	
	@Override
	public void load() throws Exception{
		clEntityBlock = Class.forName("gravigun.common.entity.EntityBlock");
		clItemGraviGun = Class.forName("gravigun.common.item.ItemGraviGun");
	}
	
	@Override
	public boolean loaded(){
		return clEntityBlock != null && clItemGraviGun != null;
	}
	
	@Override
	public String update(Entity e){
		TownBlock b = MyTownDatasource.instance.getPermBlockAtCoord(e.dimension, (int)e.posX, (int)e.posY, (int)e.posZ);
		if (b == null || b.town() == null){
			return (!MyTown.instance.getWorldWildSettings(e.dimension).getSetting("gravigun").getValue(Boolean.class)) ? null : "Gravigun is not allowed here";
		}
		
		return (!b.settings.get("core").getSetting("gravigun").getValue(Boolean.class)) ? null : "Gravigun is not allowed here";
	}
	
    @Override
    public String update(Resident res, Item tool, ItemStack item) throws Exception {
    	if (clItemGraviGun.isInstance(tool)){
            MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);
            if (pos == null) return null;
            
    		if (pos.typeOfHit != EnumMovingObjectType.TILE) return null;
    		
            if (!res.canInteract((int) pos.blockX, (int) pos.blockY, (int) pos.blockZ, "build")){
                return "Cannot build here";
            }
    	}
    	
    	return null;
    }
	
//	@Override
//	public boolean isEntityInstance(Item tool){
//		return clItemGraviGun.isInstance(tool);
//	}
	
	@Override
	public boolean isEntityInstance(Entity e){
		return clEntityBlock.isInstance(e);
	}
	
	@Override
	public String getMod() {
		return "Gravigun";
	}

	@Override
	public String getComment() {
		return "Build Check: Stop flying blocks!";
	}
}