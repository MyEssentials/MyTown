package railcraft.common.api.carts;

public abstract interface IExplosiveCart
{
  public abstract void setPrimed(boolean paramBoolean);

  public abstract boolean isPrimed();

  public abstract int getFuse();

  public abstract void setFuse(int paramInt);

  public abstract float getBlastRadius();

  public abstract void setBlastRadius(float paramFloat);

  public abstract void explode();
}