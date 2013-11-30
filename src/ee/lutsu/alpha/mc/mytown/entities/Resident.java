package ee.lutsu.alpha.mc.mytown.entities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

import com.google.common.base.Joiner;
import com.sperion.forgeperms.ForgePerms;
import com.sperion.forgeperms.api.IChatManager;

import ee.lutsu.alpha.mc.mytown.ChatChannel;
import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
//import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.commands.CmdChat;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.ISettingsSaveHandler;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;
import ee.lutsu.alpha.mc.mytown.event.tick.WorldBorder;

public class Resident {
    public static boolean allowMemberToMemberPvp = false;
    public static int pickupSpamCooldown = 5000;

    public enum Rank {
        Resident, Assistant, Mayor;

        /**
         * Gets the rank based on [R, A, M]
         */
        public static Rank parse(String rank) {
            for (Rank type : values()) {
                if (type.toString().toLowerCase().startsWith(rank.toLowerCase())) {
                    return type;
                }
            }
            return Rank.Resident;
        }
    }

    public EntityPlayer onlinePlayer;

    private String name;
    private Town town;
    private Rank rank = Rank.Resident;
    private int id = 0;
    private Date createdOn;
    private Date lastLoginOn;

    public Town location;
    public Resident location2;
    public TownBlock checkYMovement = null;
    public boolean mapMode = false;
    public Town inviteActiveFrom;
    public ChatChannel activeChannel = null;
    public boolean beingBounced = false;
    public List<Resident> friends = new ArrayList<Resident>();
    public int extraBlocks = 0;

    public int prevDimension, prevDimension2;
    public double prevX, prevY, prevZ;
    public float prevYaw, prevPitch;
    public long pickupWarningCooldown = 0;

    public boolean firstTick = true;
    public boolean wasfirstTick = true;

    public boolean hasTown() {
        return town != null;
    }

    public void setActiveChannel(ChatChannel ch) {
        activeChannel = ch;
        save();
    }

    public void setExtraBlocks(int i) {
        extraBlocks = i;
        save();
    }

    public Town town() {
        return town;
    }

    public void setTown(Town t) {
        town = t;
        settings.setParent(t == null ? null : t.settings);
        if (t == null) {
            settings.unlinkAllDown();
        }
    }

    public Rank rank() {
        return rank;
    }

    public void setRank(Rank r) {
        rank = r;
    }

    public String name() {
        return name;
    }

    public boolean isOnline() {
        return onlinePlayer != null && !onlinePlayer.isDead;
    }

    public int id() {
        return id;
    }

    public void setId(int val) {
        id = val;
    }

    public Date created() {
        return createdOn;
    }

    public Date lastLogin() {
        return lastLoginOn;
    }

    public TownSettingCollection settings = new TownSettingCollection();
    public SavedHomeList home = new SavedHomeList(this);
    public PayHandler pay = new PayHandler(this);

    public Resident(String pName) {
        this();

        name = pName;
        createdOn = new Date(System.currentTimeMillis());
        lastLoginOn = new Date(System.currentTimeMillis());
        activeChannel = ChatChannel.defaultChannel;

        save();
    }

    protected Resident() {
        settings.tag = this;
        settings.saveHandler = new ISettingsSaveHandler() {
            @Override
            public void save(TownSettingCollection sender, Object tag) {
                Resident r = (Resident) tag;
                r.save();
            }
        };
    }

    public boolean shouldShowTownBlocks() {
        return ForgePerms.getPermissionManager().canAccess(this.name(), DimensionManager.getProvider(prevDimension).getDimensionName(), "mytown.adm.showblocks");
    }

    public boolean shouldShowPlayerLocation() {
        return ForgePerms.getPermissionManager().canAccess(this.name(), DimensionManager.getProvider(prevDimension).getDimensionName(), "mytown.adm.showlocation");
    }

    public boolean canByPassCheck(TownSettingCollection.Permissions level) {
        return ForgePerms.getPermissionManager().canAccess(this.name(), DimensionManager.getProvider(prevDimension).getDimensionName(), "mytown.adm.bypass." + level.toString().toLowerCase());
    }

    public boolean pvpBypass() {
        return ForgePerms.getPermissionManager().canAccess(this.name(), DimensionManager.getProvider(prevDimension).getDimensionName(), "mytown.adm.bypass.pvp");
    }

