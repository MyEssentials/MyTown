package mytown.event.prot;

import mytown.MyTownDatasource;
import mytown.Utils;
import mytown.entities.Resident;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

public class TwilightForest extends ProtBase {
    public static TwilightForest instance = new TwilightForest();

    private Class<?> cTFCrumbleHorn, cTFMoonwormShot;

    @Override
    public void load() throws Exception {
        cTFCrumbleHorn = Class.forName("twilightforest.item.ItemTFCrumbleHorn");
        cTFMoonwormShot = Class.forName("twilightforest.entity.EntityTFMoonwormShot");
    }

    /**
     * Check if a tool was used inside a town and sees if the user of the tool
     * is allowed to use it
     */
    @Override
    public String update(Resident res, Item tool, ItemStack item) throws Exception {
        if (cTFCrumbleHorn.isInstance(tool)) {
            MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);
            
            if (!res.canInteract((int)pos.blockX, (int)pos.blockY, (int)pos.blockZ, "build")){
                return "Cannot interact here";
            }

            for (int z = 1; z <= 5; z++) {
                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        if (!res.canInteract((int)pos.blockX+x, (int)pos.blockY+y, (int)pos.blockZ+z, "build")){
                            return "Cannot interact here"; 
                        }
                    }
                }
            }
        }
        return null;
    }

    /*
     * Check if a wormshoot was fired into a town and sees if the launcher of the wormshoot
     * is allowed to use it
     */
    @Override
    public String update(Entity e) throws Exception {
        if (cTFMoonwormShot.isInstance(e)) {
            EntityThrowable t = (EntityThrowable) e;
            EntityLivingBase owner = t.getThrower();

            if (owner == null || !(owner instanceof EntityPlayer)) {
                return "No owner or is not a player";
            }

            Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) owner);

            int x = (int) (t.posX + t.motionX);
            int y = (int) (t.posY + t.motionY);
            int z = (int) (t.posZ + t.motionZ);
            int dim = thrower.onlinePlayer.dimension;

            if (!thrower.canInteract(dim, x, y, z, "build")) {
                return "Worm would hit a protected town";
            }
        }
        return null;
    }

    @Override
    public boolean isEntityInstance(Item e) {
        return cTFCrumbleHorn.isInstance(e);
    }

    @Override
    public boolean loaded() {
        return cTFCrumbleHorn != null;
    }

    @Override
    public String getMod() {
        return "TwilightForest";
    }

    @Override
    public String getComment() {
        return "Blocks Twilight Forest items";
    }

}
