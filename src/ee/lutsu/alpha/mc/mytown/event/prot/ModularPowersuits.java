package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;

public class ModularPowersuits extends ProtBase {
    public static ModularPowersuits instance = new ModularPowersuits();

    Class<?> clEntityPlasmaBolt, clEntitySpinningBlade, clEntityLuxCapacitor, clItemPowerFist, clMuseItemUtils, clMusePlayerUtils;
    Field fEntityPlasmaBolt_shootingEntity, fEntityPlasmaBolt_explosiveness, fEntityPlasmaBolt_size, fEntitySpinningBlade_shootingEntity;
    Method mItemHasActiveModule, mDoCustomRayTrace;

    @Override
    public void load() throws Exception {
        clEntityPlasmaBolt = Class.forName("net.machinemuse.powersuits.entity.EntityPlasmaBolt");
        fEntityPlasmaBolt_shootingEntity = clEntityPlasmaBolt.getDeclaredField("shootingEntity");
        fEntityPlasmaBolt_explosiveness = clEntityPlasmaBolt.getDeclaredField("explosiveness");
        fEntityPlasmaBolt_size = clEntityPlasmaBolt.getDeclaredField("size");

        clEntitySpinningBlade = Class.forName("net.machinemuse.powersuits.entity.EntitySpinningBlade");
        fEntitySpinningBlade_shootingEntity = clEntitySpinningBlade.getDeclaredField("shootingEntity");

        clEntityLuxCapacitor = Class.forName("net.machinemuse.powersuits.entity.EntityLuxCapacitor");

        clItemPowerFist = Class.forName("net.machinemuse.powersuits.item.ItemPowerFist");
        clMuseItemUtils = Class.forName("net.machinemuse.utils.MuseItemUtils");
        mItemHasActiveModule = clMuseItemUtils.getDeclaredMethod("itemHasActiveModule", ItemStack.class, String.class);

        clMusePlayerUtils = Class.forName("net.machinemuse.utils.MusePlayerUtils");
        mDoCustomRayTrace = clMusePlayerUtils.getDeclaredMethod("doCustomRayTrace", World.class, EntityPlayer.class, boolean.class, double.class);
    }

    @Override
    public boolean loaded() {
        return clEntityPlasmaBolt != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return clEntityPlasmaBolt.isInstance(e) || clEntitySpinningBlade.isInstance(e) || clEntityLuxCapacitor.isInstance(e);
    }

    @Override
    public boolean isEntityInstance(Item e) {
        return clItemPowerFist.isInstance(e);
    }

    @Override
    public String update(Entity e) throws Exception {
        if (e.isDead) {
            return null;
        }

        MovingObjectPosition pos = getThrowableHitOnNextTick((EntityThrowable) e);

        if (pos == null) {
            return null;
        }

        if (clEntityPlasmaBolt.isInstance(e)) {
            Entity shooter = (Entity) fEntityPlasmaBolt_shootingEntity.get(e);
            if (!(shooter instanceof EntityPlayer)) {
                return "Allowed for players only";
            }

            int radius = (int) Math.ceil(fEntityPlasmaBolt_size.getDouble(e) / 50.0D * 3.0D * fEntityPlasmaBolt_explosiveness.getDouble(e)) + 2; // 2
                                                                                                                                                 // for
                                                                                                                                                 // safety
            Resident res = ProtectionEvents.instance.lastOwner = Resident.getOrMake((EntityPlayer) shooter);

            int x1 = (int) e.posX - radius >> 4;
            int z1 = (int) e.posZ - radius >> 4;
            int x2 = (int) e.posX + radius >> 4;
            int z2 = (int) e.posZ + radius >> 4;

            boolean canBlow = true;
            for (int x = x1; x <= x2 && canBlow; x++) {
                for (int z = z1; z <= z2 && canBlow; z++) {
                    if (!res.canInteract(e.dimension, x << 4, (int) e.posY - radius, (int) e.posY + radius, z << 4, Permissions.Build)) {
                        canBlow = false;
                    }
                }
            }

            return canBlow ? null : "No build rights here";
        } else if (clEntitySpinningBlade.isInstance(e)) {
            Entity shooter = (Entity) fEntitySpinningBlade_shootingEntity.get(e);
            if (!(shooter instanceof EntityPlayer)) {
                return "Allowed for players only";
            }

            Resident res = ProtectionEvents.instance.lastOwner = Resident.getOrMake((EntityPlayer) shooter);

            Vec3 var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);
            Vec3 var2 = Vec3.createVectorHelper(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
            MovingObjectPosition var3 = e.worldObj.rayTraceBlocks_do_do(var1, var2, false, true);
            var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);

            if (var3 != null) {
                if (var3.typeOfHit == EnumMovingObjectType.ENTITY && !res.canAttack(var3.entityHit) || var3.typeOfHit == EnumMovingObjectType.TILE && !res.canInteract(var3.blockX, var3.blockY, var3.blockZ, Permissions.Build)) {
                    return "Target in MyTown protected area";
                }
            }
        } else if (clEntityLuxCapacitor.isInstance(e)) {
            EntityThrowable throwable = (EntityThrowable) e;
            Entity shooter = throwable.getThrower();
            if (!(shooter instanceof EntityPlayer)) {
                return "Allowed for players only";
            }

            Resident res = ProtectionEvents.instance.lastOwner = Resident.getOrMake((EntityPlayer) shooter);

            Vec3 var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);
            Vec3 var2 = Vec3.createVectorHelper(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
            MovingObjectPosition var3 = e.worldObj.rayTraceBlocks_do_do(var1, var2, false, true);
            var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);