    public boolean canInteract(int chunkX, int chunkZ, TownSettingCollection.Permissions askedFor) {
        TownBlock block = MyTownDatasource.instance.getBlock(onlinePlayer.dimension, chunkX, chunkZ);

        return canInteract(block, askedFor);
    }

    public boolean canInteract(TownBlock block, TownSettingCollection.Permissions askedFor) {
        boolean b = canInteractSub(block, askedFor);
        if (!b && canByPassCheck(askedFor)) {
            b = true;
        }

        return b;
    }

    private boolean canInteractSub(TownBlock block, TownSettingCollection.Permissions askedFor) {
        if (block == null || block.town() == null) {
            return MyTown.instance.getWorldWildSettings(onlinePlayer.dimension).outsiderRights.compareTo(askedFor) >= 0;
        }

        if (block.owner() == this || block.town() == town() && rank() != Rank.Resident) {
            return true;
        }

        if (block.owner() != null && block.owner().friends.contains(this)) {
            return block.settings.friendRights.compareTo(askedFor) >= 0;
        }

        if (town() == block.town()) {
            return block.settings.townMemberRights.compareTo(askedFor) >= 0;
        }

        if (town() != null && town().nation() != null && town().nation() == block.town().nation()) {
            return block.settings.nationMemberRights.compareTo(askedFor) >= 0;
        }

        return block.settings.outsiderRights.compareTo(askedFor) >= 0;
    }

    public boolean canInteract(TownBlock targetBlock, int y, TownSettingCollection.Permissions askedFor) {
        if (targetBlock == null || targetBlock.town() == null) {
            return canInteract(null, askedFor);
        }

        if (targetBlock.settings.yCheckOn) {
            if (y < targetBlock.settings.yCheckFrom || y > targetBlock.settings.yCheckTo) {
                targetBlock = targetBlock.getFirstFullSidingClockwise(targetBlock.town());
            }
        }

        return canInteract(targetBlock, askedFor);
    }

    public boolean canInteract(int dimension, int x, int yFrom, int yTo, int z, TownSettingCollection.Permissions askedFor) {
        TownBlock targetBlock = MyTownDatasource.instance.getPermBlockAtCoord(dimension, x, yFrom, yTo, z);
        if (targetBlock == null || targetBlock.town() == null) {
            return MyTown.instance.getWorldWildSettings(dimension).outsiderRights.compareTo(askedFor) >= 0;
        }

        return canInteract(targetBlock, askedFor);
    }

    public boolean canInteract(int dimension, int x, int y, int z, TownSettingCollection.Permissions askedFor) {
        TownBlock targetBlock = MyTownDatasource.instance.getPermBlockAtCoord(dimension, x, y, z);
        if (targetBlock == null || targetBlock.town() == null) {
            return MyTown.instance.getWorldWildSettings(dimension).outsiderRights.compareTo(askedFor) >= 0;
        }

        return canInteract(targetBlock, askedFor);
    }

    public boolean canInteract(int x, int y, int z, TownSettingCollection.Permissions askedFor) {
        return canInteract(onlinePlayer.dimension, x, y, z, askedFor);
    }

    public boolean canInteract(Entity e) {
        TownBlock targetBlock = MyTownDatasource.instance.getPermBlockAtCoord(e.dimension, (int) e.posX, (int) e.posY, (int) e.posZ);

        if (e instanceof EntityMinecart) {
            if ((e.riddenByEntity == null || e.riddenByEntity == onlinePlayer)
                    && (targetBlock != null && targetBlock.town() != null && targetBlock.settings.allowCartInteraction || (targetBlock == null || targetBlock.town() == null) && MyTown.instance.getWorldWildSettings(e.dimension).allowCartInteraction)) {
                return true;
            }
        }

        TownSettingCollection.Permissions perm = TownSettingCollection.Permissions.Build;

        if (e instanceof EntityItem) {
            perm = TownSettingCollection.Permissions.Loot;
        } else {
            boolean isNpc = false;

            for (Class<?> cl : ProtectionEvents.instance.getNPCClasses()) {
                if (cl.isInstance(e)) {
                    isNpc = true;
                }
            }

            if (isNpc) {
                perm = TownSettingCollection.Permissions.Access;
            }
        }

        return canInteract(targetBlock, perm);
    }

