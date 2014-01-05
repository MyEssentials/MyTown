package mytown;

import mytown.entities.TownBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Utils {
    public static MovingObjectPosition getMovingObjectPositionFromPlayer(World world, EntityPlayer player, boolean hitLiquid) {
        double distance = 5.0D;
        if (player instanceof EntityPlayerMP) {
            distance = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
        }

        return getMovingObjectPositionFromPlayer(world, player, hitLiquid, distance);
    }

    public static MovingObjectPosition getMovingObjectPositionFromPlayer(World world, EntityPlayer player, boolean hitLiquid, double distance) {
        float var4 = 1.0F;
        float var5 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * var4;
        float var6 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * var4;
        double var7 = player.prevPosX + (player.posX - player.prevPosX) * var4;
        double var9 = player.prevPosY + (player.posY - player.prevPosY) * var4 + 1.62D - player.yOffset;
        double var11 = player.prevPosZ + (player.posZ - player.prevPosZ) * var4;
        Vec3 var13 = world.getWorldVec3Pool().getVecFromPool(var7, var9, var11);
        float var14 = MathHelper.cos(-var6 * 0.017453292F - (float) Math.PI);
        float var15 = MathHelper.sin(-var6 * 0.017453292F - (float) Math.PI);
        float var16 = -MathHelper.cos(-var5 * 0.017453292F);
        float var17 = MathHelper.sin(-var5 * 0.017453292F);
        float var18 = var15 * var16;
        float var20 = var14 * var16;
        Vec3 var23 = var13.addVector(var18 * distance, var17 * distance, var20 * distance);
        return world.clip(var13, var23, hitLiquid);
    }

    public static boolean canBlow(int dim, double x, double yFrom, double yTo, double z) {
        TownBlock b = MyTownDatasource.instance.getBlock(dim, ChunkCoord.getCoord(x), ChunkCoord.getCoord(z));
        if (b != null && b.settings.get("core").getSetting("ycheck").getValue(String.class) != null && !b.settings.get("core").getSetting("ycheck").getValue(String.class).isEmpty()) {
        	String[] ycheck = b.settings.get("core").getSetting("ycheck").getValue(String.class).split(",");
        	int yto = Integer.parseInt(ycheck[0]);
        	int yfrom = Integer.parseInt(ycheck[1]);
            if (yTo < yto || yFrom > yfrom) {
                b = b.getFirstFullSidingClockwise(b.town());
            }
        }

        if (b == null || b.town() == null) {
            return !MyTown.instance.getWorldWildSettings(dim).getSetting("explosion").getValue(Boolean.class);
        }

        return !b.settings.get("core").getSetting("explosion").getValue(Boolean.class);
    }
}
