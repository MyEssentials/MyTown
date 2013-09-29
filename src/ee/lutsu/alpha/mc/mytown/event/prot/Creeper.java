package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

public class Creeper extends ProtBase {
    public static Creeper instance = new Creeper();

    private Field fTimeSinceIgnited, fFuseTime, fExplosionRadius;

    @Override
    public void load() throws Exception {
        // un-obfuscated
        if (EntityCreeper.class.getSimpleName().equals("EntityCreeper")) {
            fTimeSinceIgnited = EntityCreeper.class.getDeclaredField("field_70833_d");
            fFuseTime = EntityCreeper.class.getDeclaredField("field_82225_f");
            fExplosionRadius = EntityCreeper.class.getDeclaredField("field_82226_g");
        }
        /*
         * field_70833_d,timeSinceIgnited,2,The amount of time since the creeper
         * was close enough to the player to ignite field_82225_f,fuseTime,2,
         * field_82226_g,explosionRadius,2,Explosion radius for this creeper.
         * 
         * FD: qc/e net/minecraft/src/EntityCreeper/field_70833_d FD: qc/f
         * net/minecraft/src/EntityCreeper/field_82225_f FD: qc/g
         * net/minecraft/src/EntityCreeper/field_82226_g
         */
        else {
            fTimeSinceIgnited = EntityCreeper.class.getDeclaredField("e");
            fFuseTime = EntityCreeper.class.getDeclaredField("f");
            fExplosionRadius = EntityCreeper.class.getDeclaredField("g");
        }
    }

    @Override
    public boolean loaded() {
        return fExplosionRadius != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return e instanceof EntityCreeper;
    }

    @Override
    public String update(Entity e) throws Exception {
        EntityCreeper creeper = (EntityCreeper) e;
        if (creeper.getCreeperState() < 1) {
            return null;
        }

        fTimeSinceIgnited.setAccessible(true);
        fFuseTime.setAccessible(true);
        fExplosionRadius.setAccessible(true);

        int timeSinceIgnited = fTimeSinceIgnited.getInt(e);
        int fuseTime = fFuseTime.getInt(e);
        int explosionRadius = fExplosionRadius.getInt(e);

        if (e.isEntityAlive()) {
            timeSinceIgnited += creeper.getCreeperState(); // +1 exploding, -1
                                                           // calming down

            if (timeSinceIgnited < 0) {
                timeSinceIgnited = 0;
            }

            if (timeSinceIgnited >= fuseTime) {
                int radius = explosionRadius + (creeper.getPowered() ? explosionRadius : 0) + 2; // 2
                                                                                                 // for
                                                                                                 // safety

                if (canBlow(e.dimension, e.posX - radius, e.posY - radius, e.posY + radius, e.posZ - radius) && canBlow(e.dimension, e.posX - radius, e.posY - radius, e.posY + radius, e.posZ + radius) && canBlow(e.dimension, e.posX + radius, e.posY - radius, e.posY + radius, e.posZ - radius)
                        && canBlow(e.dimension, e.posX + radius, e.posY - radius, e.posY + radius, e.posZ + radius)) {
                    return null;
                }

                creeper.setCreeperState(-1); // this will the pause the fuse,
                                             // making it jump from fuseTime-1
                                             // to fuseTime
                // because the fuse check is before the fuse update

                // return "creeper explosion";
            }
        }

        return null;
    }

    private boolean canBlow(int dim, double x, double yFrom, double yTo, double z) {
        TownBlock b = MyTownDatasource.instance.getBlock(dim, ChunkCoord.getCoord(x), ChunkCoord.getCoord(z));
        if (b != null && b.settings.yCheckOn) {
            if (yTo < b.settings.yCheckFrom || yFrom > b.settings.yCheckTo) {
                b = b.getFirstFullSidingClockwise(b.town());
            }
        }

        if (b == null || b.town() == null) {
            return !MyTown.instance.getWorldWildSettings(dim).disableCreepers;
        }

        return !b.settings.disableCreepers;
    }

    @Override
    public String getMod() {
        return "VanillaCreeper";
    }

    @Override
    public String getComment() {
        return "Town permission: disableCreepers";
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }
}
