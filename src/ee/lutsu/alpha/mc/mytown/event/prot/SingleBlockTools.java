package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Method;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBoat;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import ee.lutsu.alpha.mc.mytown.Utils;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

public class SingleBlockTools extends ProtBase {
    public static SingleBlockTools instance = new SingleBlockTools();

    @Override
    public void load() throws Exception {}

    @Override
    public boolean loaded() {
        return true;
    }

    @Override
    public boolean isEntityInstance(Item e) {
        return isCritical(e);
        // public boolean onItemUseFirst(ItemStack stack, EntityPlayer player,
        // World world, int x, int y, int z, int side, float hitX, float hitY,
        // float hitZ)
    }

    private boolean isCritical(Item e) {
        if (e instanceof ItemBucket || e instanceof ItemBoat || e.getClass().getSimpleName().equalsIgnoreCase("ItemChestTransporter")) {
            return true;
        }

        Method m = null;
        try {
            m = e.getClass().getDeclaredMethod("onItemUseFirst", ItemStack.class, EntityPlayer.class, World.class, int.class, int.class, int.class, int.class, float.class, float.class, float.class);
        } catch (NoSuchMethodException e1) {} catch (NoClassDefFoundError e1) {
            // Log.warning("Cannot check the item " + e.getClass().toString() +
            // " for right click usage.");
            return true;
        } // cannot use reflection on this class!!

        return m != null;
    }

    @Override
    public String update(Resident res, Item tool, ItemStack item) throws Exception {
        boolean liquid = false;

        if (tool instanceof ItemBucket) {
            liquid = tool == Item.bucketEmpty;
        } else if (tool instanceof ItemBoat) {
            liquid = true;
        }

        MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, liquid);

        if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
            if (!res.canInteract(pos.blockX, pos.blockY, pos.blockZ, Permissions.Build)) {
                return "Cannot build here";
            }
        } else if (pos != null && pos.typeOfHit == EnumMovingObjectType.ENTITY) {
            if (!res.canAttack(pos.entityHit)) {
                return "Cannot attack here";
            }
        }

        return null;
    }

    @Override
    public String getMod() {
        return "VanillaTools";
    }

    @Override
    public String getComment() {
        return "Build check: any tool single target block right click";
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }
}
