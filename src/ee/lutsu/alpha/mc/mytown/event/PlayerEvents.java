package ee.lutsu.alpha.mc.mytown.event;

import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import cpw.mods.fml.common.IPlayerTracker;
import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.Utils;
import ee.lutsu.alpha.mc.mytown.commands.CmdChat;
import ee.lutsu.alpha.mc.mytown.entities.ItemIdRange;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.tick.WorldBorder;

public class PlayerEvents implements IPlayerTracker
{
	@ForgeSubscribe(priority=EventPriority.HIGHEST)
	public void interact(PlayerInteractEvent ev)
	{
		if (ev.isCanceled())
			return;
		
		Resident r = source().getOrMakeResident(ev.entityPlayer);
		if (ev.action == Action.RIGHT_CLICK_AIR || ev.action == Action.RIGHT_CLICK_BLOCK)
		{
			if (r.pay.tryPayByHand())
			{
				ev.setCanceled(true);
				r.onlinePlayer.stopUsingItem();
				return;
			}
		}
		else
			r.pay.cancelPayment();
		
		if (!ProtectionEvents.instance.itemUsed(r))
		{
			ev.setCanceled(true);
			r.onlinePlayer.stopUsingItem();
			return;
		}
		Permissions perm = Permissions.Build;
		int x = ev.x, y = ev.y, z = ev.z;
		Action action = ev.action;
		
		if (action == Action.RIGHT_CLICK_AIR) // entity or air click
		{
			if (ev.entityPlayer.getHeldItem() != null && ev.entityPlayer.getHeldItem().getItem() != null)
			{
				Item item = ev.entityPlayer.getHeldItem().getItem();
				MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(r.onlinePlayer.worldObj, r.onlinePlayer, false);
				if (pos == null)
				{
					if (item instanceof ItemBow || item instanceof ItemEgg || item instanceof ItemPotion || item instanceof ItemFishingRod || item instanceof ItemExpBottle || item instanceof ItemEnderEye || item.getClass().getSimpleName().equalsIgnoreCase("ItemNanoBow"))
					{
						perm = Permissions.Build;
					}
					else
						return;
					
					x = (int)ev.entityPlayer.posX;
					y = (int)ev.entityPlayer.posY;
					z = (int)ev.entityPlayer.posZ;
				}
				else
				{
					action = Action.RIGHT_CLICK_BLOCK;
					if (pos.typeOfHit == EnumMovingObjectType.ENTITY)
					{
						x = (int)pos.entityHit.posX;
						y = (int)pos.entityHit.posY;
						z = (int)pos.entityHit.posZ;
					}
					else
					{
						x = pos.blockX;
						y = pos.blockY;
						z = pos.blockZ;
					}
				}
			}
			else
				return;
		}
		
		TownBlock targetBlock = MyTownDatasource.instance.getPermBlockAtCoord(ev.entityPlayer.dimension, x, y, z);
		
		if (action == Action.RIGHT_CLICK_BLOCK && (ev.entityPlayer.getHeldItem() != null && ev.entityPlayer.getHeldItem().getItem() != null && (ev.entityPlayer.getHeldItem().getItem() instanceof ItemMinecart || ItemIdRange.contains(MyTown.instance.carts, ev.entityPlayer.getHeldItem()))))
		{
			int en = ev.entityPlayer.worldObj.getBlockId(x , y, z);
			if (Block.blocksList[en] instanceof BlockRail)
			{
				if ((targetBlock != null && targetBlock.town() != null && targetBlock.settings.allowCartInteraction) || ((targetBlock == null || targetBlock.town() == null) && MyTown.instance.getWorldWildSettings(ev.entityPlayer.dimension).allowCartInteraction))
					return;
			}
		}
		else if (action == Action.RIGHT_CLICK_BLOCK)
		{
			if (!r.onlinePlayer.isSneaking())
			{
				TileEntity te = r.onlinePlayer.worldObj.getBlockTileEntity(x , y, z);
				if (te != null && te instanceof IInventory && ((IInventory)te).isUseableByPlayer(r.onlinePlayer))
					perm = Permissions.Access;
			}
			
			ItemStack itemstack = ev.entityPlayer.getHeldItem();
			Item item = itemstack == null ? null : itemstack.getItem();
			
			if (ev.entityPlayer.getHeldItem() == null/* || !(ev.entityPlayer.getHeldItem().getItem() instanceof ItemBlock)*/)
				perm = Permissions.Access;
			
			/*
			if (item != null && (item instanceof ItemBow || item instanceof ItemEgg || item instanceof ItemPotion || item instanceof ItemFishingRod || item instanceof ItemExpBottle || item instanceof ItemEnderEye))
				perm = Permissions.Build;*/
			
			// placing a block
			if (ev.face != -1 && perm == Permissions.Build && item != null && item instanceof ItemBlock)
			{
	            if (ev.face == 0)
	            	y--;
	            else if (ev.face == 1)
	            	y++;
	            else if (ev.face == 2)
	            	z--;
	            else if (ev.face == 3)
	            	z++;
	            else if (ev.face == 4)
	            	x--;
	            else if (ev.face == 5)
	            	x++;
	            
	            targetBlock = MyTownDatasource.instance.getPermBlockAtCoord(ev.entityPlayer.dimension, x, y, z);
			}
		}

		if (!r.canInteract(targetBlock, perm))
		{
			//Log.info("Permission denied %s %s", perm, action);
			// see if its a allowed block
			if (perm == Permissions.Build && action == Action.LEFT_CLICK_BLOCK)
			{
				World w = ev.entityPlayer.worldObj;
				//Log.info("Block is %s:%s", w.getBlockId(x, y, z), w.getBlockMetadata(x, y, z));
				if (ItemIdRange.contains(MyTown.instance.leftClickAccessBlocks, w.getBlockId(x, y, z), w.getBlockMetadata(x, y, z)))
				{
					perm = Permissions.Access;
					if (r.canInteract(targetBlock, perm))
						return;
				}
			}
			
			r.onlinePlayer.stopUsingItem();
			ev.setCanceled(true);
			if (perm == Permissions.Access)
				ev.entityPlayer.sendChatToPlayer(Term.ErrPermCannotAccessHere.toString());
			else
				ev.entityPlayer.sendChatToPlayer(Term.ErrPermCannotBuildHere.toString());
		}	
	}
	
