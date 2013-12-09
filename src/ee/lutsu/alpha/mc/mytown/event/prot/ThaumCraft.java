package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
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

    private Class<?> clAlumentum = null, clTileArcaneBore, clEntityFrostShard, clItemWandCasting, clEntityPechBlast;
    private Method mGetFocus, mGetFocusPotency;
    private Field fBore_toDig, fBore_digX, fBore_digZ, fBore_digY, fFrostShard_shootingEntity;

    @Override
    public void load() throws Exception {
        clItemWandCasting = Class.forName("thaumcraft.common.items.wands.ItemWandCasting");
        mGetFocus = clItemWandCasting.getDeclaredMethod("getFocus", ItemStack.class);
        mGetFocusPotency = clItemWandCasting.getDeclaredMethod("getFocusPotency", ItemStack.class);
        
        clEntityFrostShard = Class.forName("thaumcraft.common.entities.projectile.EntityFrostShard");
        clEntityPechBlast = Class.forName("thaumcraft.common.entities.projectile.EntityPechBlast");
        clAlumentum = Class.forName("thaumcraft.common.entities.projectile.EntityAlumentum");

        clTileArcaneBore = Class.forName("thaumcraft.common.tiles.TileArcaneBore");
        fBore_toDig = clTileArcaneBore.getDeclaredField("toDig");
        fBore_digX = clTileArcaneBore.getDeclaredField("digX");
        fBore_digY = clTileArcaneBore.getDeclaredField("digY");
        fBore_digZ = clTileArcaneBore.getDeclaredField("digZ");

        fFrostShard_shootingEntity = clEntityFrostShard.getDeclaredField("shootingEntity");
    }

    @Override
    public boolean loaded() {
        return clAlumentum != null && clEntityFrostShard != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return clAlumentum.isInstance(e) || clEntityFrostShard.isInstance(e) || clEntityPechBlast.isInstance(e);
    }

    @Override
    public boolean isEntityInstance(TileEntity e) {
        return clTileArcaneBore.isInstance(e);
        //return e.getClass() == clTileArcaneBore;
    }

    @Override
    public boolean isEntityInstance(Item e) {
        return clItemWandCasting.isInstance(e);
    }

    @Override
    public String update(Entity e) throws Exception {
        if (clAlumentum.isInstance(e)) {
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

            if (!thrower.canInteract(dim, x - explosionRadius, y, z - explosionRadius, Permissions.Build) || !thrower.canInteract(dim, x - explosionRadius, y, z + explosionRadius, Permissions.Build) || !thrower.canInteract(dim, x + explosionRadius, y, z - explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x + explosionRadius, y, z + explosionRadius, Permissions.Build)) {
                return "Explosion would hit a protected town";
            }
        } else if (clEntityFrostShard.isInstance(e)) {
            Entity shooter = (Entity) fFrostShard_shootingEntity.get(e);

            if (shooter == null || !(shooter instanceof EntityPlayer)) {
                return "No owner";
            }

            Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) shooter);

            int x = (int) (e.posX + e.motionX);
            int y = (int) (e.posY + e.motionY);
            int z = (int) (e.posZ + e.motionZ);
            int radius = 1;
            int dim = thrower.onlinePlayer.dimension;

            if (!thrower.canInteract(dim, x - radius, y - radius, y + radius, z - radius, Permissions.Build) || !thrower.canInteract(dim, x - radius, y - radius, y + radius, z + radius, Permissions.Build) || !thrower.canInteract(dim, x + radius, y - radius, y + radius, z - radius, Permissions.Build) || !thrower.canInteract(dim, x + radius, y - radius, y + radius, z + radius, Permissions.Build)) {
                return "Cannot build here";
            }
        } else if (clEntityPechBlast.isInstance(e)){
            EntityThrowable throwable = (EntityThrowable) e;
            EntityLivingBase thrower = throwable.getThrower();
            if (thrower == null || !(thrower instanceof EntityPlayer)) return "No owner";
            Resident res = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) thrower);
            
            int x = (int) (e.posX + e.motionX);
            int y = (int) (e.posY + e.motionY);
            int z = (int) (e.posZ + e.motionZ);
            int radius = 1;
            int dim = res.onlinePlayer.dimension;

            if (!res.canInteract(dim, x - radius, y - radius, y + radius, z - radius, Permissions.Build) || !res.canInteract(dim, x - radius, y - radius, y + radius, z + radius, Permissions.Build) || !res.canInteract(dim, x + radius, y - radius, y + radius, z - radius, Permissions.Build) || !res.canInteract(dim, x + radius, y - radius, y + radius, z + radius, Permissions.Build)) {
                return "Cannot attack here";
            }
        }

        return null;
    }

    @Override
    public String update(Resident res, Item tool, ItemStack item) throws Exception {
        if (clItemWandCasting.isInstance(tool)){
            Object focus = mGetFocus.invoke(tool, item);
            if (focus == null) return null;
            String focusName = focus.getClass().getName();
            
            if (focusName.equals("thaumcraft.common.items.wands.foci.ItemFocusFire")){
                List<Entity> list = this.getTargets(res.onlinePlayer.worldObj, res.onlinePlayer.getLook(17), res.onlinePlayer, 17);
                
                Log.info(list.toString());
                
                for (Entity e : list) {
                    Log.info("%s attacked %s", res.name(), e.getClass().getSimpleName());
                    if (!res.canAttack(e)) {
                        return "Cannot attack here";
                    }
                }
            } else if (focusName.equals("thaumcraft.common.items.wands.foci.ItemFocusShock")){
                List<Entity> list = this.getTargets(res.onlinePlayer.worldObj, res.onlinePlayer.getLook(17), res.onlinePlayer, 20);
                for (Entity e : list) {
                    Log.info("%s attacked %s", res.name(), e.getClass().getSimpleName());
                    if (!res.canAttack(e)) {
                        return "Cannot attack here";
                    }
                }
            } else if (focusName.equals("thaumcraft.common.items.wands.foci.ItemFocusExcavation")){
                MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);

                if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
                    if (!res.canInteract(res.onlinePlayer.dimension, pos.blockX, pos.blockY, pos.blockZ, Permissions.Build)) {
                        return "Cannot build here";
                    }
                }
            } else if (focusName.equals("thaumcraft.common.items.wands.foci.ItemFocusTrade")){
                if (!res.onlinePlayer.isSneaking()) {
                    MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);
                    
                    if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
                        int x = pos.blockX;
                        int y = pos.blockY;
                        int z = pos.blockZ;
                        int radius = 3;
                        int dim = res.onlinePlayer.dimension;

                        if (!res.canInteract(dim, x - radius, y - radius, y + radius, z - radius, Permissions.Build) || !res.canInteract(dim, x - radius, y - radius, y + radius, z + radius, Permissions.Build) || !res.canInteract(dim, x + radius, y - radius, y + radius, z - radius, Permissions.Build) || !res.canInteract(dim, x + radius, y - radius, y + radius, z + radius, Permissions.Build)) {
                            return "Cannot build here";
                        }
                    }
                }
            } else if (focusName.equals("thaumcraft.common.items.wands.foci.ItemFocusPortableHole")){
            	int potency = (Integer) mGetFocusPotency.invoke(clItemWandCasting.cast(tool), item);
            	int maxdist = 33 + potency * 8;
                MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, maxdist);
                if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE){
                    int x = pos.blockX;
                    int y = pos.blockY;
                    int z = pos.blockZ;
                    int radius = 1;
                    int dim = res.onlinePlayer.dimension;
                    
                    if (!res.canInteract(dim, x - radius, y - radius, y + radius, z - radius, Permissions.Build) || !res.canInteract(dim, x - radius, y - radius, y + radius, z + radius, Permissions.Build) || !res.canInteract(dim, x + radius, y - radius, y + radius, z - radius, Permissions.Build) || !res.canInteract(dim, x + radius, y - radius, y + radius, z + radius, Permissions.Build)) {
                        return "Cannot build here";
                    }
                }
            }
            //Thaumic Tinkerer Foci
            else if (focusName.equals("vazkii.tinkerer.common.item.foci.ItemFocusSmelt")){
            	MovingObjectPosition pos = getTargetBlock(res.onlinePlayer.worldObj, res.onlinePlayer, false);
                int x = pos.blockX;
                int y = pos.blockY;
                int z = pos.blockZ;
                int dim = res.onlinePlayer.dimension;
                if (!res.canInteract(dim, x , y, y, z, Permissions.Build)) {
                    return "Cannot build here";
                }
            }
        }
        return null;
    }
    
    private List<Entity>  getTargets(World world, Vec3 tvec, EntityPlayer p, double range) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Entity pointedEntity = null;
        Vec3 vec3d = Vec3.fakePool.getVecFromPool(p.posX, p.posY, p.posZ);
        Vec3 vec3d2 = vec3d.addVector(tvec.xCoord * range, tvec.yCoord * range, tvec.zCoord * range);
        float f1 = 1.0F;
        List<?> list = world.getEntitiesWithinAABBExcludingEntity(p, p.boundingBox.addCoord(tvec.xCoord * range, tvec.yCoord * range, tvec.zCoord * range).expand(f1, f1, f1));

        ArrayList<Entity> l = new ArrayList<Entity>();
        for (int i = 0; i < list.size(); i++){
            Entity entity = (Entity)list.get(i);
            if (entity.canBeCollidedWith()){
                float f2 = Math.max(1.0F, entity.getCollisionBorderSize());
                AxisAlignedBB axisalignedbb = entity.boundingBox.expand(f2, f2 * 1.25F, f2);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3d, vec3d2);
                
                if (movingobjectposition != null){
                    pointedEntity = entity;
                    
                    if ((pointedEntity != null) && (p.canEntityBeSeen(pointedEntity))){
                        l.add(pointedEntity);
                    }
                }
            }
        }

        return l;
    }
    
    public static MovingObjectPosition getTargetBlock(World world, Entity entity, boolean par3){
		float var4 = 1.0F;
		float var5 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * var4;
		float var6 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * var4;
		double var7 = entity.prevPosX + (entity.posX - entity.prevPosX) * var4;
		double var9 = entity.prevPosY + (entity.posY - entity.prevPosY) * var4 + 1.62D - entity.yOffset;
		double var11 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * var4;
		Vec3 var13 = world.getWorldVec3Pool().getVecFromPool(var7, var9, var11);
		float var14 = MathHelper.cos(-var6 * 0.01745329F - 3.141593F);
		float var15 = MathHelper.sin(-var6 * 0.01745329F - 3.141593F);
		float var16 = -MathHelper.cos(-var5 * 0.01745329F);
		float var17 = MathHelper.sin(-var5 * 0.01745329F);
		float var18 = var15 * var16;
		float var20 = var14 * var16;
		double var21 = 10.0D;
		Vec3 var23 = var13.addVector(var18 * var21, var17 * var21, var20 * var21);
		return world.rayTraceBlocks_do_do(var13, var23, par3, !par3);
    }

    @Override
    public String update(TileEntity e) throws Exception {
        if (clTileArcaneBore.isInstance(e)) {
            fBore_toDig.setAccessible(true);
            fBore_digX.setAccessible(true);
            fBore_digY.setAccessible(true);
            fBore_digZ.setAccessible(true);

            if (fBore_toDig.getBoolean(e)) {
                Resident actor = getActorFromLocation(e.worldObj.provider.dimensionId, e.xCoord, e.yCoord, e.zCoord, "#thaumcraft-bore#");
                if (!actor.canInteract(e.worldObj.provider.dimensionId, fBore_digX.getInt(e), fBore_digY.getInt(e), fBore_digZ.getInt(e), Permissions.Build)) {
                    Log.warning(String.format("Thaumcraft bore at Dim %s (%s,%s,%s) tried to break (%s,%s,%s) which failed. Actor: %s", e.worldObj.provider.dimensionId, e.xCoord, e.yCoord, e.zCoord, fBore_digX.getInt(e), fBore_digY.getInt(e), fBore_digZ.getInt(e), actor.name()));

                    fBore_toDig.set(e, false);
                }
            }
        }

        return null;
    }

    @Override
    public String getMod() {
        return "Thaumcraft4";
    }

    @Override
    public String getComment() {
        return "Build check: EntityAlumentum & Wand Foci";
    }
}
