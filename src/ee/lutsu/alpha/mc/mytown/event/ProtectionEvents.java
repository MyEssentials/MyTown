package ee.lutsu.alpha.mc.mytown.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.ItemIdRange;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.event.prot.BuildCraft;
import ee.lutsu.alpha.mc.mytown.event.prot.ComputerCraft;
import ee.lutsu.alpha.mc.mytown.event.prot.Creeper;
import ee.lutsu.alpha.mc.mytown.event.prot.CustomNPCs;
import ee.lutsu.alpha.mc.mytown.event.prot.FireBall;
import ee.lutsu.alpha.mc.mytown.event.prot.IndustrialCraft;
import ee.lutsu.alpha.mc.mytown.event.prot.LOTR;
import ee.lutsu.alpha.mc.mytown.event.prot.MFR;
import ee.lutsu.alpha.mc.mytown.event.prot.Mekanism;
import ee.lutsu.alpha.mc.mytown.event.prot.Mobs;
import ee.lutsu.alpha.mc.mytown.event.prot.ModularPowersuits;
import ee.lutsu.alpha.mc.mytown.event.prot.PortalGun;
import ee.lutsu.alpha.mc.mytown.event.prot.RailCraft;
import ee.lutsu.alpha.mc.mytown.event.prot.SteveCarts;
import ee.lutsu.alpha.mc.mytown.event.prot.TNT;
import ee.lutsu.alpha.mc.mytown.event.prot.ThaumCraft;
import ee.lutsu.alpha.mc.mytown.event.prot.TheMistsOfRioV;
import ee.lutsu.alpha.mc.mytown.event.prot.TinkersConstruct;
import ee.lutsu.alpha.mc.mytown.event.prot.TrainCraft;
import ee.lutsu.alpha.mc.mytown.event.prot.TwilightForest;

public class ProtectionEvents implements ITickHandler {
    public static ArrayList<ProtBase> entityProtections = new ArrayList<ProtBase>();
    public static ArrayList<ProtBase> tileProtections = new ArrayList<ProtBase>();
    public static ArrayList<ProtBase> toolProtections = new ArrayList<ProtBase>();

    public static ProtectionEvents instance = new ProtectionEvents();

    public Resident lastOwner = null;
    public boolean enabled = false;
    public ArrayList<Entity> toRemove = new ArrayList<Entity>();
    public ArrayList<TileEntity> toRemoveTile = new ArrayList<TileEntity>();
    public boolean loaded = false;
    @SuppressWarnings("rawtypes")
	private List<Class> npcClasses = null;
    public boolean dynamicEnabling = true;

    public ProtectionEvents() {
        ProtectionEvents.entityProtections.addAll(Arrays.asList(new ProtBase[] { Creeper.instance, Mobs.instance, LOTR.instance, TNT.instance, ThaumCraft.instance, PortalGun.instance, IndustrialCraft.instance, SteveCarts.instance, RailCraft.instance, TrainCraft.instance, Mekanism.instance,
                ModularPowersuits.instance, MFR.instance, TwilightForest.instance, TheMistsOfRioV.instance, FireBall.instance }));
        ProtectionEvents.tileProtections.addAll(Arrays.asList(new ProtBase[] { BuildCraft.instance, ComputerCraft.instance, ThaumCraft.instance }));
        ProtectionEvents.toolProtections.addAll(Arrays.asList(new ProtBase[] { BuildCraft.instance, ComputerCraft.instance, ThaumCraft.instance, ModularPowersuits.instance, TinkersConstruct.instance, TwilightForest.instance }));
    }

    public static void addEntityProtection(ProtBase protection) {
        entityProtections.add(protection);
    }

    public static void addTileProtection(ProtBase protection) {
        tileProtections.add(protection);
    }

    public static void addToolProtection(ProtBase protection) {
        toolProtections.add(protection);
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
            
            Log.info("%s used %s", r.name(), tool.getClass().toString());

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
                Log.severe(String.format("[%s]Player %s tried to bypass at dim %d, %d,%d,%d using %s - %s", lastCheck.getClass().getSimpleName(), pl.username, pl.dimension, (int) pl.posX, (int) pl.posY, (int) pl.posZ, sTool, kill));
                MyTown.sendChatToPlayer(pl, "§4You cannot use that here - " + kill);
                return false;
            }
        } catch (Exception er) {
            Log.severe("Error in player " + r.onlinePlayer.toString() + " item use check", er);
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
                            Log.severe(String.format("Player %s tried to bypass at dim %d, %d,%d,%d using %s - %s", lastOwner.name(), lastOwner.onlinePlayer.dimension, (int) lastOwner.onlinePlayer.posX, (int) lastOwner.onlinePlayer.posY, (int) lastOwner.onlinePlayer.posZ, e.toString(), kill));
                            MyTown.sendChatToPlayer(lastOwner.onlinePlayer, "§4You cannot use that here - " + kill);
                        } else {
                            Log.severe(String.format("Player %s tried to bypass using %s - %s", lastOwner.name(), e.toString(), kill));
                        }
                    } else {
                        Log.severe(String.format("Entity %s tried to bypass using %s", e.toString(), kill));
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
                            Log.severe(String.format("Player %s tried to bypass at dim %d, %d,%d,%d using %s - %s", lastOwner.name(), lastOwner.onlinePlayer.dimension, (int) lastOwner.onlinePlayer.posX, (int) lastOwner.onlinePlayer.posY, (int) lastOwner.onlinePlayer.posZ, block, kill));
                            MyTown.sendChatToPlayer(lastOwner.onlinePlayer, "§4You cannot use that here - " + kill);
                        } else {
                            Log.severe(String.format("Player %s tried to bypass using %s - %s", lastOwner.name(), block, kill));
                        }
                    } else {
                        Log.severe(String.format("TileEntity %s tried to bypass using %s", block, kill));
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
            Log.severe("Error in entity " + ms + " pre-update check", er);
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
                    Log.info("§f[§1Prot§f]Module %s §4failed §fto load. (%s)", prot.getClass().getSimpleName(), e.getMessage());

                    if (!dynamicEnabling) {
                        throw new RuntimeException("ProtectionEvents cannot load " + prot.getClass().getSimpleName() + " class. Is " + prot.getMod() + " loaded?", e);
                    }
                }
            }

            if (dynamicEnabling && prot.enabled) {
                Log.info("§f[§1Prot§f]Module %s §2loaded§f.", prot.getClass().getSimpleName());
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
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {}

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.WORLD);
    }

    @Override
    public String getLabel() {
        return "MyTown protection event handler";
    }
}
