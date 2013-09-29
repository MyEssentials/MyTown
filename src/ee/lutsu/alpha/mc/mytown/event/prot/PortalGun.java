package ee.lutsu.alpha.mc.mytown.event.prot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import com.google.common.collect.Lists;

import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.Permissions;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;

public class PortalGun extends ProtBase {
    public static PortalGun instance = new PortalGun();

    @SuppressWarnings("rawtypes")
    Class clPortalBall = null;
    List<String> systemOwnerNames = Lists.newArrayList("", "def", "coopA", "coopB");

    // Used to iterate over a central position and the 6 blocks surrounding it
    private final static int[] xPos = { 0, 1, -1, 0, 0, 0, 0 };
    private final static int[] zPos = { 0, 0, 0, 1, -1, 0, 0 };
    private final static int[] yPos = { 0, 0, 0, 0, 0, 1, -1 };

    @Override
    public void load() throws Exception {
        clPortalBall = Class.forName("portalgun.common.entity.EntityPortalBall");
    }

    @Override
    public boolean loaded() {
        return clPortalBall != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return e.getClass() == clPortalBall;
    }

    @Override
    public String update(Entity e) throws Exception {

        Vec3 currPos = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);
        Vec3 nextPos = Vec3.createVectorHelper(e.posX + e.motionX, e.posY + e.motionY, e.posZ + e.motionZ);
        MovingObjectPosition collision = e.worldObj.clip(currPos, nextPos);

        // Not trying to make a portal yet
        if (collision == null) {
            return null;
        }

        String owner = e.getDataWatcher().getWatchableObjectString(18);

        // not default portal
        if (owner != null && !systemOwnerNames.contains(owner)) {
            if (owner.endsWith("_A") || owner.endsWith("_B")) {
                owner = owner.substring(0, owner.length() - 2);
            }

            Resident r = ProtectionEvents.instance.lastOwner = MyTownDatasource.instance.getResident(owner);
            if (r == null || !r.isOnline()) {
                return "Owner " + owner + " not found or offline";
            }

            for (int i = 0; i < 7; i++) {
                if (!r.canInteract(collision.blockX + xPos[i], collision.blockY + yPos[i], collision.blockZ + zPos[i], Permissions.Build)) {
                    return "Cannot shoot portals in this town";
                }
            }
        } else {
            Set<ChunkCoord> chunks = new HashSet<ChunkCoord>();
            for (int i = 0; i < 5; i++) {
                chunks.add(ChunkCoord.getCoord(collision.blockX + xPos[i], collision.blockZ + zPos[i]));
            }

            for (ChunkCoord chunk : chunks) {
                TownBlock b = MyTownDatasource.instance.getBlock(e.dimension, chunk.x, chunk.z);
                if (b != null && b.town() != null) {
                    return "Cannot use default portals in towns";
                }
            }
        }

        return null;
    }

    @Override
    public String getMod() {
        return "PortalgunMod";
    }

    @Override
    public String getComment() {
        return "Build check: EntityPortalBall. Disables non-owner balls completly in town";
    }
}
