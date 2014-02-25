package mytown.event.prot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import mytown.ChunkCoord;
import mytown.MyTown;
import mytown.MyTownDatasource;
import mytown.Utils;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.TownSettingCollection.Permissions;
import mytown.event.ProtBase;
import mytown.event.ProtectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ThaumCraft extends ProtBase {
	public static ThaumCraft instance = new ThaumCraft();
	public int explosionRadius = 6;

	private Class<?> clTileArcaneBore, clEntityFrostShard, clItemWandCasting, clEntityPrimalOrb;
	private Method mGetFocus, mGetFocusPotency;
	private Field fBore_toDig, fBore_digX, fBore_digZ, fBore_digY, fFrostShard_shootingEntity;

	@Override
	public void load() throws Exception {
		clItemWandCasting = Class.forName("thaumcraft.common.items.wands.ItemWandCasting");
		mGetFocus = clItemWandCasting.getDeclaredMethod("getFocus", ItemStack.class);
		mGetFocusPotency = clItemWandCasting.getDeclaredMethod("getFocusPotency", ItemStack.class);

		clEntityFrostShard = Class.forName("thaumcraft.common.entities.projectile.EntityFrostShard");
		clEntityPrimalOrb = Class.forName("thaumcraft.common.entities.projectile.EntityPrimalOrb");
		
		clTileArcaneBore = Class.forName("thaumcraft.common.tiles.TileArcaneBore");
		fBore_toDig = clTileArcaneBore.getDeclaredField("toDig");
		fBore_digX = clTileArcaneBore.getDeclaredField("digX");
		fBore_digY = clTileArcaneBore.getDeclaredField("digY");
		fBore_digZ = clTileArcaneBore.getDeclaredField("digZ");

		fFrostShard_shootingEntity = clEntityFrostShard.getDeclaredField("shootingEntity");
	}

	@Override
	public boolean loaded() {
		return clTileArcaneBore != null || clEntityFrostShard != null || clItemWandCasting != null || clEntityPrimalOrb != null;
	}

	@Override
	public boolean isEntityInstance(Entity e) {
		return clEntityFrostShard.isInstance(e) || clEntityPrimalOrb.isInstance(e);
	}

	@Override
	public boolean isEntityInstance(TileEntity e) {
		return clTileArcaneBore.isInstance(e);
	}

	@Override
	public boolean isEntityInstance(Item e) {
		return clItemWandCasting.isInstance(e);
	}

	@Override
	public String update(Entity e) throws Exception {
		Entity shooter = null;
		int radius = 0;
		int x = (int) (e.posX + e.motionX), y = (int) (e.posY + e.motionY), z = (int) (e.posZ + e.motionZ);
		
		if (clEntityFrostShard.isInstance(e)) {
			shooter = (Entity) fFrostShard_shootingEntity.get(e);
			radius = 1;
		} else if (clEntityPrimalOrb.isInstance(e)){
			EntityThrowable primalOrb = (EntityThrowable)e;
			shooter = primalOrb.getThrower();
			radius = 4;
		}

		if (shooter == null || !(shooter instanceof EntityPlayer)) {
			return "No owner";
		}
		
		Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) shooter);
		
		int dim = e.dimension;
		if (!thrower.canInteract(dim, x - radius, y - radius, y + radius, z - radius, Permissions.Build) || !thrower.canInteract(dim, x - radius, y - radius, y + radius, z + radius, Permissions.Build)
				|| !thrower.canInteract(dim, x + radius, y - radius, y + radius, z - radius, Permissions.Build) || !thrower.canInteract(dim, x + radius, y - radius, y + radius, z + radius, Permissions.Build)) {
			return "Cannot build here";
		}

		return null;
	}

	@Override
	public String update(Resident res, Item tool, ItemStack item) throws Exception {
		if (clItemWandCasting.isInstance(tool)) {
			Object focus = mGetFocus.invoke(tool, item);
			if (focus == null)
				return null;
			String focusName = focus.getClass().getName();
			int potency = (Integer) mGetFocusPotency.invoke(clItemWandCasting.cast(tool), item);

			if (focusName.equals("thaumcraft.common.items.wands.foci.ItemFocusFire")) {
				List<Entity> list = getTargets(res.onlinePlayer.worldObj, res.onlinePlayer.getLook(17), res.onlinePlayer, 17);

				MyTown.instance.bypassLog.info(list.toString());

				for (Entity e : list) {
					MyTown.instance.bypassLog.info("%s attacked %s", res.name(), e.getClass().getSimpleName());
					if (!res.canAttack(e)) {
						return "Cannot attack here";
					}
				}
			} else if (focusName.equals("thaumcraft.common.items.wands.foci.ItemFocusShock")) {
				List<Entity> list = getTargets(res.onlinePlayer.worldObj, res.onlinePlayer.getLook(17), res.onlinePlayer, 20);
				for (Entity e : list) {
					MyTown.instance.bypassLog.info("%s attacked %s", res.name(), e.getClass().getSimpleName());
					if (!res.canAttack(e)) {
						return "Cannot attack here";
					}
				}
			} else if (focusName.equals("thaumcraft.common.items.wands.foci.ItemFocusExcavation")) {
				MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);

				if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
					if (!res.canInteract(res.onlinePlayer.dimension, pos.blockX, pos.blockY, pos.blockZ, Permissions.Build)) {
						return "Cannot build here";
					}
				}
			} else if (focusName.equals("thaumcraft.common.items.wands.foci.ItemFocusTrade")) {
				MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, 10.0D);

				if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
					int x = pos.blockX;
					int y = pos.blockY;
					int z = pos.blockZ;
					int radius = 3 + potency;
					int dim = res.onlinePlayer.dimension;
					
					MyTown.instance.bypassLog.info("X: %s, Y: %s, Z: %s, Radius: %S, Dim: %s", x, y, z, radius, dim);

					if (!res.canInteract(dim, x - radius, y - radius, y + radius, z - radius, Permissions.Build) || !res.canInteract(dim, x - radius, y - radius, y + radius, z + radius, Permissions.Build) ||
						!res.canInteract(dim, x + radius, y - radius, y + radius, z - radius, Permissions.Build) || !res.canInteract(dim, x + radius, y - radius, y + radius, z + radius, Permissions.Build)) {
// FIXME - Can't figure out what to swap this back too.
//						res.onlinePlayer.worldObj.setBlock(x, y, z, blockid);

						return "Cannot build here";
					}
				}
			} else if (focusName.equals("thaumcraft.common.items.wands.foci.ItemFocusPortableHole")) {
				int maxdist = 33 + potency * 8;
				MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.onlinePlayer.worldObj, res.onlinePlayer, false, maxdist);
				if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
					int x = pos.blockX;
					int y = pos.blockY;
					int z = pos.blockZ;
					int radius = 1;
					int dim = res.onlinePlayer.dimension;

					if (!res.canInteract(dim, x - radius, y - radius, y + radius, z - radius, Permissions.Build) || !res.canInteract(dim, x - radius, y - radius, y + radius, z + radius, Permissions.Build) || !res.canInteract(dim, x + radius, y - radius, y + radius, z - radius, Permissions.Build)
							|| !res.canInteract(dim, x + radius, y - radius, y + radius, z + radius, Permissions.Build)) {
						return "Cannot build here";
					}
				}
			}
			// Thaumic Tinkerer Foci
			else if (focusName.equals("vazkii.tinkerer.common.item.foci.ItemFocusSmelt")) {
				MovingObjectPosition pos = getTargetBlock(res.onlinePlayer.worldObj, res.onlinePlayer, false);
				int x = pos.blockX;
				int y = pos.blockY;
				int z = pos.blockZ;
				int dim = res.onlinePlayer.dimension;
				if (!res.canInteract(dim, x, y, y, z, Permissions.Build)) {
					return "Cannot build here";
				}
			} else if (focusName.equals("vazkii.tinkerer.common.item.foci.ItemFocusDislocation")) {
				MovingObjectPosition pos = getTargetBlock(res.onlinePlayer.worldObj, res.onlinePlayer, false);
				if (pos != null && pos.typeOfHit == EnumMovingObjectType.TILE) {
// FIXME - Undo glitch for selection of protected block
				}
			}
		}
		return null;
	}

	@Override
	public String update(TileEntity e) throws Exception {
		if (clTileArcaneBore.isInstance(e)) {
			fBore_toDig.setAccessible(true);
			fBore_digX.setAccessible(true);
			fBore_digY.setAccessible(true);
			fBore_digZ.setAccessible(true);

			if (fBore_toDig.getBoolean(e)) {
				TownBlock b = MyTownDatasource.instance.getBlock(e.worldObj.provider.dimensionId, ChunkCoord.getCoord(fBore_digX.getInt(e)), ChunkCoord.getCoord(fBore_digZ.getInt(e)));
				if (b == null) {
					if (MyTown.instance.getWorldWildSettings(e.worldObj.provider.dimensionId).allowTCBores)
						return null;
					MyTown.instance.bypassLog.warning(String.format("Thaumcraft bore at Dim %s (%s,%s,%s) tried to break (%s,%s,%s) [Wild] which failed.", e.worldObj.provider.dimensionId, e.xCoord, e.yCoord, e.zCoord, fBore_digX.getInt(e), fBore_digY.getInt(e), fBore_digZ.getInt(e)));
					fBore_toDig.set(e, false);
				} else {
					if (b.settings.allowTCBores)
						return null;
					MyTown.instance.bypassLog.warning(String.format("Thaumcraft bore at Dim %s (%s,%s,%s) tried to break (%s,%s,%s) [Town] which failed.", e.worldObj.provider.dimensionId, e.xCoord, e.yCoord, e.zCoord, fBore_digX.getInt(e), fBore_digY.getInt(e), fBore_digZ.getInt(e)));
					fBore_toDig.set(e, false);
				}
			}
		}

		return null;
	}

	@Override
	public String getMod() {
		return "Thaumcraft4";
	}

	@Override
	public String getComment() {
		return "Build check: EntityAlumentum, Wand Foci, & Arcane Bore";
	}

	private List<Entity> getTargets(World world, Vec3 tvec, EntityPlayer p, double range) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Entity pointedEntity = null;
		Vec3 vec3d = Vec3.fakePool.getVecFromPool(p.posX, p.posY, p.posZ);
		Vec3 vec3d2 = vec3d.addVector(tvec.xCoord * range, tvec.yCoord * range, tvec.zCoord * range);
		float f1 = 1.0F;
		List<?> list = world.getEntitiesWithinAABBExcludingEntity(p, p.boundingBox.addCoord(tvec.xCoord * range, tvec.yCoord * range, tvec.zCoord * range).expand(f1, f1, f1));

		ArrayList<Entity> l = new ArrayList<Entity>();
		for (int i = 0; i < list.size(); i++) {
			Entity entity = (Entity) list.get(i);
			if (entity.canBeCollidedWith()) {
				float f2 = Math.max(1.0F, entity.getCollisionBorderSize());
				AxisAlignedBB axisalignedbb = entity.boundingBox.expand(f2, f2 * 1.25F, f2);
				MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3d, vec3d2);

				if (movingobjectposition != null) {
					pointedEntity = entity;

					if ((pointedEntity != null) && (p.canEntityBeSeen(pointedEntity))) {
						l.add(pointedEntity);
					}
				}
			}
		}

		return l;
	}

	public static MovingObjectPosition getTargetBlock(World world, Entity entity, boolean par3) {
		float var4 = 1.0F;
		float var5 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * var4;
		float var6 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * var4;
		double var7 = entity.prevPosX + (entity.posX - entity.prevPosX) * var4;
		double var9 = entity.prevPosY + (entity.posY - entity.prevPosY) * var4 + 1.62D - entity.yOffset;
		double var11 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * var4;
		Vec3 var13 = world.getWorldVec3Pool().getVecFromPool(var7, var9, var11);
		float var14 = MathHelper.cos(-var6 * 0.01745329F - 3.141593F);
		float var15 = MathHelper.sin(-var6 * 0.01745329F - 3.141593F);
		float var16 = -MathHelper.cos(-var5 * 0.01745329F);
		float var17 = MathHelper.sin(-var5 * 0.01745329F);
		float var18 = var15 * var16;
		float var20 = var14 * var16;
		double var21 = 10.0D;
		Vec3 var23 = var13.addVector(var18 * var21, var17 * var21, var20 * var21);
		return world.rayTraceBlocks_do_do(var13, var23, par3, !par3);
	}
}