	@ForgeSubscribe
	public void pickup(EntityItemPickupEvent ev)
	{
		if (ev.isCanceled())
			return;
		
		Resident r = source().getOrMakeResident(ev.entityPlayer);

		if (!r.canInteract(ev.item))
		{
			long time = System.currentTimeMillis();
			if (time > r.pickupWarningCooldown)
			{
				ev.entityPlayer.sendChatToPlayer(Term.ErrPermCannotPickup.toString());
				r.pickupWarningCooldown = time + Resident.pickupSpamCooldown;
			}
			ev.setCanceled(true);
		}
	}
	
	@ForgeSubscribe
	public void entityAttack(AttackEntityEvent ev)
	{
		if (ev.isCanceled())
			return;
		
		Resident attacker = source().getOrMakeResident(ev.entityPlayer);

		if (!attacker.canAttack(ev.target))
		{
			ev.entityPlayer.sendChatToPlayer(Term.ErrPermCannotAttack.toString());
			ev.setCanceled(true);
		}
	}
	
	@ForgeSubscribe
	public void onLivingAttackEvent(LivingAttackEvent ev)
	{
		if (ev.isCanceled() || ev.entity != null)
			return;
		
		if (ev.entityLiving instanceof EntityPlayer)
		{
			Resident t = source().getOrMakeResident((EntityPlayer)ev.entityLiving);
			
			if ((ev.source.getEntity() != null && !t.canBeAttackedBy(ev.source.getEntity())) || (ev.source.getSourceOfDamage() != null && !t.canBeAttackedBy(ev.source.getSourceOfDamage())))
			{
				ev.setCanceled(true);
				// spamming
				/*Log.info(String.format("%s is attacking %s whos in a protected area",
						ev.source.getEntity() != null ? ev.source.getEntity() : ev.source.getSourceOfDamage(),
						t.onlinePlayer));
				*/
			}
		}
		
		Entity target = ev.entity;
		Resident attacker = null;
		
		if (ev.source.getEntity() != null && ev.source.getEntity() instanceof EntityPlayer)
			attacker = source().getOrMakeResident((EntityPlayer)ev.source.getEntity());
		
		if (ev.source.getSourceOfDamage() != null && ev.source.getSourceOfDamage() instanceof EntityPlayer)
			attacker = source().getOrMakeResident((EntityPlayer)ev.source.getSourceOfDamage());
		
		if (!attacker.isOnline())
		{
			ev.setCanceled(true);
			return;
		}
		
		if (!attacker.canAttack(target))
		{
			attacker.onlinePlayer.sendChatToPlayer(Term.ErrPermCannotAttack.toString());
			ev.setCanceled(true);
			return;
		}
	}

