package ee.lutsu.alpha.mc.mytown.event.prot;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Utils;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

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
		
		return (!b.coreSettings.getSetting("gravigun").getValue(Boolean.class)) ? null : "Gravigun is not allowed here";
	}
	
    @Override
    public String update(Resident res, Item tool, ItemStack item) throws Exception {
    	if (clItemGraviGun.isInstance(tool)){
            MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);
            if (pos == null) return null;
            
    		if (pos.typeOfHit != EnumMovingObjectType.TILE) return null;
    		
            if (!res.canInteract((int) pos.blockX, (int) pos.blockY, (int) pos.blockZ, Permissions.Build)){
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