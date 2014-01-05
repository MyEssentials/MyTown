package mytown.event.prot;

import mytown.Utils;
import mytown.event.ProtBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityWitherSkull;

public class FireBall extends ProtBase {
    public static FireBall instance = new FireBall();

    @Override
    public void load() throws Exception {}

    @Override
    public boolean loaded() {
        return true;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return e instanceof EntityFireball || e instanceof EntityWither;
    }

    @Override
    public String update(Entity e) throws Exception {
    	int radius = 0;

    	if (e instanceof EntityWitherSkull || e instanceof EntityLargeFireball) {
    		radius = 1+1;
    	} else if (e instanceof EntityWither && ((EntityWither)e).getDataWatcher().getWatchableObjectInt(20) > 0) {
    		radius = 7 + 2;
    	}
    		
        double x = e.posX + e.motionX;
        double y = e.posY + e.motionY;
        double z = e.posZ + e.motionZ;
        int dim = e.dimension;

        if (!Utils.canBlow(dim, x - radius, y, y, z - radius) || !Utils.canBlow(dim, x - radius, y, y, z + radius) || !Utils.canBlow(dim, x + radius, y, y, z - radius) || !Utils.canBlow(dim, x + radius, y, y, z + radius)) {
            return "Explosion would hit a protected town";
        }

        return null;
    }

    @Override
    public String getMod() {
        return "Vanilla FireBall/Wither Skull";
    }

    @Override
    public String getComment() {
        return "Town permission: disableFireBall";
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }
}