    public boolean canAttack(Entity e) {
        if (e instanceof EntityPlayer) {
            if (pvpBypass()) {
                return true;
            }

            TownBlock targetBlock = MyTownDatasource.instance.getBlock(onlinePlayer.dimension, e.chunkCoordX, e.chunkCoordZ);

            if (Town.pvpSafeTowns != null && targetBlock != null && targetBlock.town() != null) {
                for (String s : Town.pvpSafeTowns) {
                    if (targetBlock.town().name().equals(s)) {
                        Log.log(Level.INFO, "Found safe town: %s", s);
                        return false;
                    }
                }
            }

            // disable friendly fire
            if (!allowMemberToMemberPvp && town() != null && MyTownDatasource.instance.getOrMakeResident((EntityPlayer) e).town() == town()) {
                return false;
            }

            if (Town.allowFullPvp == true) {
                return true;
            }

            if (targetBlock != null && targetBlock.town() != null) {
                if (targetBlock.settings.yCheckOn) {
                    int y = (int) e.posY;
                    if (y < targetBlock.settings.yCheckFrom || y > targetBlock.settings.yCheckTo) {
                        targetBlock = targetBlock.getFirstFullSidingClockwise(targetBlock.town());
                    }
                }

                if (targetBlock != null) {
                    return Town.allowMemberToForeignPvp && town() == targetBlock.town();
                }
            }
            TownBlock sourceBlock = MyTownDatasource.instance.getBlock(onlinePlayer.dimension, onlinePlayer.chunkCoordX, onlinePlayer.chunkCoordZ);
            if (sourceBlock != null && sourceBlock.town() != null) {
                if (sourceBlock.settings.yCheckOn) {
                    int y = (int) e.posY;
                    if (y < sourceBlock.settings.yCheckFrom || y > sourceBlock.settings.yCheckTo) {
                        sourceBlock = sourceBlock.getFirstFullSidingClockwise(sourceBlock.town());
                    }
                }

                if (sourceBlock != null) {
                    return Town.allowMemberToForeignPvp && town() == sourceBlock.town();
                }
            }

            return true;
        } else {
            TownBlock targetBlock = MyTownDatasource.instance.getPermBlockAtCoord(e.dimension, (int) e.posX, (int) e.posY, (int) e.posZ);

            if (e instanceof EntityMinecart) {
                if (targetBlock != null && targetBlock.town() != null && targetBlock.settings.allowCartInteraction || (targetBlock == null || targetBlock.town() == null) && MyTown.instance.getWorldWildSettings(e.dimension).allowCartInteraction) {
                    return true;
                }
            } else if (e instanceof IMob) {
                if (targetBlock != null && targetBlock.town() != null && targetBlock.settings.allowKillingMobsByNonResidents) {
                    return true;
                }
            }

            return canInteract(targetBlock, TownSettingCollection.Permissions.Build);
        }
    }

    public void sendLocationMap(int dim, int cx, int cz) {
        int heightRad = 4;
        int widthRad = 9;
        StringBuilder sb = new StringBuilder();
        String c;

        MyTown.sendChatToPlayer(onlinePlayer, Term.TownMapHead.toString());
        for (int z = cz - heightRad; z <= cz + heightRad; z++) {
            sb.setLength(0);
            for (int x = cx - widthRad; x <= cx + widthRad; x++) {
                TownBlock b = MyTownDatasource.instance.getBlock(dim, x, z);

                boolean mid = z == cz && x == cx;
                boolean isTown = b != null && b.town() != null;
                boolean ownTown = isTown && b.town() == town;
                boolean takenPlot = ownTown && b.owner() != null;
                boolean ownPlot = takenPlot && b.owner() == this;

                if (mid) {
                    c = ownPlot ? "§e" : ownTown ? "§a" : isTown ? "§c" : "§f";
                } else {
                    c = ownPlot ? "§6" : ownTown ? "§2" : isTown ? "§4" : "§7";
                }

                c += takenPlot ? "X" : isTown ? "O" : "_";

                sb.append(c);
            }
            MyTown.sendChatToPlayer(onlinePlayer, sb.toString());
        }
    }

