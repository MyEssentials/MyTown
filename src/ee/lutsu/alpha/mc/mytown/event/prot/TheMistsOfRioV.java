package ee.lutsu.alpha.mc.mytown.event.prot;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;

public class TheMistsOfRioV extends ProtBase {
    public static TheMistsOfRioV instance = new TheMistsOfRioV();
    public int explosionRadius = 3 + 2;

    private Class<?> clDarkMatter = null;

    @Override
    public void load() throws Exception {
        clDarkMatter = Class.forName("sheenrox82.RioV.src.entity.projectile.EntityDarkMatter");
    }

    @Override
    public boolean loaded() {
        return clDarkMatter != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return clDarkMatter.isInstance(e);
    }

    @Override
    public String update(Entity e) throws Exception {
        
    	if (clDarkMatter.isInstance(e)) {
            EntityThrowable t = (EntityThrowable) e;
            EntityLivingBase owner = t.getThrower();

            if (owner == null || !(owner instanceof EntityPlayer)) {
                return "No owner or is not a player";
            }

            Resident thrower = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident((EntityPlayer) owner);

            int x = (int) (t.posX + t.motionX);
            int y = (int) (t.posY + t.motionY);
            int z = (int) (t.posZ + t.motionZ);
            int dim = thrower.onlinePlayer.dimension;

            if (!thrower.canInteract(dim, x - explosionRadius, y, z - explosionRadius, Permissions.Build) || !thrower.canInteract(dim, x - explosionRadius, y, z + explosionRadius, Permissions.Build) || !thrower.canInteract(dim, x + explosionRadius, y, z - explosionRadius, Permissions.Build)
                    || !thrower.canInteract(dim, x + explosionRadius, y, z + explosionRadius, Permissions.Build)) {
                return "Explosion would hit a protected town";
            }
        }

        return null;
    }

    @Override
    public String getMod() {
        return "The Mists Of RioV";
    }

    @Override
    public String getComment() {
        return "Build check: EntityDarkMatter";
    }
}
