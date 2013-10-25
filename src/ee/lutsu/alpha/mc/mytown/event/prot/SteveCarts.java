package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.Vec3;
import ee.lutsu.alpha.mc.mytown.ChatChannel;
import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.commands.CmdChat;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

public class SteveCarts extends ProtBase {
    public static SteveCarts instance = new SteveCarts();

    Class<?> clSteveCart = null, clRailer, clMiner;
    Method mGetNextblock;
    Field fWorkModules;

    @Override
    public void load() throws Exception {
        clSteveCart = Class.forName("vswe.stevescarts.Carts.MinecartModular");
        fWorkModules = clSteveCart.getDeclaredField("workModules");

        Class<?> c = Class.forName("vswe.stevescarts.Modules.Workers.ModuleWorker");
        mGetNextblock = c.getDeclaredMethod("getNextblock");

        clRailer = Class.forName("vswe.stevescarts.Modules.Workers.ModuleRailer");
        clMiner = Class.forName("vswe.stevescarts.Modules.Workers.Tools.ModuleTool");
    }

    @Override
    public boolean loaded() {
        return clSteveCart != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return e.getClass() == clSteveCart;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String update(Entity e) throws Exception {
        if ((int) e.posX == (int) e.prevPosX && (int) e.posY == (int) e.prevPosY && (int) e.posZ == (int) e.prevPosZ) {
            return null;
        }

        fWorkModules.setAccessible(true);

        ArrayList<Object> modules = (ArrayList<Object>) fWorkModules.get(e);
        ArrayList<Object> railerModules = new ArrayList<Object>();
        ArrayList<Object> minerModules = new ArrayList<Object>();

        for (Object o : modules) {
            if (clRailer.isInstance(o)) {
                railerModules.add(o);
            }
            if (clMiner.isInstance(o)) {
                minerModules.add(o);
            }
        }

        Object module = null;
        if (railerModules.size() > 0) {
            module = railerModules.get(0);
        } else if (minerModules.size() > 0) {
            module = minerModules.get(0);
        } else {
            return null; // none found
        }

        Vec3 next = (Vec3) mGetNextblock.invoke(module);
        MyTownDatasource.instance.getBlock(e.dimension, ChunkCoord.getCoord(next.xCoord), ChunkCoord.getCoord(next.zCoord));

        if (railerModules.size() > 0) // railer
        {
            if (!canRoam(e.dimension, next.xCoord, next.yCoord - 1, next.yCoord + 1, next.zCoord, false)) {
                blockAction((EntityMinecart) e);
                return null;
            }
        }

        if (minerModules.size() > 0) // miner
        {
            int radius = 3 + 1;
            int y = (int) next.yCoord + 2;

            if (!canRoam(e.dimension, next.xCoord - radius, y - radius, y + radius, next.zCoord - radius, true) || !canRoam(e.dimension, next.xCoord - radius, y - radius, y + radius, next.zCoord + radius, true)
                    || !canRoam(e.dimension, next.xCoord + radius, y - radius, y + radius, next.zCoord - radius, true) || !canRoam(e.dimension, next.xCoord + radius, y - radius, y + radius, next.zCoord + radius, true)) {
                blockAction((EntityMinecart) e);
                return null;
            }
        }

        return null;
    }

    private boolean canRoam(int dim, double x, double yFrom, double yTo, double z, boolean miner) {
        TownBlock b = MyTownDatasource.instance.getBlock(dim, ChunkCoord.getCoord(x), ChunkCoord.getCoord(z));
        if (b != null && b.settings.yCheckOn) {
            if (yTo < b.settings.yCheckFrom || yFrom > b.settings.yCheckTo) {
                b = b.getFirstFullSidingClockwise(b.town());
            }
        }

        if (b == null || b.town() == null) {
            return miner && MyTown.instance.getWorldWildSettings(dim).allowStevecartsMiners || !miner && MyTown.instance.getWorldWildSettings(dim).allowStevecartsRailers;
        }

        return miner && b.settings.allowStevecartsMiners || !miner && b.settings.allowStevecartsRailers;
    }

    private void blockAction(EntityMinecart e) throws IllegalArgumentException, IllegalAccessException {
        dropMinecart(e);

        Log.severe(String.format("§4Stopped a steve cart found @ dim %s, %s,%s,%s", e.dimension, (int) e.posX, (int) e.posY, (int) e.posZ));

        String msg = String.format("A steve cart broke @ %s,%s,%s because it wasn't allowed there", (int) e.posX, (int) e.posY, (int) e.posZ);
        String formatted = Formatter.formatChatSystem(msg, ChatChannel.Local);
        CmdChat.sendChatToAround(e.dimension, e.posX, e.posY, e.posZ, formatted, null);
    }

    @Override
    public String getMod() {
        return "StevesCarts";
    }

    @Override
    public String getComment() {
        return "Town permission: allowStevecartsMiners & allowStevecartsRailers";
    }
    /*
     * //fCargo.setAccessible(true); //mIsValidForTrack.setAccessible(true);
     * module = railerModules.get(0); // will always return false if the miner
     * is under ground because the miner first removes the wall //if
     * (tryWorkRailer(module, next)) //{
     * 
     * boolean hasRails = false; for (Object railer : railerModules) {
     * ItemStack[] cargo = (ItemStack[])fCargo.get(railer);
     * 
     * for (int i = 0; i < cargo.length; i++) { ItemStack stack = cargo[i]; if
     * (stack == null) continue;
     * 
     * hasRails = true; cargo[i] = null; e.entityDropItem(stack, 1); } } if
     * (hasRails) Log.severe(String.format(
     * "§4A railer steve cart found in %s at dim %s, %s,%s,%s. Dropping rails.",
     * b == null || b.town() == null ? "wilderness" : b.town().name(),
     * e.dimension, (int)next.xCoord, (int)next.yCoord, (int)next.zCoord));
     * 
     * 
     * private boolean tryWorkRailer(Object cart, Vec3 next) throws
     * IllegalAccessException, IllegalArgumentException,
     * InvocationTargetException { int x = (int)next.xCoord; int y =
     * (int)next.yCoord; int z = (int)next.zCoord;
     * 
     * return canPlaceTrack(cart, x, y + 1, z) || canPlaceTrack(cart, x, y, z)
     * || canPlaceTrack(cart, x, y - 1, z); }
     * 
     * private boolean canPlaceTrack(Object cart, int i, int j, int k) throws
     * IllegalAccessException, IllegalArgumentException,
     * InvocationTargetException { return (Boolean)mIsValidForTrack.invoke(cart,
     * i, j, k, true); }
     */
}
