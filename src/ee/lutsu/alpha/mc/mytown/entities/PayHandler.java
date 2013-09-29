package ee.lutsu.alpha.mc.mytown.entities;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeEventFactory;

import com.sperion.forgeperms.ForgePerms;

import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.Term;

public class PayHandler {
    public static long timeToPaySec = 1 * 60;
    public Resident owner;

    private ItemStack requestedItem;
    private IDone doneHandler;
    private Object[] doneHandlerArgs;
    private long timeUntil;

    public PayHandler(Resident owner) {
        this.owner = owner;
    }

    public void cancelPayment() {
        if (requestedItem == null) {
            return;
        }

        requestedItem = null;
    }

    public boolean tryPayByHand() {
        if (requestedItem == null) {
            return false;
        }

        if (timeUntil < System.currentTimeMillis()) {
            requestedItem = null;
            return false;
        }

        ItemStack hand = owner.onlinePlayer.getHeldItem();
        if (hand == null || hand.itemID != requestedItem.itemID || hand.getItemDamage() != requestedItem.getItemDamage()) {
            return false;
        }

        if (hand.stackSize < requestedItem.stackSize) {
            return false;
        }

        hand.stackSize -= requestedItem.stackSize;
        if (hand.stackSize <= 0) {
            ForgeEventFactory.onPlayerDestroyItem(owner.onlinePlayer, hand);
        }

        purchaseComplete();

        return true;
    }

    private void purchaseComplete() {
        requestedItem = null;

        if (doneHandler != null) {
            doneHandler.run(owner, doneHandlerArgs);
        }
    }

    public void requestPayment(String action, ItemStack stack, IDone actor, Object... args) {
        requestedItem = stack;
        doneHandler = actor;
        doneHandlerArgs = args;
        if (stack == null || stack.stackSize < 1 || ForgePerms.getPermissionsHandler().canAccess(owner.name(), DimensionManager.getProvider(owner.prevDimension).getDimensionName(),
        // owner.onlinePlayer.worldObj.provider.getDimensionName(),
                "mytown.cost.bypass." + action)) {
            purchaseComplete();
        } else {
            timeUntil = System.currentTimeMillis() + timeToPaySec * 1000;

            notifyUser();
        }
    }

    private void notifyUser() {
        if (!owner.isOnline()) {
            return;
        }

        MyTown.sendChatToPlayer(owner.onlinePlayer, Term.PayByHandNotify.toString(requestedItem.stackSize, requestedItem.getDisplayName(), requestedItem.getItem().itemID + (requestedItem.getItemDamage() != 0 ? ":" + requestedItem.getItemDamage() : "")));
        MyTown.sendChatToPlayer(owner.onlinePlayer, Term.PayByHandNotify2.toString());
    }

    public interface IDone {
        public void run(Resident player, Object[] args);
    }
}
