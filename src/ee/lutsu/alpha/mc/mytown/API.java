package ee.lutsu.alpha.mc.mytown;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public abstract class API {
    public abstract boolean canEnter(EntityPlayer player, int dimension, int x, int yFrom, int yTo, int z);

    public abstract boolean canLoot(EntityPlayer player, int dimension, int x, int yFrom, int yTo, int z);

    public abstract boolean canAccess(EntityPlayer player, int dimension, int x, int yFrom, int yTo, int z);

    public abstract boolean canBuild(EntityPlayer player, int dimension, int x, int yFrom, int yTo, int z);

    public abstract boolean canAttack(EntityPlayer player, Entity e);
}