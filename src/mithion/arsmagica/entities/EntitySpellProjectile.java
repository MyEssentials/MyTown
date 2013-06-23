package mithion.arsmagica.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntitySpellProjectile extends Entity{
    public EntitySpellProjectile(World par1World) {
        super(par1World);
    }
    private int xTile;
    private int yTile;
    private int zTile;
    private int inTile;
    private boolean inGround;
    public EntityLiving shootingEntity;
    private int ticksAlive;
    private int ticksInAir;
    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;
    private ItemStack spellEffect;
    private int castingMode;
    private float scale;
    private int animationTicks;
    private int maxAge;
    private int projectileTextureIndex;
    private static final float GRAVITY_TERMINAL_VELOCITY = -2.0F;
    @Override
    protected void entityInit() {}
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {}
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {}
}