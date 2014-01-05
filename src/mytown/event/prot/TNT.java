package mytown.event.prot;

import mytown.Utils;
import mytown.event.ProtBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;

public class TNT extends ProtBase {
    public static TNT instance = new TNT();

    public int explosionRadius = 4;

    @Override
    public void load() throws Exception {}

    @Override
    public boolean loaded() {
        return true;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return e instanceof EntityTNTPrimed;
    }

    @Override
    public String update(Entity e) throws Exception {
        EntityTNTPrimed tnt = (EntityTNTPrimed) e;
        
        if (tnt.isDead || tnt.fuse > 1) {
            return null;
        }

        int radius = explosionRadius + 2; // 2 for safety

        int x1 = (int) e.posX - radius >> 4;
        int z1 = (int) e.posZ - radius >> 4;
        int x2 = (int) e.posX + radius >> 4;
        int z2 = (int) e.posZ + radius >> 4;

        boolean canBlow = true;
        for (int x = x1; x <= x2 && canBlow; x++) {
            for (int z = z1; z <= z2 && canBlow; z++) {
                if (!Utils.canBlow(e.dimension, x << 4, (int) e.posY - radius, (int) e.posY + radius, z << 4)) {
                    canBlow = false;
                }
            }
        }

        return canBlow ? null : "TNT explosion disabled here";
    }

    @Override
    public String getMod() {
        return "VanillaTNT";
    }

    @Override
    public String getComment() {
        return "Town permission: disableTNT";
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }
}
