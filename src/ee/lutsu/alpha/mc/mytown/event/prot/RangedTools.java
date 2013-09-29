package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Method;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import ee.lutsu.alpha.mc.mytown.Utils;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

public class RangedTools extends ProtBase {
    public static RangedTools instance = new RangedTools();

    @Override
    public void load() throws Exception {}

    @Override
    public boolean loaded() {
        return true;
    }

    @Override
    public boolean isEntityInstance(Item e) {
        Method m = null;
        try {
            m = e.getClass().getDeclaredMethod("onUsingItemTick", ItemStack.class, EntityPlayer.class, int.class);
        } catch (NoSuchMethodException e1) {} catch (NoClassDefFoundError e1) {
            // Log.warning("Cannot check the item " + e.getClass().toString() +
            // " for right click usage.");
            return true;
        } // cannot use reflection on this class!!

        return m != null;
    }

    @Override
    public String update(Resident res, Item tool, ItemStack item) throws Exception {
        MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 20);
        if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
            if (!res.canInteract(pos.blockX, pos.blockY, pos.blockZ, Permissions.Build)) {
                return "Cannot build here";
            }
        } else if (pos != null && pos.typeOfHit == EnumMovingObjectType.ENTITY) {
            if (!res.canAttack(pos.entityHit)) {
                return "Cannot attack here";
            }
        }

        // liquids
        pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, true, 20);
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
        return "VanillaRangedTools";
    }

    @Override
    public String getComment() {
        return "PVP & Build check: any tool ranged 20 block distance";
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }
}
