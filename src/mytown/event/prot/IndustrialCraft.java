package mytown.event.prot;

import java.lang.reflect.Field;
import java.util.List;

import mytown.MyTownDatasource;
import mytown.Utils;
import mytown.entities.Resident;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class IndustrialCraft extends ProtBase {
    public static IndustrialCraft instance = new IndustrialCraft();
    // tnts
    Class<?> clDynamite = null, clStickyDynamite, clEntityIC2Explosive;
    Field fFuse1, fFuse2, fExplosivePower;

    // laser
    public int explosionRadius = 6;
    Class<?> clLaser = null;
    Field fTickInAir, fOwner, fExplosive, fRange, fPower, fBlockBreaks;

    @Override
    public void load() throws Exception {
    	
        clDynamite = Class.forName("ic2.core.block.EntityDynamite");
        clStickyDynamite = Class.forName("ic2.core.block.EntityStickyDynamite");
        clEntityIC2Explosive = Class.forName("ic2.core.block.EntityIC2Explosive");
        fExplosivePower = clEntityIC2Explosive.getDeclaredField("explosivePower");

        fFuse1 = clEntityIC2Explosive.getDeclaredField("fuse");
        fFuse2 = clDynamite.getDeclaredField("fuse");

        clLaser = Class.forName("ic2.core.item.tool.EntityMiningLaser");
        fOwner = clLaser.getDeclaredField("owner");
        fTickInAir = clLaser.getDeclaredField("ticksInAir");
        fExplosive = clLaser.getDeclaredField("explosive");
        fRange = clLaser.getDeclaredField("range");
        fPower = clLaser.getDeclaredField("power");
        fBlockBreaks = clLaser.getDeclaredField("blockBreaks");
    }

    @Override
    public boolean loaded() {
        return clDynamite != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return clLaser.isInstance(e) || clDynamite.isInstance(e) || clStickyDynamite.isInstance(e) || clEntityIC2Explosive.isInstance(e);
    }

    @Override
    public String update(Entity e) throws Exception {
        if (e.isDead) {
            return null;
        }

        if (clLaser.isInstance(e)) {
            fTickInAir.setAccessible(true);
            EntityPlayer owner = (EntityPlayer) fOwner.get(e); // actually living
            Integer ticksInAir = (Integer) fTickInAir.get(e);
            Boolean explosive = (Boolean) fExplosive.get(e);
            Float range = (Float) fRange.get(e);
            Float power = (Float) fPower.get(e);
            Integer blockBreaks = (Integer) fBlockBreaks.get(e);

            if (owner == null) {
                return "no owner";
            }

            Resident res = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getOrMakeResident(owner);

            if (range < 1.0F || power <= 0.0F || blockBreaks <= 0) {
                if (explosive) {
                    int x = (int) e.posX, y = (int) e.posY, z = (int) e.posZ;

                    if (!res.canInteract(x - explosionRadius, y - explosionRadius, y + explosionRadius, z - explosionRadius, "build") || !res.canInteract(x - explosionRadius, y - explosionRadius, y + explosionRadius, z + explosionRadius, "build")
                            || !res.canInteract(x + explosionRadius, y - explosionRadius, y + explosionRadius, z - explosionRadius, "build") || !res.canInteract(x + explosionRadius, y - explosionRadius, y + explosionRadius, z + explosionRadius, "build")) {
                        return "Explosion would hit a protected town";
                    }
                }
                return null;
            }

            ticksInAir++;

            Vec3 var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);
            Vec3 var2 = Vec3.createVectorHelper(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
            MovingObjectPosition var3 = e.worldObj.rayTraceBlocks_do_do(var1, var2, false, true);
            var1 = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);

            if (var3 != null) {
                var2 = Vec3.createVectorHelper(var3.hitVec.xCoord, var3.hitVec.yCoord, var3.hitVec.zCoord);
            } else {
                var2 = Vec3.createVectorHelper(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
            }

            Entity var4 = null;
            List<?> var5 = e.worldObj.getEntitiesWithinAABBExcludingEntity(e, e.boundingBox.addCoord(e.motionX, e.motionY, e.motionZ).expand(1.0D, 1.0D, 1.0D));
            double var6 = 0.0D;
            int var8;

            for (var8 = 0; var8 < var5.size(); ++var8) {
                Entity var9 = (Entity) var5.get(var8);

                if (var9.canBeCollidedWith() && (var9 != owner || ticksInAir >= 5)) {
                    float var10 = 0.3F;
                    AxisAlignedBB var11 = var9.boundingBox.expand(var10, var10, var10);
                    MovingObjectPosition var12 = var11.calculateIntercept(var1, var2);

                    if (var12 != null) {
                        double var13 = var1.distanceTo(var12.hitVec);

                        if (var13 < var6 || var6 == 0.0D) {
                            var4 = var9;
                            var6 = var13;
                        }
                    }
                }
            }

            if (var4 != null) {
                var3 = new MovingObjectPosition(var4);
            }

            if (var3 != null) {
                if (var3.typeOfHit == EnumMovingObjectType.ENTITY && !res.canAttack(var3.entityHit) || var3.typeOfHit == EnumMovingObjectType.TILE && !res.canInteract(var3.blockX, var3.blockY, var3.blockZ, "build")) {
                    return "Target in MyTown protected area";
                }

                if (explosive) {
                    // 4 corner check
                    int x, y, z;

                    if (var3.entityHit != null) {
                        x = (int) var3.entityHit.posX;
                        y = (int) var3.entityHit.posY;
                        z = (int) var3.entityHit.posZ;
                    } else {
                        x = var3.blockX;
                        y = var3.blockY;
                        z = var3.blockZ;
                    }

                    if (!res.canInteract(x - explosionRadius, y - explosionRadius, y + explosionRadius, z - explosionRadius, "build") || !res.canInteract(x - explosionRadius, y - explosionRadius, y + explosionRadius, z + explosionRadius, "build")
                            || !res.canInteract(x + explosionRadius, y - explosionRadius, y + explosionRadius, z - explosionRadius, "build") || !res.canInteract(x + explosionRadius, y - explosionRadius, y + explosionRadius, z + explosionRadius, "build")) {
                        return "Explosion would hit a protected town";
                    }
                }
            }

            return null;
        } else {
            int radius = 1;
            int fuse = 0;

            if (clDynamite.isInstance(e) || clStickyDynamite.isInstance(e)) {
                fuse = fFuse2.getInt(e);
            } else {
                fuse = fFuse1.getInt(e);
                radius = (int) Math.ceil(fExplosivePower.getFloat(e));
            }

            if (fuse > 1) {
                return null;
            }

            radius = radius + 2; // 2 for safety

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
    }

    @Override
    public String getMod() {
        return "IndustrialCraft2";
    }

    @Override
    public String getComment() {
        return "Town permission: disableTNT, Build & PVP check: EntityMiningLaser";
    }
}