    public String prefix() {
        String w = onlinePlayer != null ? String.valueOf(onlinePlayer.dimension) : null;

        String prefix;
        IChatManager chatManager = ForgePerms.getChatManager();
        if (chatManager == null){
            Log.info("Chat Manager is null!");
            return "";
        } else{
            prefix = chatManager.getPlayerPrefix(w, name());
        }
        if (prefix != null) {
            prefix = Formatter.applyColorCodes(prefix);
        }
        return prefix;
    }

    public String postfix() {
        String w = onlinePlayer != null ? String.valueOf(onlinePlayer.dimension) : null;

        String postfix;
        IChatManager chatManager = ForgePerms.getChatManager();
        if (chatManager == null){
            Log.info("Chat Manager is null!");
            return "";
        } else{
            postfix = chatManager.getPlayerSuffix(w, name());
        }
        if (postfix != null) {
            postfix = Formatter.applyColorCodes(postfix);
        }
        return postfix;
    }

    public static Resident loadFromDB(int id, String name, Town town, Rank r, ChatChannel c, Date created, Date lastLogin, String extra, String home) {
        Resident res = new Resident();
        res.name = name;
        res.id = id;
        res.town = town;
        res.rank = r;
        res.activeChannel = c;
        res.createdOn = created;
        res.lastLoginOn = lastLogin;
        res.home.deserialize(home);

        if (town != null) {
            town.residents().add(res);
        }

        res.settings.setParent(town == null ? null : town.settings);

        if (extra != null && !extra.equals("")) {
            String[] extraParts = extra.split("\\|");
            res.settings.deserialize(extraParts[0]);

            if (extraParts.length > 1) {
                res.extraBlocks = Integer.parseInt(extraParts[1]);
            }
        }

        return res;
    }

    public String serializeExtra() {
        return settings.serialize() + "|" + String.valueOf(extraBlocks);
    }

    public void checkLocation() {
        if (beingBounced) {
            return;
        }

        MyTownDatasource source = MyTownDatasource.instance;

        int pX = ChunkCoord.getCoord(onlinePlayer.posX);
        int pZ = ChunkCoord.getCoord(onlinePlayer.posZ);
        TownBlock block = checkYMovement;

        if (block == null) {
            block = source.getBlock(onlinePlayer.dimension, pX, pZ);
        }

        if (block == null && location != null) {
            // entered wild
            MyTown.sendChatToPlayer(onlinePlayer, Term.PlayerEnteredWild.toString());
            location = null;
            location2 = null;
            checkYMovement = null;
        } else if (block != null && block.town() != null) {
            // entered town or another town
            if (!canInteract(block, (int) onlinePlayer.posY, TownSettingCollection.Permissions.Enter)) {
                beingBounced = true;
                try {
                    MyTown.sendChatToPlayer(onlinePlayer, Term.TownYouCannotEnter.toString(block.town().name()));
                    bounceBack();

                    pX = ChunkCoord.getCoord(onlinePlayer.posX);
                    pZ = ChunkCoord.getCoord(onlinePlayer.posZ);

                    TownBlock block2 = source.getBlock(onlinePlayer.dimension, pX, pZ);
                    if (block2 != null && block2.town() != null && !canInteract(block2, (int) onlinePlayer.posY, TownSettingCollection.Permissions.Enter)) {
                        // bounce failed, send to spawn
                        Log.warning(String.format("Player %s is inside a enemy town %s (%s, %s, %s, %s) with bouncing on. Sending to spawn.", name(), block2.town().name(), onlinePlayer.dimension, onlinePlayer.posX, onlinePlayer.posY, onlinePlayer.posZ));

                        respawnPlayer();
                    }
                } finally {
                    beingBounced = false;
                }
            } else {
                checkYMovement = block.settings.yCheckOn ? block : null;

                if (block.owner() != location2 || block.town() != location) {
                    if (block.town() != location) {
                        if (block.town() == town()) {
                            MyTown.sendChatToPlayer(onlinePlayer, Term.PlayerEnteredOwnTown.toString(block.town().name()));
                        } else {
                            MyTown.sendChatToPlayer(onlinePlayer, Term.PlayerEnteredTown.toString(block.town().name()));
                        }
                    }

                    if (block.owner() == this) {
                        MyTown.sendChatToPlayer(onlinePlayer, Term.PlayerEnteredOwnPlot.toString(block.owner().name()));
                    } else if (block.owner() != null) {
                        MyTown.sendChatToPlayer(onlinePlayer, Term.PlayerEnteredOwnPlot.toString(block.owner().name()));
                    } else {
                        MyTown.sendChatToPlayer(onlinePlayer, Term.PlayerEnteredUnclaimedPlot.toString());
                    }

                    location = block.town();
                    location2 = block.owner();
                }
            }
        }
    }