            if (var3 != null) {
                if (var3.typeOfHit == EnumMovingObjectType.ENTITY && !res.canAttack(var3.entityHit) || var3.typeOfHit == EnumMovingObjectType.TILE && !res.canInteract(var3.blockX, var3.blockY, var3.blockZ, Permissions.Build)) {
                    return "Target in MyTown protected area";
                }
            }
        }

        return null;
    }

    @Override
    public String update(Resident res, Item tool, ItemStack item) throws Exception {
        if (clItemPowerFist.isInstance(tool)) {
            if ((Boolean) mItemHasActiveModule.invoke(null, item, "Railgun")) {
                //Log.warning("Railgun fired by " + res.name());
                MovingObjectPosition hitMOP = (MovingObjectPosition) mDoCustomRayTrace.invoke(null, res.onlinePlayer.worldObj, res.onlinePlayer, true, 64);
                if (hitMOP != null) {
                    switch (hitMOP.typeOfHit) {
                        case ENTITY:
                            if (!res.canAttack(hitMOP.entityHit)) {
                                return "Cannot attack here";
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getMod() {
        return "ModularPowersuits";
    }

    @Override
    public String getComment() {
        return "Permissions check: Build";
    }

    @Override
    public boolean defaultEnabled() {
        return false;
    }

    /**
     * All it does it gets a list of people in the range of something (a spell
     * for instance)
     */
    @SuppressWarnings("unused")
    private List<Entity> getTargets(World world, Vec3 tvec, EntityPlayer p, double range) {
        Entity pointedEntity = null;
        Vec3 vec3d = world.getWorldVec3Pool().getVecFromPool(p.posX, p.posY, p.posZ);
        Vec3 vec3d2 = vec3d.addVector(tvec.xCoord * range, tvec.yCoord * range, tvec.zCoord * range);
        float f1 = 1.0F;
        List<?> list = world.getEntitiesWithinAABBExcludingEntity(p, p.boundingBox.addCoord(tvec.xCoord * range, tvec.yCoord * range, tvec.zCoord * range).expand(f1, f1, f1));

        ArrayList<Entity> l = new ArrayList<Entity>();
        for (int i = 0; i < list.size(); i++) {
            Entity entity = (Entity) list.get(i);
            if (entity.canBeCollidedWith()) {
                float f2 = Math.max(1.0F, entity.getCollisionBorderSize());
                AxisAlignedBB axisalignedbb = entity.boundingBox.expand(f2, f2 * 1.25F, f2);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3d, vec3d2);

                if (movingobjectposition != null) {
                    pointedEntity = entity;

                    if (pointedEntity != null && p.canEntityBeSeen(pointedEntity)) {
                        l.add(pointedEntity);
                    }
                }
            }
        }

        return l;
    }
}