	@ForgeSubscribe
	public void entityInteract(EntityInteractEvent ev)
	{
		if (ev.isCanceled())
			return;
		
		Resident r = source().getOrMakeResident(ev.entityPlayer);

		if (!r.canInteract(ev.target))
		{
			ev.entityPlayer.sendChatToPlayer(Term.ErrPermCannotInteract.toString());
			ev.setCanceled(true);
		}
	}
	
	@ForgeSubscribe
	public void minecartCollision(MinecartCollisionEvent ev)
	{
		if (!(ev.collider instanceof EntityPlayer))
			return;
		
		Resident r = source().getOrMakeResident((EntityPlayer)ev.collider);
		
		TownBlock t = source().getBlock(r.onlinePlayer.dimension, ev.minecart.chunkCoordX, ev.minecart.chunkCoordZ);
		
		if (t == null || t.town() == null || t.town() == r.town() || t.settings.allowCartInteraction)
			return;
		
		long time = System.currentTimeMillis();
		if (t.town().minecraftNotificationTime < time)
		{
			t.town().minecraftNotificationTime = time + Town.dontSendCartNotification;
			t.town().sendNotification(Level.WARNING, Term.MinecartMessedWith.toString());
		}
	}

	private MyTownDatasource source(){ return MyTownDatasource.instance; }

	@Override
	public void onPlayerLogin(EntityPlayer player) 
	{
		// load the resident
		Resident r = source().getOrMakeResident(player);
		
		if (!WorldBorder.instance.isWithinArea(player))
		{
			Log.warning(String.format("Player %s logged in over the world edge %s (%s, %s, %s). Sending to spawn.",
					r.name(), player.dimension,
					player.posX, player.posY, player.posZ));
			r.respawnPlayer();
		}
		
		TownBlock t = source().getBlock(r.onlinePlayer.dimension, player.chunkCoordX, player.chunkCoordZ);

		r.location = t != null && t.town() != null ? t.town() : null;
		r.location2 = t != null && t.town() != null ? t.owner() : null;
		
		if (!r.canInteract(t, (int)player.posY, Permissions.Enter))
		{
			Log.warning(String.format("Player %s logged in at a enemy town %s (%s, %s, %s, %s) with bouncing on. Sending to spawn.",
					r.name(), r.location.name(),
					player.dimension, player.posX, player.posY, player.posZ));
			r.respawnPlayer();
		}
		
		if (r.town() != null)
			r.town().notifyPlayerLoggedOn(r);
		
		r.loggedIn();
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) 
	{
		Resident res = source().getOrMakeResident(player);

		if (res.town() != null)
			res.town().notifyPlayerLoggedOff(res);
		
		res.loggedOf();
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
	}

	@ForgeSubscribe
	public void serverChat(ServerChatEvent ev)
	{
		if (ev.isCanceled() || ev.message == null || ev.message.trim().length() < 1 || !Formatter.formatChat)
			return;
		
		ev.setCanceled(true);
		Resident res = source().getOrMakeResident(ev.player);
		CmdChat.sendToChannelFromDirectTalk(res, ev.message, res.activeChannel, false);
	}
	
	@ForgeSubscribe
	public void livingUpdate(LivingUpdateEvent ev)
	{
		if (ev.isCanceled() || !(ev.entityLiving instanceof EntityPlayer))
			return;

		// so we don't re-link to player to be online
		// as this is called after the player logs off
		Resident res = source().getOrMakeResident((EntityPlayer)ev.entityLiving);
		
		if (res != null && res.isOnline())
			res.update();
	}
}
