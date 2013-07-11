package ee.lutsu.alpha.mc.mytown.event.prot;

import java.util.ArrayList;
import java.util.List;

import mithion.arsmagica.entities.EntitySpellProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;

public class ArsMagica extends ProtBase {
    public static ArsMagica instance = new ArsMagica();

    private Class<?> clSpellScrollBase = null, clIDamagingSpell,
            clEntitySpellProjectile, clISummonCreature, clEntityLightMage,
            clEntityDarkMage; // clIRangedSpell, clIBeamSpell;
    public int explosionRadius = 6;

    /**
     * Load the classes needed for protection checking (Throws exception if they
     * are not there)
     */
    @Override
    public void load() throws Exception {
        clSpellScrollBase = Class
                .forName("mithion.arsmagica.api.spells.SpellScrollBase");
        clIDamagingSpell = Class
                .forName("mithion.arsmagica.api.spells.interfaces.IDamagingSpell");
        // clIRangedSpell =
        // Class.forName("mithion.arsmagica.api.spells.interfaces.IRangedSpell");
        // clIBeamSpell =
        // Class.forName("mithion.arsmagica.api.spells.interfaces.IBeamSpell");
        clEntitySpellProjectile = Class
                .forName("mithion.arsmagica.entities.EntitySpellProjectile");
        clISummonCreature = Class
                .forName("mithion.arsmagica.api.spells.interfaces.ISummonCreature");
        clEntityLightMage = Class
                .forName("mithion.arsmagica.entities.EntityLightMage");
        clEntityDarkMage = Class
                .forName("mithion.arsmagica.entities.EntityDarkMage");
    }

    /**
     * Whether the Protection is loaded or not
     */
    @Override
    public boolean loaded() {
        return clSpellScrollBase != null;
    }

    /**
     * Check if an entity (projectile in this case) is within the town, and if
     * it is allowed
     */
    @Override
    public String update(Entity e) throws Exception {
        if (clEntitySpellProjectile.isInstance(e)) {
            EntitySpellProjectile o = (EntitySpellProjectile) e;
            Entity owner = o.shootingEntity;

            if (owner == null) {
                return "No owner or is not a player";
            }
            if (clEntityLightMage.isInstance(owner)
                    || clEntityDarkMage.isInstance(owner)) {
                return null;
            }

            Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance
                    .getResident((EntityPlayer) owner);

            int x = (int) (o.posX + o.motionX);
            int y = (int) (o.posY + o.motionY);
            int z = (int) (o.posZ + o.motionZ);
            int dim = thrower.onlinePlayer.dimension;

            if (!thrower.canInteract(dim, x - explosionRadius, y, z
                    - explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x - explosionRadius, y, z
                            + explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x + explosionRadius, y, z
                            - explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x + explosionRadius, y, z
                            + explosionRadius, Permissions.Build)) {
                return "Explosion would hit a protected town";
            }
        }
        return null;
    }

    /**
     * Check if a tool was used inside a town and sees if the user of the tool
     * is allowed to use it
     */
    @Override
    public String update(Resident res, Item tool, ItemStack item)
            throws Exception {
        if (clSpellScrollBase.isInstance(tool)
                && (clIDamagingSpell.isInstance(tool) || clISummonCreature
                        .isInstance(tool))) {
            List<Entity> list = getTargets(res.onlinePlayer.worldObj,
                    res.onlinePlayer.getLook(17), res.onlinePlayer, 8);
            for (Entity e : list) {
                if (!res.canAttack(e)) {
                    return "Cannot attack here";
                }
            }
        }
        return null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return clEntitySpellProjectile.isInstance(e);
    }

    @Override
    public boolean isEntityInstance(Item e) {
        return clSpellScrollBase.isInstance(e);
    }

    /**
     * All it does it gets a list of people in the range of something (a spell
     * for instance)
     */
    private List<Entity> getTargets(World world, Vec3 tvec, EntityPlayer p,
            double range) {
        Entity pointedEntity = null;
        Vec3 vec3d = world.getWorldVec3Pool().getVecFromPool(p.posX, p.posY,
                p.posZ);
        Vec3 vec3d2 = vec3d.addVector(tvec.xCoord * range, tvec.yCoord * range,
                tvec.zCoord * range);
        float f1 = 1.0F;
        List<?> list = world.getEntitiesWithinAABBExcludingEntity(p,
                p.boundingBox.addCoord(tvec.xCoord * range,
                        tvec.yCoord * range, tvec.zCoord * range).expand(f1,
                        f1, f1));

        ArrayList<Entity> l = new ArrayList<Entity>();
        for (int i = 0; i < list.size(); i++) {
            Entity entity = (Entity) list.get(i);
            if (entity.canBeCollidedWith()) {
                float f2 = Math.max(1.0F, entity.getCollisionBorderSize());
                AxisAlignedBB axisalignedbb = entity.boundingBox.expand(f2,
                        f2 * 1.25F, f2);
                MovingObjectPosition movingobjectposition = axisalignedbb
                        .calculateIntercept(vec3d, vec3d2);

                if (movingobjectposition != null) {
                    pointedEntity = entity;

                    if (pointedEntity != null
                            && p.canEntityBeSeen(pointedEntity)) {
                        l.add(pointedEntity);
                    }
                }
            }
        }

        return l;
    }

    @Override
    public String getMod() {
        return "Ars Magica";
    }

    @Override
    public String getComment() {
        return "PVP & Build check: Ars Magica Spells";
    }

}
