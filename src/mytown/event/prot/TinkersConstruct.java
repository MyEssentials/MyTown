package mytown.event.prot;

import java.lang.reflect.Field;

import mytown.ChunkCoord;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Utils;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

public class TinkersConstruct extends ProtBase {
    public static TinkersConstruct instance = new TinkersConstruct();

    private Class<?> clHammer, clExcavator, clBlueSlime, clLaunchedPotion, clRotatingBase, clExplosivePrimed;
    private Field fowner, ftntPlacedBy;
    private int explosionRadius = 7;  //Actually 5, +2 incase

    @Override
    public void load() throws Exception {
        clHammer = Class.forName("tconstruct.items.tools.Hammer");
        clExcavator = Class.forName("tconstruct.items.tools.Excavator");
        clBlueSlime = Class.forName("tconstruct.entity.BlueSlime");
        clLaunchedPotion = Class.forName("tconstruct.entity.projectile.LaunchedPotion");
        
        clExplosivePrimed = Class.forName("tconstruct.entity.item.ExplosivePrimed");
        ftntPlacedBy = clExplosivePrimed.getDeclaredField("tntPlacedBy");
        ftntPlacedBy.setAccessible(true);
        
        clRotatingBase = Class.forName("tconstruct.entity.projectile.RotatingBase");
        fowner = clRotatingBase.getField("owner");
    }

    /**
     * Check if a tool was used inside a town and sees if the user of the tool
     * is allowed to use it
     */
    @Override
    public String update(Resident res, Item tool, ItemStack item) throws Exception {
        if (clHammer.isInstance(tool) || clExcavator.isInstance(tool)) {
            MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);
            if (pos == null) return null;
            
            if (!res.canInteract((int) pos.blockX, (int) pos.blockY, (int) pos.blockZ, "build")){
                return "Cannot interact here";
            }
            
            for (int z = -1; z <= 1; z++) {
                for (int x = -1; x <= 1; x++) {
                    //Log.warning("Checking (%s, %s, %s)", pos2.xCoord + x, pos2.yCoord, pos2.zCoord + z);
                    if (!res.canInteract((int) pos.blockX + x, (int) pos.blockY, (int) pos.blockZ + z, "build")) {
                        return "Cannot interact here";
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public String update(Entity e) throws Exception{
    	if (clBlueSlime.isInstance(e)){
    		EntityLiving mob = (EntityLiving) e;
            if (e.isEntityAlive()) {
                if (!canBe(mob.dimension, mob.posX, mob.posY, mob.posY +1, mob.posZ)) {
                    ProtectionEvents.instance.toRemove.add(e);  // silent removal of the mob
                    return null;
                }
            }
    	} else if(clLaunchedPotion.isInstance(e) || clRotatingBase.isInstance(e)){
    		EntityLivingBase owner = null;
    		
    		if (clLaunchedPotion.isInstance(e)){
        		EntityThrowable t = (EntityThrowable) e;
        		owner = t.getThrower();
    		} else if (clRotatingBase.isInstance(e)){
    			owner = (EntityLivingBase) fowner.get(e);
    		}

            if (owner == null || !(owner instanceof EntityPlayer)) {
                return "No owner or is not a player";
            }

            Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) owner);

            int x = (int) (e.posX + e.motionX);
            int y = (int) (e.posY + e.motionY);
            int z = (int) (e.posZ + e.motionZ);
            int dim = thrower.onlinePlayer.dimension;

            if (!thrower.canInteract(dim, x, y, z, "build")) {
                return (clLaunchedPotion.isInstance(e) ? "Potion" : "Dagger") + " would land in a town";
            }
    	} else if (clExplosivePrimed.isInstance(e)){
    		EntityLivingBase placer = (EntityLivingBase) ftntPlacedBy.get(e);

            int x = (int) e.posX;
            int y = (int) e.posY;
            int z = (int) e.posZ;

            if (placer == null || !(placer instanceof EntityPlayer)) {
            	if (!Utils.canTNTBlow(e.dimension, x-explosionRadius, y-explosionRadius, y+explosionRadius, z-explosionRadius) || !Utils.canTNTBlow(e.dimension, x-explosionRadius, y-explosionRadius, y+explosionRadius, z+explosionRadius) ||
            			!Utils.canTNTBlow(e.dimension, x+explosionRadius, y-explosionRadius, y+explosionRadius, z-explosionRadius) || !Utils.canTNTBlow(e.dimension, x+explosionRadius, y-explosionRadius, y+explosionRadius, z+explosionRadius)){
            		return "TNT explosion disabled here";
            	}
            }

            Resident res = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) placer);
            
            if (!res.canInteract(x - explosionRadius, y - explosionRadius, y + explosionRadius, z - explosionRadius, "build") || !res.canInteract(x - explosionRadius, y - explosionRadius, y + explosionRadius, z + explosionRadius, "build")
                    || !res.canInteract(x + explosionRadius, y - explosionRadius, y + explosionRadius, z - explosionRadius, "build") || !res.canInteract(x + explosionRadius, y - explosionRadius, y + explosionRadius, z + explosionRadius, "build")) {
                return "Explosion would hit a protected town";
            }

            return null;
    	}
    	
    	return null;
    }

    private boolean canBe(int dim, double x, double yFrom, double yTo, double z) {
        TownBlock b = MyTownDatasource.instance.getBlock(dim, ChunkCoord.getCoord(x), ChunkCoord.getCoord(z));
        if (b != null && b.settings.get("core").getSetting("ycheck").getValue(String.class) != null && !b.settings.get("core").getSetting("ycheck").getValue(String.class).isEmpty()) {
        	String[] ycheck = b.settings.get("core").getSetting("ycheck").getValue(String.class).split(",");
        	int yto = Integer.parseInt(ycheck[0]);
        	int yfrom = Integer.parseInt(ycheck[1]);
            if (yTo < yto || yFrom > yfrom) {
                b = b.getFirstFullSidingClockwise(b.town());
            }
        }

        if (b == null || b.town() == null) {
            return !MyTown.instance.getWorldWildSettings(dim).getSetting("mobsoff").getValue(Boolean.class);
        }

        return !b.settings.get("core").getSetting("mobsoff").getValue(Boolean.class);
    }

    @Override
    public boolean isEntityInstance(Item e) {
        return clHammer.isInstance(e) || clExcavator.isInstance(e);
    }
    
    @Override
    public boolean isEntityInstance(Entity e){
    	return clBlueSlime.isInstance(e) || clLaunchedPotion.isInstance(e) || clRotatingBase.isInstance(e) || clExplosivePrimed.isInstance(e);
    }

    @Override
    public boolean loaded() {
        return clHammer != null;
    }

    @Override
    public String getMod() {
        return "Tinkers Construct";
    }

    @Override
    public String getComment() {
        return "Blocks Tinkers Construct tools";
    }

}
