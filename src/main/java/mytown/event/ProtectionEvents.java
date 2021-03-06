package mytown.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.entities.ItemIdRange;
import mytown.entities.Resident;
import mytown.event.prot.BuildCraft;
import mytown.event.prot.ComputerCraft;
import mytown.event.prot.Creeper;
import mytown.event.prot.CustomNPCs;
import mytown.event.prot.DubstepGun;
import mytown.event.prot.Erebus;
import mytown.event.prot.FireBall;
import mytown.event.prot.FlansMod;
import mytown.event.prot.IndustrialCraft;
import mytown.event.prot.LOTR;
import mytown.event.prot.LycanitesMobs;
import mytown.event.prot.MFR;
import mytown.event.prot.Mekanism;
import mytown.event.prot.MinecartProtection;
import mytown.event.prot.Mobs;
import mytown.event.prot.ModularPowersuits;
import mytown.event.prot.PortalGun;
import mytown.event.prot.ProjectileProtection;
import mytown.event.prot.RailCraft;
import mytown.event.prot.Reliquary;
import mytown.event.prot.SteveCarts;
import mytown.event.prot.TNT;
import mytown.event.prot.ThaumCraft;
import mytown.event.prot.TheMistsOfRioV;
import mytown.event.prot.ThermalExpansion;
import mytown.event.prot.TinkersConstruct;
import mytown.event.prot.TrainCraft;
import mytown.event.prot.TwilightForest;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INpc;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ProtectionEvents implements ITickHandler {
	public static ArrayList<ProtBase> entityProtections = new ArrayList<ProtBase>();
	public static ArrayList<ProtBase> tileProtections = new ArrayList<ProtBase>();
	public static ArrayList<ProtBase> toolProtections = new ArrayList<ProtBase>();
	public static ArrayList<ProtBase> hostileMobs = new ArrayList<ProtBase>();
	public static ArrayList<ProtBase> attackMobs = new ArrayList<ProtBase>();

	public static ProtectionEvents instance = new ProtectionEvents();

	public Resident lastOwner = null;
	public boolean enabled = false;
	public ArrayList<Entity> toRemove = new ArrayList<Entity>();
	public ArrayList<TileEntity> toRemoveTile = new ArrayList<TileEntity>();
	public boolean loaded = false;
	@SuppressWarnings("rawtypes")
	private List<Class> npcClasses = null;
	public boolean dynamicEnabling = true;
	public boolean mobsoffspawnonly = false;
	public int[] projectileExemption;

	public ProtectionEvents() {
		ProtectionEvents.entityProtections.addAll(Arrays.asList(new ProtBase[] { Creeper.instance, Mobs.instance, LOTR.instance, TNT.instance, ThaumCraft.instance, PortalGun.instance, IndustrialCraft.instance, SteveCarts.instance, RailCraft.instance, TrainCraft.instance, Mekanism.instance,
				ModularPowersuits.instance, MFR.instance, TwilightForest.instance, TheMistsOfRioV.instance, FireBall.instance, ThermalExpansion.instance, MinecartProtection.instance, Erebus.instance, LycanitesMobs.instance, DubstepGun.instance, Reliquary.instance, FlansMod.instance,
				ProjectileProtection.instance }));
		ProtectionEvents.tileProtections.addAll(Arrays.asList(new ProtBase[] { BuildCraft.instance, ComputerCraft.instance, ThaumCraft.instance }));
		ProtectionEvents.toolProtections.addAll(Arrays.asList(new ProtBase[] { BuildCraft.instance, ComputerCraft.instance, ThaumCraft.instance, ModularPowersuits.instance, TinkersConstruct.instance, TwilightForest.instance }));
		ProtectionEvents.hostileMobs.addAll(Arrays.asList(new ProtBase[] { LycanitesMobs.instance }));
		ProtectionEvents.attackMobs.addAll(Arrays.asList(new ProtBase[] { LycanitesMobs.instance, CustomNPCs.instance }));
	}

	public int[] stringToIdList(String string) {
		String[] strList = string.split(",");
		int idList[];
		if (string == null || string.length() == 0) {
			idList = new int[0];
		} else {
			idList = new int[strList.length];
			for (int i = 0; i < strList.length; i++) {
				idList[i] = Integer.parseInt(strList[i].trim());
			}
		}

		return idList;
	}

	public String IdListToString(int idList[]) {
		if (idList == null || idList.length == 0) {
			return "";
		} else {
			String string = Arrays.toString(idList);
			return string.substring(1, string.length() - 2); // Remove brackets
		}
	}

	public boolean isHostileMob(Entity e) {
		for (ProtBase mob : hostileMobs) {
			if (mob.enabled && mob.isHostileMob(e)) {
				return true;
			}
		}

		return false;
	}

	public boolean canAttackMob(Entity e) {
		for (ProtBase mob : attackMobs) {
			if (mob.enabled && mob.canAttackMob(e)) {
				return true;
			}
		}

		return false;
	}

	public boolean itemUsed(Resident r) {
		try {
			String kill = null;

			ItemStack item = r.onlinePlayer.getHeldItem();
			if (item == null) {
				return true;
			}

			Item tool = item.getItem();
			if (tool == null) {
				return true;
			}

			// Always allow the usage of cart type items
			if (ItemIdRange.contains(MyTown.instance.carts, item)) {
				return true;
			}

			// Log.info(String.format("Item click : %s %s %s", r.name(), item,
			// tool.getClass()));

			ProtBase lastCheck = null;
			kill = null;
			for (ProtBase prot : toolProtections) {
				if (prot.enabled && prot.isEntityInstance(tool)) {
					lastCheck = prot;
					kill = prot.update(r, tool, item);
					if (kill != null) {
						break;
					}
				}
			}

			if (kill != null) {
				String sTool = String.format("[%s] %s", item.itemID + (item.isStackable() && item.getItemDamage() > 0 ? ":" + item.getItemDamage() : ""), tool.getUnlocalizedName());

				EntityPlayer pl = r.onlinePlayer;
				MyTown.instance.bypassLog.severe(String.format("[%s]Player %s tried to bypass at dim %d, %d,%d,%d using %s - %s", lastCheck.getClass().getSimpleName(), pl.username, pl.dimension, (int) pl.posX, (int) pl.posY, (int) pl.posZ, sTool, kill));
				MyTown.sendChatToPlayer(pl, "§4You cannot use that here - " + kill);
				return false;
			}
		} catch (Exception er) {
			MyTown.instance.bypassLog.severe("Error in player " + r.onlinePlayer.toString() + " item use check", er);
		}
		return true;
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (!enabled) {
			return;
		}

		setFields();

		World world = (World) tickData[0];
		Entity e = null;
		TileEntity t = null;
		String kill = null;

		toRemove.clear();
		toRemoveTile.clear();

		try {
			for (int i = 0; i < world.loadedEntityList.size(); i++) {
				e = (Entity) world.loadedEntityList.get(i);
				if (e == null || e.isDead) {
					continue;
				}

				lastOwner = null;
				kill = null;

				if (e instanceof EntityPlayer) {
					EntityPlayer pl = (EntityPlayer) e;
					if (pl.isUsingItem()) {
						Resident r = MyTownDatasource.instance.getOrMakeResident(pl);
						if (!ProtectionEvents.instance.itemUsed(r)) {
							r.onlinePlayer.stopUsingItem();
						}
					}
				}

				for (ProtBase prot : entityProtections) {
					if (prot.enabled && prot.isEntityInstance(e)) {
						kill = prot.update(e);
						if (kill != null) {
							break;
						}
					}
				}

				if (kill != null) {
					if (lastOwner != null) {
						if (lastOwner.isOnline()) {
							MyTown.instance.bypassLog.severe(String.format("Player %s tried to bypass at dim %d, %d,%d,%d using %s - %s", lastOwner.name(), lastOwner.onlinePlayer.dimension, (int) lastOwner.onlinePlayer.posX, (int) lastOwner.onlinePlayer.posY, (int) lastOwner.onlinePlayer.posZ,
									e.toString(), kill));
							MyTown.sendChatToPlayer(lastOwner.onlinePlayer, "§4You cannot use that here - " + kill);
						} else {
							MyTown.instance.bypassLog.severe(String.format("Player %s tried to bypass using %s - %s", lastOwner.name(), e.toString(), kill));
						}
					} else {
						MyTown.instance.bypassLog.severe(String.format("Entity %s tried to bypass using %s", e.toString(), kill));
					}

					toRemove.add(e);
				}
			}

			e = null;

			for (Entity en : toRemove) {
				world.removeEntity(en);
			}

			for (int i = 0; i < world.loadedTileEntityList.size(); i++) {
				t = (TileEntity) world.loadedTileEntityList.get(i);
				if (t == null) {
					continue;
				}

				lastOwner = null;
				kill = null;

				for (ProtBase prot : tileProtections) {
					if (prot.enabled && prot.isEntityInstance(t)) {
						kill = prot.update(t);
						if (kill != null) {
							break;
						}
					}
				}

				if (kill != null) {
					String block = String.format("TileEntity %s @ dim %s, %s,%s,%s", t.getClass().toString(), t.worldObj.provider.dimensionId, t.xCoord, t.yCoord, t.zCoord);
					if (lastOwner != null) {
						if (lastOwner.isOnline()) {
							MyTown.instance.bypassLog.severe(String.format("Player %s tried to bypass at dim %d, %d,%d,%d using %s - %s", lastOwner.name(), lastOwner.onlinePlayer.dimension, (int) lastOwner.onlinePlayer.posX, (int) lastOwner.onlinePlayer.posY, (int) lastOwner.onlinePlayer.posZ,
									block, kill));
							MyTown.sendChatToPlayer(lastOwner.onlinePlayer, "§4You cannot use that here - " + kill);
						} else {
							MyTown.instance.bypassLog.severe(String.format("Player %s tried to bypass using %s - %s", lastOwner.name(), block, kill));
						}
					} else {
						MyTown.instance.bypassLog.severe(String.format("TileEntity %s tried to bypass using %s", block, kill));
					}

					toRemoveTile.add(t);
				}
			}

			for (TileEntity en : toRemoveTile) {
				Block.blocksList[en.worldObj.getBlockId(en.xCoord, en.yCoord, en.zCoord)].dropBlockAsItem(en.worldObj, en.xCoord, en.yCoord, en.zCoord, en.worldObj.getBlockMetadata(en.xCoord, en.yCoord, en.zCoord), 0);
				en.worldObj.setBlock(en.xCoord, en.yCoord, en.zCoord, 0);
			}
		} catch (Exception er) {
			String ms = e == null ? t == null ? "#unknown#" : t.toString() : e.toString();
			MyTown.instance.bypassLog.severe("Error in entity " + ms + " pre-update check", er);
		}
	}

	@SuppressWarnings("rawtypes")
	public List<Class> getNPCClasses() {
		if (npcClasses == null) {
			npcClasses = Lists.newArrayList((Class) INpc.class);

			try {
				CustomNPCs.addNPCClasses(npcClasses);
			} catch (Throwable t) {

			}
		}

		return npcClasses;
	}

	public static List<ProtBase> getProtections() {
		Set<ProtBase> set = Sets.newHashSet();

		set.addAll(entityProtections);
		set.addAll(tileProtections);
		set.addAll(toolProtections);

		return new ArrayList<ProtBase>(set);
	}

	private void setFields() {
		if (loaded) {
			return;
		}

		for (ProtBase prot : getProtections()) {
			if (dynamicEnabling) {
				prot.enabled = true;
			}

			if (prot.enabled && !prot.loaded()) {
				try {
					prot.load();
				} catch (Exception e) {
					prot.enabled = false;
					MyTown.instance.coreLog.info("§f[§1Prot§f]Module %s §4failed §fto load. (%s)", prot.getClass().getSimpleName(), e.getMessage());
					if (!dynamicEnabling) {
						throw new RuntimeException("ProtectionEvents cannot load " + prot.getClass().getSimpleName() + " class. Is " + prot.getMod() + " loaded?", e);
					}
				}
			}

			if (prot.enabled) {
				MyTown.instance.coreLog.info("§f[§1Prot§f]Module %s §2loaded§f.", prot.getClass().getSimpleName());
			}
		}

		loaded = true;
	}

	public void reload() {
		loaded = false;

		for (ProtBase prot : getProtections()) {
			prot.reload();
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "MyTown protection event handler";
	}
}
