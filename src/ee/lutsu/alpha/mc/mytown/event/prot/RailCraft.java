package ee.lutsu.alpha.mc.mytown.event.prot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;
import railcraft.common.api.carts.IExplosiveCart;
import ee.lutsu.alpha.mc.mytown.ChatChannel;
import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.commands.CmdChat;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

public class RailCraft extends ProtBase {
    public static RailCraft instance = new RailCraft();

    Class clCartTNT = null, clBore;

    @Override
    public void load() throws Exception {
        clCartTNT = Class.forName("mods.railcraft.common.carts.EntityCartTNT");
        clBore = Class.forName("mods.railcraft.common.carts.EntityTunnelBore");
    }

    @Override
    public boolean loaded() {
        return clCartTNT != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return e.getClass() == clCartTNT || e.getClass() == clBore;
    }

    @Override
    public String update(Entity e) throws Exception {
        if (e.getClass() == clCartTNT) {
            IExplosiveCart cart = (IExplosiveCart) e;

            if (e.isDead || !cart.isPrimed() || cart.getFuse() > 1) {
                return null;
            }

            int radius = (int) Math.ceil(cart.getBlastRadius()) + 2; // 2 for
                                                                     // safety

            if (canBlow(e.dimension, e.posX - radius, e.posY - radius, e.posY + radius, e.posZ - radius) && canBlow(e.dimension, e.posX - radius, e.posY - radius, e.posY + radius, e.posZ + radius) && canBlow(e.dimension, e.posX + radius, e.posY - radius, e.posY + radius, e.posZ - radius)
                    && canBlow(e.dimension, e.posX + radius, e.posY - radius, e.posY + radius, e.posZ + radius)) {
                return null;
            }

            return "TNT cart explosion disabled here";
        } else {
            if ((int) e.posX == (int) e.prevPosX && (int) e.posY == (int) e.prevPosY && (int) e.posZ == (int) e.prevPosZ) {
                return null;
            }

            float offset = 3.3F;
            int radius = 1 + 1; // for safety
            MathHelper.floor_double(getXAhead(e, e.posX, offset));
            MathHelper.floor_double(getZAhead(e, e.posZ, offset));

            if (canRoam(e.dimension, e.posX - radius, e.posY - radius, e.posY + radius, e.posZ - radius) && canRoam(e.dimension, e.posX - radius, e.posY - radius, e.posY + radius, e.posZ + radius) && canRoam(e.dimension, e.posX + radius, e.posY - radius, e.posY + radius, e.posZ - radius)
                    && canRoam(e.dimension, e.posX + radius, e.posY - radius, e.posY + radius, e.posZ + radius)) {
                return null;
            }

            blockAction((EntityMinecart) e);
            return null;
        }
    }

    private boolean canRoam(int dim, double x, double yFrom, double yTo, double z) {
        TownBlock b = MyTownDatasource.instance.getBlock(dim, ChunkCoord.getCoord(x), ChunkCoord.getCoord(z));
        if (b != null && b.settings.yCheckOn) {
            if (yTo < b.settings.yCheckFrom || yFrom > b.settings.yCheckTo) {
                b = b.getFirstFullSidingClockwise(b.town());
            }
        }

        if (b == null || b.town() == null) {
            return MyTown.instance.getWorldWildSettings(dim).allowRailcraftBores;
        }

        return b.settings.allowRailcraftBores;
    }

    private void blockAction(EntityMinecart e) throws IllegalArgumentException, IllegalAccessException {
        dropMinecart(e);

        Log.severe(String.format("§4Stopped a railcraft bore found @ dim %s, %s,%s,%s", e.dimension, (int) e.posX, (int) e.posY, (int) e.posZ));

        String msg = String.format("A bore broke @ %s,%s,%s because it wasn't allowed there", (int) e.posX, (int) e.posY, (int) e.posZ);
        String formatted = Formatter.formatChatSystem(msg, ChatChannel.Local);
        CmdChat.sendChatToAround(e.dimension, e.posX, e.posY, e.posZ, formatted, null);
    }

    protected double getXAhead(Entity cart, double x, double offset) {
        if (getFacing(cart) == ForgeDirection.EAST) {
            x += offset;
        } else if (getFacing(cart) == ForgeDirection.WEST) {
            x -= offset;
        }
        return x;
    }

    protected double getZAhead(Entity cart, double z, double offset) {
        if (getFacing(cart) == ForgeDirection.NORTH) {
            z -= offset;
        } else if (getFacing(cart) == ForgeDirection.SOUTH) {
            z += offset;
        }
        return z;
    }

    protected final ForgeDirection getFacing(Entity cart) {
        return ForgeDirection.getOrientation(cart.getDataWatcher().getWatchableObjectByte(5));
    }

    private boolean canBlow(int dim, double x, double yFrom, double yTo, double z) {
        TownBlock b = MyTownDatasource.instance.getBlock(dim, ChunkCoord.getCoord(x), ChunkCoord.getCoord(z));
        if (b != null && b.settings.yCheckOn) {
            if (yTo < b.settings.yCheckFrom || yFrom > b.settings.yCheckTo) {
                b = b.getFirstFullSidingClockwise(b.town());
            }
        }

        if (b == null || b.town() == null) {
            return !MyTown.instance.getWorldWildSettings(dim).disableTNT;
        }

        return !b.settings.disableTNT;
    }

    @Override
    public String getMod() {
        return "RailCraft";
    }

    @Override
    public String getComment() {
        return "Town permission: disableTNT & allowRailcraftBores ";
    }
}
