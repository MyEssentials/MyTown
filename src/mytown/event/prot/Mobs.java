package mytown.event.prot;

import mytown.ChunkCoord;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.entities.TownBlock;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class Mobs extends ProtBase {
    public static Mobs instance = new Mobs();

    private boolean loaded = false;

    @Override
    public void load() throws Exception {
        MinecraftForge.EVENT_BUS.register(this);
        loaded = true;
    }

    @Override
    public boolean loaded() {
        return loaded;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return e instanceof EntityMob || e instanceof EntitySlime;
    }

    @Override
    public String update(Entity e) throws Exception {
        if ((int) e.posX == (int) e.prevPosX && (int) e.posY == (int) e.prevPosY && (int) e.posZ == (int) e.prevPosZ) {
            return null;
        }
        
        EntityLiving mob = (EntityLiving)e;
        
        if (e.isEntityAlive()) {
            if (!canBe(mob.dimension, mob.posX, mob.posY, mob.posY + 1, mob.posZ)) {
                // silent removal of the mob
                ProtectionEvents.instance.toRemove.add(e);
                return null;
            }
        }

        return null;
    }

    @ForgeSubscribe
    public void entityJoinWorld(EntityJoinWorldEvent ev) {
        if (!isEntityInstance(ev.entity)) {
            return;
        }
        
        EntityLiving mob = (EntityLiving) ev.entity;
        if (!canBe(mob.dimension, mob.posX, mob.posY, mob.posY + 1, mob.posZ)) {
            ev.setCanceled(true);
        }
    }

    private boolean canBe(int dim, double x, double yFrom, double yTo, double z) {
        TownBlock b = MyTownDatasource.instance.getBlock(dim, ChunkCoord.getCoord(x), ChunkCoord.getCoord(z));
        if (b != null && b.settings.get("core").getSetting("yon").getValue(Boolean.class)) {
            if (yTo < b.settings.get("core").getSetting("yfrom").getValue(Integer.class) || yFrom > b.settings.get("core").getSetting("yto").getValue(Integer.class)) {
                b = b.getFirstFullSidingClockwise(b.town());
            }
        }

        if (b == null || b.town() == null) {
            return !MyTown.instance.getWorldWildSettings(dim).getSetting("mobsoff").getValue(Boolean.class);
        }

        return !b.settings.get("core").getSetting("mobsoff").getValue(Boolean.class);
    }

    @Override
    public String getMod() {
        return "VanillaMobLocation";
    }

    @Override
    public String getComment() {
        return "Town permission: disableMobs";
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }
}
