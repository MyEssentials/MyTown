package mytown.event.prot;

import java.lang.reflect.Field;

import mytown.MyTownDatasource;
import mytown.entities.Resident;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;

public class MFR extends ProtBase {
    public static MFR instance = new MFR();

    private Class<?> clEntitySafariNet, clEntityNeedle;
    private Field f_owner;

    @Override
    public void load() throws Exception {
        clEntitySafariNet = Class.forName("powercrystals.minefactoryreloaded.entity.EntitySafariNet");
        clEntityNeedle = Class.forName("powercrystals.minefactoryreloaded.entity.EntityNeedle");
        f_owner = clEntityNeedle.getDeclaredField("_owner");
    }

    @Override
    public String update(Entity e) throws Exception {
        if (clEntitySafariNet.isInstance(e)) {
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

            if (!thrower.canInteract(dim, x, y, z, "roam")) {
                return "SafariNet would land in a town";
            }
        } else if (clEntityNeedle.isInstance(e)){
        	Entity needle = (Entity) e;
        	String owner = (String) f_owner.get(e);
        	if (owner == null || owner.trim().isEmpty() || owner.trim() == ""){
        		return "No owner";
        	}
        	
        	Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident(owner);

            int x = (int) (needle.posX + needle.motionX);
            int y = (int) (needle.posY + needle.motionY);
            int z = (int) (needle.posZ + needle.motionZ);
            int dim = thrower.onlinePlayer.dimension;

            if (!thrower.canInteract(dim, x, y, z, "build")) {
                return "Needle would land in a town";
            }
        }

        return null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return clEntitySafariNet.isInstance(e) || clEntityNeedle.isInstance(e);
    }

    @Override
    public boolean loaded() {
        return clEntitySafariNet != null;
    }

    @Override
    public String getMod() {
        return "MineFactory Reloaded";
    }

    @Override
    public String getComment() {
        return "Stops MineFactory Reloaded items";
    }
}
