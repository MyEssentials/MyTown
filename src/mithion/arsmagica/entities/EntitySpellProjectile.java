package mithion.arsmagica.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntitySpellProjectile extends Entity {
    public EntitySpellProjectile(World par1World) {
        super(par1World);
    }

    public EntityLiving shootingEntity;
    public double accelerationX;
    public double accelerationY;
    public double accelerationZ;

    @Override
    protected void entityInit() {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {}
}