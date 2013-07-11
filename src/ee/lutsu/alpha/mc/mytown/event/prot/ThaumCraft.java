package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Utils;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;

public class ThaumCraft extends ProtBase {
    public static ThaumCraft instance = new ThaumCraft();
    public int explosionRadius = 6;

    private Class clAlumentum = null, clTileArcaneBore, clEntityFrostShard,
            clItemWandFire, clItemWandExcavation, clItemWandLightning,
            clItemWandTrade;
    Field fBore_toDig, fBore_digX, fBore_digZ, fBore_digY;
    Field fFrostShard_shootingEntity;

    // boolean toDig = false; int digX = 0; int digZ = 0; int digY = 0;

    @Override
    public void load() throws Exception {
        clEntityFrostShard = Class
                .forName("thaumcraft.common.entities.projectile.EntityFrostShard");
        clAlumentum = Class
                .forName("thaumcraft.common.entities.projectile.EntityAlumentum");

        clItemWandExcavation = Class
                .forName("thaumcraft.common.items.wands.ItemWandExcavation");
        clItemWandFire = Class
                .forName("thaumcraft.common.items.wands.ItemWandFire");
        clItemWandLightning = Class
                .forName("thaumcraft.common.items.wands.ItemWandLightning");
        clItemWandTrade = Class
                .forName("thaumcraft.common.items.wands.ItemWandTrade");

        clTileArcaneBore = Class
                .forName("thaumcraft.common.tiles.TileArcaneBore");
        fBore_toDig = clTileArcaneBore.getDeclaredField("toDig");
        fBore_digX = clTileArcaneBore.getDeclaredField("digX");
        fBore_digY = clTileArcaneBore.getDeclaredField("digY");
        fBore_digZ = clTileArcaneBore.getDeclaredField("digZ");

        fFrostShard_shootingEntity = clEntityFrostShard
                .getDeclaredField("shootingEntity");
    }

    @Override
    public boolean loaded() {
        return clAlumentum != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return clAlumentum.isInstance(e) || clEntityFrostShard.isInstance(e);
    }

    @Override
    public boolean isEntityInstance(TileEntity e) {
        return e.getClass() == clTileArcaneBore;
    }

    @Override
    public boolean isEntityInstance(Item e) {
        return clItemWandExcavation.isInstance(e)
                || clItemWandFire.isInstance(e)
                || clItemWandLightning.isInstance(e)
                || clItemWandTrade.isInstance(e);
    }

    @Override
    public String update(Entity e) throws Exception {
        if (clAlumentum.isInstance(e)) {
            EntityThrowable t = (EntityThrowable) e;
            EntityLiving owner = t.getThrower();

            if (owner == null || !(owner instanceof EntityPlayer)) {
                return "No owner or is not a player";
            }

            Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance
                    .getResident((EntityPlayer) owner);

            int x = (int) (t.posX + t.motionX);
            int y = (int) (t.posY + t.motionY);
            int z = (int) (t.posZ + t.motionZ);
            int dim = thrower.onlinePlayer.dimension;

            if (!thrower.canInteract(dim, x - explosionRadius, y, z
                    - explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x - explosionRadius, y, z
                            + explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x + explosionRadius, y, z
                            - explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x + explosionRadius, y, z
                            + explosionRadius, Permissions.Build)) {
                return "Explosion would hit a protected town";
            }
        } else if (clEntityFrostShard.isInstance(e)) {
            Entity shooter = (Entity) fFrostShard_shootingEntity.get(e);

            if (shooter == null || !(shooter instanceof EntityPlayer)) {
                return "No owner";
            }

            Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance
                    .getResident((EntityPlayer) shooter);

            int x = (int) (e.posX + e.motionX);
            int y = (int) (e.posY + e.motionY);
            int z = (int) (e.posZ + e.motionZ);
            int radius = 1;
            int dim = thrower.onlinePlayer.dimension;

            if (!thrower.canInteract(dim, x - radius, y - radius, y + radius, z
                    - radius, Permissions.Build)
                    || !thrower.canInteract(dim, x - radius, y - radius, y
                            + radius, z + radius, Permissions.Build)
                    || !thrower.canInteract(dim, x + radius, y - radius, y
                            + radius, z - radius, Permissions.Build)
                    || !thrower.canInteract(dim, x + radius, y - radius, y
                            + radius, z + radius, Permissions.Build)) {
                return "Cannot build here";
            }
        }

        return null;
    }