    public void bounceBack() {
        if (wasfirstTick) {
            return;
        }

        if (onlinePlayer instanceof EntityPlayerMP) {
            if (onlinePlayer.dimension != prevDimension) {
                MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) onlinePlayer, prevDimension);
            }

            if (onlinePlayer.ridingEntity != null) {
                // reversing boats
                Entity e = onlinePlayer.ridingEntity;

                e.motionX *= -1;
                e.motionY *= -1;
                e.motionZ *= -1;
                e.setPosition(prevX, prevY, prevZ);

                onlinePlayer.motionX *= -1;
                onlinePlayer.motionY *= -1;
                onlinePlayer.motionZ *= -1;

                // onlinePlayer.mountEntity(e); // unomunts
            }

            ((EntityPlayerMP) onlinePlayer).playerNetServerHandler.setPlayerLocation(prevX, prevY, prevZ, prevYaw, prevPitch);
        } else {
            throw new RuntimeException("Cannot bounce non multiplayer players");
        }
    }

    public void respawnPlayer() {
        respawnPlayer(null);
    }

    public void respawnPlayer(SavedHome h) {
        if (!(onlinePlayer instanceof EntityPlayerMP)) {
            throw new RuntimeException("Cannot move a non-player");
        }

        EntityPlayerMP pl = (EntityPlayerMP) onlinePlayer;
        WorldServer world = null;

        if (h == null) {
            if (pl.dimension != pl.worldObj.provider.getRespawnDimension(pl)) {
                MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(pl, pl.worldObj.provider.getRespawnDimension(pl));
            }

            world = MinecraftServer.getServer().worldServerForDimension(pl.dimension);
            ChunkCoordinates c = pl.getBedLocation(pl.dimension);
            boolean forcedSpawn = pl.isSpawnForced(pl.dimension);

            if (c != null) {
                c = EntityPlayer.verifyRespawnCoordinates(world, c, forcedSpawn);
            }

            if (c != null) {
                pl.setLocationAndAngles(c.posX + 0.5F, c.posY + 0.1F, c.posZ + 0.5F, 0.0F, 0.0F);
            } else {
                MyTown.sendChatToPlayer(pl, Term.NoBedMessage.toString());
                WorldInfo info = world.getWorldInfo();
                pl.setLocationAndAngles(info.getSpawnX() + 0.5F, info.getSpawnY() + 0.1F, info.getSpawnZ() + 0.5F, 0, 0);
            }
        } else {
            if (pl.dimension != h.dim) {
                MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(pl, h.dim);
            }

            world = MinecraftServer.getServer().worldServerForDimension(pl.dimension);
            pl.setLocationAndAngles(h.x, h.y, h.z, h.look1, h.look2);
        }

        world.theChunkProviderServer.loadChunk((int) pl.posX >> 4, (int) pl.posZ >> 4);

        while (!world.getCollidingBoundingBoxes(pl, pl.boundingBox).isEmpty()) {
            pl.setPosition(pl.posX, pl.posY + 1.0D, pl.posZ);
        }

        pl.playerNetServerHandler.setPlayerLocation(pl.posX, pl.posY, pl.posZ, pl.rotationYaw, pl.rotationPitch);
    }

    public void sendToTownSpawn(Town t) {
        if (!(onlinePlayer instanceof EntityPlayerMP)) {
            throw new RuntimeException("Cannot move a non-player");
        }

        if (t.spawnBlock == null || t.getSpawn() == null) {
            throw new RuntimeException("Town doesn't have a spawn");
        }

        EntityPlayerMP pl = (EntityPlayerMP) onlinePlayer;

        if (pl.dimension != t.getSpawnDimension()) {
            MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(pl, t.getSpawnDimension());
        }

        Vec3 pos = t.getSpawn();
        pl.playerNetServerHandler.setPlayerLocation(pos.xCoord, pos.yCoord, pos.zCoord, t.getSpawnEye2(), t.getSpawnEye1());
    }

    public void sendToServerSpawn() {
        if (!(onlinePlayer instanceof EntityPlayerMP)) {
            throw new RuntimeException("Cannot move a non-player");
        }

        EntityPlayerMP pl = (EntityPlayerMP) onlinePlayer;

        if (pl.dimension != 0) {
            MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(pl, 0);
        }

        WorldServer world = MinecraftServer.getServer().worldServerForDimension(pl.dimension);
        WorldInfo info = world.getWorldInfo();
        pl.setLocationAndAngles(info.getSpawnX() + 0.5F, info.getSpawnY() + 0.1F, info.getSpawnZ() + 0.5F, 0, 0);

        world.theChunkProviderServer.loadChunk((int) pl.posX >> 4, (int) pl.posZ >> 4);

        while (!world.getCollidingBoundingBoxes(pl, pl.boundingBox).isEmpty()) {
            pl.setPosition(pl.posX, pl.posY + 1.0D, pl.posZ);
        }

        pl.playerNetServerHandler.setPlayerLocation(pl.posX, pl.posY, pl.posZ, pl.rotationYaw, pl.rotationPitch);
    }

    public void save() {
        MyTownDatasource.instance.saveResident(this);
    }

    public void checkWorldBorderLocation() {
        if (WorldBorder.instance.enabled) {
            if ((int) onlinePlayer.posX != (int) prevX || (int) onlinePlayer.posZ != (int) prevZ) {
                if (!WorldBorder.instance.isWithinArea(onlinePlayer)) {
                    beingBounced = true;
                    try {
                        MyTown.sendChatToPlayer(onlinePlayer, Term.OutofBorderCannotEnter.toString());
                        bounceBack();

                        if (!WorldBorder.instance.isWithinArea(onlinePlayer)) {
                            // bounce failed, send to spawn
                            Log.warning(String.format("Player %s is over the edge of the world %s (%s, %s, %s). Sending to spawn.", name(), onlinePlayer.dimension, onlinePlayer.posX, onlinePlayer.posY, onlinePlayer.posZ));

                            respawnPlayer();
                        }
                    } finally {
                        beingBounced = false;
                    }
                }
            }
        }
    }

    /**
     * Called by LivingUpdateEvent
     */
    public void update() {
        if (beingBounced) {
            return;
        }

        if (teleportToSpawnStamp != 0) {
            if (teleportToSpawnStamp <= System.currentTimeMillis()) {
                asyncEndSpawnTeleport();
            } else if ((int) onlinePlayer.posX != (int) prevX || (int) onlinePlayer.posZ != (int) prevZ || (int) onlinePlayer.posY != (int) prevY) {
                asyncResetSpawnTeleport();
            }
        }

        wasfirstTick = false;

        if (firstTick) {
            firstTick = false;
            wasfirstTick = true;
        } else {
            checkWorldBorderLocation();

            prevDimension = onlinePlayer.dimension;
            prevX = onlinePlayer.posX;
            prevY = onlinePlayer.posY;
            prevZ = onlinePlayer.posZ;
            prevYaw = onlinePlayer.rotationYaw;
            prevPitch = onlinePlayer.rotationPitch;
        }
    }

    public String formattedName() {
        return prefix() + name() + postfix();
    }

    public void sendInfoTo(ICommandSender cs, boolean adminInfo) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<String> fnames = new ArrayList<String>();
        List<String> fnames2 = new ArrayList<String>();

        for (Resident r : friends) {
            fnames.add(Formatter.formatResidentName(r));
        }

        for (Resident r : MyTownDatasource.instance.residents.values()) {
            if (r.friends.contains(this)) {
                fnames2.add(Formatter.formatResidentName(r));
            }
        }

        String sFriends = Joiner.on("§2, ").join(fnames);
        String sFriends2 = Joiner.on("§2, ").join(fnames2);

        MyTown.sendChatToPlayer(cs, Term.ResStatusName.toString(Formatter.formatResidentName(this)));

        if (adminInfo && isOnline()) {
            MyTown.sendChatToPlayer(cs, Term.ResStatusLocation.toString(location != null ? location.name() : "wild", onlinePlayer.dimension, (int) onlinePlayer.posX, (int) onlinePlayer.posY, (int) onlinePlayer.posZ));
        }

        MyTown.sendChatToPlayer(cs, Term.ResStatusGeneral1.toString(format.format(createdOn)));
        MyTown.sendChatToPlayer(cs, Term.ResStatusGeneral2.toString(isOnline() ? "online" : format.format(lastLoginOn)));
        MyTown.sendChatToPlayer(cs, Term.ResStatusTown.toString(town == null ? "none" : town().name(), town == null ? "Loner" : rank.toString()));

        if (fnames.size() > 0) {
            MyTown.sendChatToPlayer(cs, Term.ResStatusFriends.toString(sFriends));
        }
        if (fnames2.size() > 0) {
            MyTown.sendChatToPlayer(cs, Term.ResStatusFriends2.toString(sFriends2));
        }

    }

    public void loggedIn() {
        lastLoginOn = new Date(System.currentTimeMillis());
        save();
    }

    public void loggedOf() {
        firstTick = true;
        onlinePlayer = null;
        lastLoginOn = new Date(System.currentTimeMillis());
        save();
    }

    public boolean addFriend(Resident r) {
        for (Resident res : friends) {
            if (res == r) {
                return false;
            }
        }

        friends.add(r);
        save();
        return true;
    }

    public boolean removeFriend(Resident r) {
        if (friends.remove(r)) {
            save();
            return true;
        } else {
            return false;
        }
    }

    private SavedHome teleportTargetHome = null;
    private long teleportToSpawnStamp = 0;
    public static long teleportToSpawnWaitSeconds = 1 * 60; // 1 minute
    public static long teleportToHomeWaitSeconds = 1 * 60; // 1 minute

    public void asyncStartSpawnTeleport(SavedHome home) {
        teleportTargetHome = home;

        System.currentTimeMillis();
        long takesTime = ForgePerms.getPermissionManager().canAccess(this.name(), DimensionManager.getProvider(prevDimension).getDimensionName(), "mytown.adm.bypass.teleportwait") ? 0 : home != null ? teleportToHomeWaitSeconds * 1000 : teleportToSpawnWaitSeconds * 1000;

        teleportToSpawnStamp = System.currentTimeMillis() + takesTime;

        if (takesTime > 0) {
            CmdChat.sendChatToAround(onlinePlayer.dimension, onlinePlayer.posX, onlinePlayer.posY, onlinePlayer.posZ, (home != null ? Term.HomeCmdTeleportNearStarted : Term.SpawnCmdTeleportNearStarted).toString(name(), (int) Math.ceil(takesTime / 1000)), onlinePlayer);

            MyTown.sendChatToPlayer(onlinePlayer, (home != null ? Term.HomeCmdTeleportStarted : Term.SpawnCmdTeleportStarted).toString((int) Math.ceil(takesTime / 1000)));
        }
    }

    private void asyncResetSpawnTeleport(){ // when the player moved
        if (!isOnline()) {
            return;
        }

        teleportToSpawnStamp = 0;
        MyTown.sendChatToPlayer(onlinePlayer, Term.SpawnCmdTeleportReset.toString());
    }

    private void asyncEndSpawnTeleport(){ // time out, move it
        if (!isOnline()) {
            return;
        }

        teleportToSpawnStamp = 0;

        if (teleportTargetHome != null) {
            respawnPlayer(teleportTargetHome);
        } else {
            sendToServerSpawn();
        }

        MyTown.sendChatToPlayer(onlinePlayer, Term.SpawnCmdTeleportEnded.toString());
    }

    public boolean canBeAttackedBy(Entity e) {
        if (e instanceof EntityGolem) // player controlled entities
        {
            // are we in our own town?
            TownBlock targetBlock = MyTownDatasource.instance.getPermBlockAtCoord(onlinePlayer.dimension, (int) onlinePlayer.posX, (int) onlinePlayer.posY, (int) onlinePlayer.posZ);
            if (targetBlock != null && targetBlock.town() != null) {
                return Town.allowFullPvp || town() != targetBlock.town();
            }
        }

        return true;
    }

    public static Resident getOrMake(EntityPlayer pl) {
        return MyTownDatasource.instance.getOrMakeResident(pl);
    }
}
