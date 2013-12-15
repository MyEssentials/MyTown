package ee.lutsu.alpha.mc.mytown.event.prot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

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
    		
        int x = (int) (e.posX + e.motionX);
        int y = (int) (e.posY + e.motionY);
        int z = (int) (e.posZ + e.motionZ);
        int dim = e.dimension;

        if (!canBlow(dim, x - radius, y, z - radius) || !canBlow(dim, x - radius, y, z + radius) || !canBlow(dim, x + radius, y, z - radius)
                || !canBlow(dim, x + radius, y, z + radius)) {
            return "Explosion would hit a protected town";
        }

        return null;
    }

	private boolean canBlow(int dim, int x, int y, int z) {
		TownBlock b = MyTownDatasource.instance.getPermBlockAtCoord(dim, x, y, z);
	
		if (b == null || b.town() == null) {
			return !MyTown.instance.getWorldWildSettings(dim).disableFireBall;
		}
	
		return !b.settings.disableFireBall;
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