    @Override
    public String update(Resident res, Item tool, ItemStack item)
            throws Exception {
        if (clItemWandFire.isInstance(tool)) {
            List<Entity> list = getTargets(res.onlinePlayer.worldObj,
                    res.onlinePlayer.getLook(17), res.onlinePlayer, 17);
            for (Entity e : list) {
                if (!res.canAttack(e)) {
                    return "Cannot attack here";
                }
            }
        } else if (clItemWandLightning.isInstance(tool)) {
            List<Entity> list = getTargets(res.onlinePlayer.worldObj,
                    res.onlinePlayer.getLook(20), res.onlinePlayer, 20);
            for (Entity e : list) {
                if (!res.canAttack(e)) {
                    return "Cannot attack here";
                }
            }
        } else if (clItemWandExcavation.isInstance(tool)) {
            MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(
                    res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);

            if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
                if (!res.canInteract(res.onlinePlayer.dimension, pos.blockX,
                        pos.blockY, pos.blockZ, Permissions.Build)) {
                    return "Cannot build here";
                }
            }
        } else if (clItemWandTrade.isInstance(tool)) {
            if (!res.onlinePlayer.isSneaking()) {
                MovingObjectPosition pos = Utils
                        .getMovingObjectPositionFromPlayer(
                                res.onlinePlayer.worldObj, res.onlinePlayer,
                                false, 10.0D);

                if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
                    int x = pos.blockX;
                    int y = pos.blockY;
                    int z = pos.blockZ;
                    int radius = 3;
                    int dim = res.onlinePlayer.dimension;

                    if (!res.canInteract(dim, x - radius, y - radius, y
                            + radius, z - radius, Permissions.Build)
                            || !res.canInteract(dim, x - radius, y - radius, y
                                    + radius, z + radius, Permissions.Build)
                            || !res.canInteract(dim, x + radius, y - radius, y
                                    + radius, z - radius, Permissions.Build)
                            || !res.canInteract(dim, x + radius, y - radius, y
                                    + radius, z + radius, Permissions.Build)) {
                        return "Cannot build here";
                    }
                }
            }
        }

        return null;
    }

    private List<Entity> getTargets(World world, Vec3 tvec, EntityPlayer p,
            double range) {
        Entity pointedEntity = null;
        Vec3 vec3d = world.getWorldVec3Pool().getVecFromPool(p.posX, p.posY,
                p.posZ);
        Vec3 vec3d2 = vec3d.addVector(tvec.xCoord * range, tvec.yCoord * range,
                tvec.zCoord * range);
        float f1 = 1.0F;
        List list = world.getEntitiesWithinAABBExcludingEntity(
                p,
                p.boundingBox.addCoord(tvec.xCoord * range,
                        tvec.yCoord * range, tvec.zCoord * range).expand(f1,
                        f1, f1));

        ArrayList<Entity> l = new ArrayList<Entity>();
        for (int i = 0; i < list.size(); i++) {
            Entity entity = (Entity) list.get(i);
            if (entity.canBeCollidedWith()) {
                float f2 = Math.max(1.0F, entity.getCollisionBorderSize());
                AxisAlignedBB axisalignedbb = entity.boundingBox.expand(f2,
                        f2 * 1.25F, f2);
                MovingObjectPosition movingobjectposition = axisalignedbb
                        .calculateIntercept(vec3d, vec3d2);

                if (movingobjectposition != null) {
                    pointedEntity = entity;

                    if (pointedEntity != null
                            && p.canEntityBeSeen(pointedEntity)) {
                        l.add(pointedEntity);
                    }
                }
            }
        }

        return l;
    }

    @Override
    public String update(TileEntity e) throws Exception {
        if (clTileArcaneBore.isInstance(e)) {
            fBore_toDig.setAccessible(true);
            fBore_digX.setAccessible(true);
            fBore_digY.setAccessible(true);
            fBore_digZ.setAccessible(true);

            if (fBore_toDig.getBoolean(e)) {
                Resident actor = getActorFromLocation(
                        e.worldObj.provider.dimensionId, e.xCoord, e.yCoord,
                        e.zCoord, "#thaumcraft-bore#");
                if (!actor.canInteract(e.worldObj.provider.dimensionId,
                        fBore_digX.getInt(e), fBore_digY.getInt(e),
                        fBore_digZ.getInt(e), Permissions.Build)) {
                    Log.warning(String
                            .format("Thaumcraft bore at Dim %s (%s,%s,%s) tried to break (%s,%s,%s) which failed. Actor: %s",
                                    e.worldObj.provider.dimensionId, e.xCoord,
                                    e.yCoord, e.zCoord, fBore_digX.getInt(e),
                                    fBore_digY.getInt(e), fBore_digZ.getInt(e),
                                    actor.name()));

                    fBore_toDig.set(e, false);
                }
            }
        }

        return null;
    }

    @Override
    public String getMod() {
        return "ThaumCraft";
    }

    @Override
    public String getComment() {
        return "Build check: EntityAlumentum & ItemWandExcavation";
    }
}
