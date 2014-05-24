package mytown.entities;

import forgeperms.api.ForgePermsAPI;
import mytown.MyTown;
import mytown.Term;
import net.minecraft.item.ItemStack;

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

		if (ForgePermsAPI.econManager.playerWithdraw(owner.name(), owner.onlinePlayer.worldObj.provider.getDimensionName(), requestedItem.itemID + ":" + requestedItem.getItemDamage(), requestedItem.stackSize)) {
			purchaseComplete();
			return true;
		}
		return false;
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
		if (stack == null || stack.stackSize < 1 || ForgePermsAPI.permManager.canAccess(owner.name(), owner.onlinePlayer.worldObj.provider.getDimensionName(), "mytown.cost.bypass." + action)) {
			purchaseComplete();
		} else {
			if (ForgePermsAPI.econManager.rightClickToPay()) {
				timeUntil = System.currentTimeMillis() + timeToPaySec * 1000;
				notifyUser();
			} else {
				if (ForgePermsAPI.econManager.playerWithdraw(owner.name(), owner.onlinePlayer.worldObj.provider.getDimensionName(), requestedItem.itemID + ":" + requestedItem.getItemDamage(), requestedItem.stackSize)) {
					MyTown.sendChatToPlayer(owner.onlinePlayer, "Took " + ForgePermsAPI.econManager.format(requestedItem.itemID + ":" + requestedItem.getItemDamage(), requestedItem.stackSize));
					purchaseComplete();
				} else {
					MyTown.sendChatToPlayer(owner.onlinePlayer, "You don't have enough money to pay for this! Now, WASH THE DISHES!");
				}
			}
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
