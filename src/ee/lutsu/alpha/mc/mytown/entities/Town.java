package ee.lutsu.alpha.mc.mytown.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

import com.sperion.forgeperms.ForgePerms;

import ee.lutsu.alpha.mc.mytown.ChunkCoord;
import ee.lutsu.alpha.mc.mytown.CommandException;
import ee.lutsu.alpha.mc.mytown.Formatter;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
//import ee.lutsu.alpha.mc.mytown.Permissions;
import ee.lutsu.alpha.mc.mytown.Term;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.ISettingsSaveHandler;

public class Town {
    public static int minDistanceFromOtherTown = 5;
    public static int dontSendCartNotification = 5000;
    public static boolean allowFullPvp = false;
    public static boolean allowMemberToForeignPvp = true;
    public static String[] pvpSafeTowns = new String[0];

    private int id;
    private String name;
    private int extraBlocks;
    private List<Resident> residents = new ArrayList<Resident>();
    private List<TownBlock> blocks;
    private Nation nation;
    private Vec3 spawnLocation;
    private float spawnEye1, spawnEye2;
    private int spawnDimension;
    public TownBlock spawnBlock;
    public Nation pendingNationInvitation = null;

    public long minecraftNotificationTime = 0;

    public String name() {
        return name;
    }

    public int id() {
        return id;
    }

    public int extraBlocks() {
        return extraBlocks;
    }

    public List<Resident> residents() {
        return residents;
    }

    public List<TownBlock> blocks() {
        return blocks;
    }

    public Nation nation() {
        return nation;
    }

    public void setId(int val) {
        id = val;
    }

    public void setExtraBlocks(int val) {
        extraBlocks = val;
        save();
    }

    public void sqlSetExtraBlocks(int val) {
        extraBlocks = val;
    }

    public void setNation(Nation n) {
        nation = n;
    } // used internally only

    public TownSettingCollection settings = new TownSettingCollection();

    public int getSpawnDimension() {
        return spawnDimension;
    }

    public Vec3 getSpawn() {
        return spawnLocation;
    }

    public float getSpawnEye1() {
        return spawnEye1;
    }

    public float getSpawnEye2() {
        return spawnEye2;
    }

    public void setSpawn(TownBlock b, Vec3 loc, float eye1, float eye2) {
        spawnBlock = b;
        spawnDimension = b.worldDimension();
        spawnLocation = loc;
        spawnEye1 = eye1;
        spawnEye2 = eye2;
        save();
    }

    public void resetSpawn() {
        spawnBlock = null;
        spawnLocation = null;
        spawnDimension = 0;
        spawnEye1 = 0;
        spawnEye2 = 0;
        save();
    }

    public static void assertNewTownParams(String pName, Resident creator,
            TownBlock home) throws CommandException {
        if (creator.town() != null) {
            throw new CommandException(Term.TownErrCreatorPartOfTown);
        }

        canSetName(pName, null);
        if (home != null) {
            canAddBlock(home, true, null);
        }
    }

    public Town(String pName, Resident creator, TownBlock home)
            throws CommandException {
        assertNewTownParams(pName, creator, home);

        id = -1;
        name = pName;

        residents = new ArrayList<Resident>();
        blocks = new ArrayList<TownBlock>();

        residents.add(creator);
        creator.setTown(this);
        creator.setRank(Rank.Mayor);

        if (home != null) {
            home.setTown(this);
            blocks.add(home);
        }

        setSettings();
        MyTownDatasource.instance.addTown(this);
        save(); // has town id now
        creator.save();
    }

    /**
     * Used by SQL loading
     */
    public Town(int pId, String pName, int pExtraBlocks,
            List<TownBlock> pBlocks, String extra) {
        id = pId;
        name = pName;
        extraBlocks = pExtraBlocks;
        blocks = pBlocks;

        setSettings();
        deserializeExtra(extra);

        for (TownBlock res : blocks) {
            res.setTown(this); // needs parent settings to be on
        }
    }

    private void setSettings() {
        settings.tag = this;
        settings.setParent(MyTown.instance.serverSettings);
        settings.saveHandler = new ISettingsSaveHandler() {
            @Override
            public void save(TownSettingCollection sender, Object tag) {
                Town r = (Town) tag;
                r.save();
            }
        };
    }

    public Resident getFirstMayor() {
        for (Resident r : residents) {
            if (r.rank() == Rank.Mayor) {
                return r;
            }
        }

        return null;
    }

    public int blocksPerResident() {
        Resident mayor = getFirstMayor();
        if (mayor == null) {
            return 1;
        }

        if (ForgePerms.getPermissionsHandler().canAccess(mayor.name(),
                mayor.onlinePlayer.worldObj.provider.getDimensionName(),
                "mytown.mayor.blocks.32")) {
            return 32;
        }
        if (ForgePerms.getPermissionsHandler().canAccess(mayor.name(),
                mayor.onlinePlayer.worldObj.provider.getDimensionName(),
                "mytown.mayor.blocks.16")) {
            return 16;
        }
        if (ForgePerms.getPermissionsHandler().canAccess(mayor.name(),
                mayor.onlinePlayer.worldObj.provider.getDimensionName(),
                "mytown.mayor.blocks.8")) {
            return 8;
        }
        if (ForgePerms.getPermissionsHandler().canAccess(mayor.name(),
                mayor.onlinePlayer.worldObj.provider.getDimensionName(),
                "mytown.mayor.blocks.4")) {
            return 4;
        }
        if (ForgePerms.getPermissionsHandler().canAccess(mayor.name(),
                mayor.onlinePlayer.worldObj.provider.getDimensionName(),
                "mytown.mayor.blocks.2")) {
            return 2;
        }

        return 1;
    }

    public int residentBlockMultiplier(Resident res) {
        if (res == null) {
            return 1;
        }

        if (ForgePerms.getPermissionsHandler().canAccess(res.name(),
                res.onlinePlayer.worldObj.provider.getDimensionName(),
                "mytown.resident.blocksmulti.10")) {
            return 10;
        }
        if (ForgePerms.getPermissionsHandler().canAccess(res.name(),
                res.onlinePlayer.worldObj.provider.getDimensionName(),
                "mytown.resident.blocksmulti.8")) {
            return 8;
        }
        if (ForgePerms.getPermissionsHandler().canAccess(res.name(),
                res.onlinePlayer.worldObj.provider.getDimensionName(),
                "mytown.resident.blocksmulti.4")) {
            return 4;
        }
        if (ForgePerms.getPermissionsHandler().canAccess(res.name(),
                res.onlinePlayer.worldObj.provider.getDimensionName(),
                "mytown.resident.blocksmulti.2")) {
            return 2;
        }

        return 1;
    }

    public int perResidentBlocks() {
        int b = 0;
        int perRes = blocksPerResident();
        for (Resident r : residents) {
            b += perRes * residentBlockMultiplier(r) + r.extraBlocks;
        }

        return b;
    }

    public int totalBlocks() {
        return perResidentBlocks() + extraBlocks
                + (nation() == null ? 0 : nation().getTotalExtraBlocks(this));
    }

    public int freeBlocks() {
        return totalBlocks() - blocks.size();
    }

    public void setResidentRank(Resident res, Rank r) {
        res.setRank(r);
        res.save();
    }

    public void setTownName(String newName) throws CommandException {
        canSetName(newName, this);

        name = newName;
        save();
    }

    public static void canSetName(String name, Town self)
            throws CommandException {
        if (name == null || name.equals("")) {
            throw new CommandException(Term.TownErrTownNameCannotBeEmpty);
        }

        for (Town t : MyTownDatasource.instance.towns) {
            if (t != self && t.name.equalsIgnoreCase(name)) {
                throw new CommandException(Term.TownErrTownNameAlreadyInUse);
            }
        }
    }

    public static void canAddBlock(TownBlock block, boolean ignoreRoomCheck,
            Town self) throws CommandException {
        if (block.town() != null) {
            throw new CommandException(Term.TownErrAlreadyClaimed);
        }

        int sqr = minDistanceFromOtherTown * minDistanceFromOtherTown;
        for (TownBlock b : MyTownDatasource.instance.blocks) {
            if (b != block
                    && b.town() != null
                    && b.town() != self
                    && b.worldDimension() == block.worldDimension()
                    && (b.town().nation() == null || self != null
                            && b.town().nation() != self.nation())
                    && block.squaredDistanceTo(b) <= sqr
                    && !b.settings.allowClaimingNextTo) {
                throw new CommandException(
                        Term.TownErrBlockTooCloseToAnotherTown);
            }
        }

        if (!ignoreRoomCheck && self != null && self.freeBlocks() < 1) {
            throw new CommandException(Term.TownErrNoFreeBlocks);
        }
    }

    public void addBlock(TownBlock block) throws CommandException {
        addBlock(block, false);
    }

    public void addBlock(TownBlock block, boolean bypassChecks)
            throws CommandException {
        if (!bypassChecks) {
            canAddBlock(block, false, this);
        }

        block.setTown(this);
        blocks.add(block);
        save();
    }

    public void addResident(Resident res) throws CommandException {
        if (res.town() != null) {
            throw new CommandException(Term.TownErrPlayerAlreadyInTown);
        }

        res.setTown(this);
        residents.add(res);
        res.save();
    }

    public void removeBlocks(List<TownBlock> b) throws CommandException {
        for (TownBlock block : b) {
            if (block.town() == null || block.town() != this) {
                throw new CommandException(Term.TownErrAlreadyClaimed);
            }

            if (spawnBlock == block) {
                resetSpawn();
            }

            block.setTown(null);
            blocks.remove(block);
            MyTownDatasource.instance.unloadBlock(block);
        }
        save();
    }

    public void removeBlockUnsafe(TownBlock block) {
        if (spawnBlock == block) {
            resetSpawn();
        }

        block.setTown(null);
        blocks.remove(block);
        MyTownDatasource.instance.unloadBlock(block);
    }

    public void removeBlock(TownBlock block) throws CommandException {
        if (block.town() == null || block.town() != this) {
            throw new CommandException(Term.TownErrNotClaimedByYourTown);
        }

        removeBlockUnsafe(block);

        save();
    }

    public void removeResident(Resident res) {
        res.setTown(null); // unlinks plots
        res.setRank(Rank.Resident);
        residents.remove(res);
        res.save();

        boolean town_change = false;
        for (TownBlock b : blocks) {
            if (b.owner() == res) {
                b.sqlSetOwner(null); // sets settings parent to town
                town_change = true;
            }
        }

        if (town_change) {
            save(); // saves block owner to null
        }
    }

    public void deleteTown() throws CommandException {
        if (nation() != null) {
            throw new CommandException(Term.TownErrCannotDeleteInNation);
        }

        for (Resident res : residents) {
            res.setTown(null);
        }

        residents.clear();
        spawnBlock = null;

        for (TownBlock block : blocks) {
            block.setTown(null);
            MyTownDatasource.instance.unloadBlock(block);
        }
        blocks.clear();

        settings.unlinkAllDown();
        MyTownDatasource.instance.deleteTown(this); // sets resident town to 0
        MyTownDatasource.instance.unloadTown(this);
    }

    public void save() {
        MyTownDatasource.instance.saveTown(this);
    }

    public void sendNotification(Level lvl, String msg) {
        String formatted = Formatter.townNotification(lvl, msg);
        for (Resident r : residents) {
            if (!r.isOnline()) {
                continue;
            }

            MyTown.sendChatToPlayer(r.onlinePlayer, formatted);
        }
    }

    public String serializeExtra() {
        return settings.serialize()
                + ";"
                + (spawnLocation == null ? "" : spawnDimension + "/"
                        + spawnLocation.xCoord + "/" + spawnLocation.yCoord
                        + "/" + spawnLocation.zCoord + "/" + spawnEye1 + "/"
                        + spawnEye2);
    }

    public void deserializeExtra(String val) {
        String[] parts = val.split(";");
        if (parts.length > 0) {
            settings.deserialize(parts[0]);
        }

        if (parts.length > 1 && parts[1].trim().length() > 0) {
            String[] location = parts[1].split("/");
            spawnDimension = Integer.parseInt(location[0]);

            double x = Double.parseDouble(location[1]);
            double y = Double.parseDouble(location[2]);
            double z = Double.parseDouble(location[3]);
            float a = Float.parseFloat(location[4]);
            float b = Float.parseFloat(location[5]);

            spawnLocation = Vec3.createVectorHelper(x, y, z);
            spawnEye1 = a;
            spawnEye2 = b;

            int cx = ChunkCoord.getCoord(x);
            int cz = ChunkCoord.getCoord(z);

            for (TownBlock r : blocks) {
                if (r.equals(spawnDimension, cx, cz)) {
                    spawnBlock = r;
                }
            }

            if (spawnBlock == null) {
                spawnLocation = null;
            }
        } else {
            spawnDimension = 0;
            spawnLocation = null;
            spawnEye1 = 0;
            spawnEye2 = 0;
            spawnBlock = null;
        }
    }

    public void sendTownInfo(ICommandSender pl, boolean adminInfo) {
        Town t = this;

        StringBuilder mayors = new StringBuilder();
        StringBuilder assistants = new StringBuilder();
        StringBuilder residents = new StringBuilder();

        for (Resident r : t.residents()) {
            if (r.rank() == Rank.Mayor) {
                if (mayors.length() > 0) {
                    mayors.append("§2, ");
                }
                mayors.append(Formatter.formatResidentName(r));
            } else if (r.rank() == Rank.Assistant) {
                if (assistants.length() > 0) {
                    assistants.append("§2, ");
                }
                assistants.append(Formatter.formatResidentName(r));
            } else if (r.rank() == Rank.Resident) {
                if (residents.length() > 0) {
                    residents.append("§2, ");
                }
                residents.append(Formatter.formatResidentName(r));
            }
        }

        if (mayors.length() < 1) {
            mayors.append("none");
        }
        if (assistants.length() < 1) {
            assistants.append("none");
        }
        if (residents.length() < 1) {
            residents.append("none");
        }

        StringBuilder blocks_list = new StringBuilder();

        if (adminInfo) {
            for (TownBlock block : blocks) {
                if (blocks_list.length() > 0) {
                    blocks_list.append(", ");
                }
                blocks_list.append(String.format("(%s,%s)", block.x(), block
                        .z()));
            }
        }

        String townColor = "§2";
        if (pl instanceof EntityPlayer) {
            Resident target = MyTownDatasource.instance
                    .getOrMakeResident((EntityPlayer) pl);
            if (target.town() != this) {
                townColor = "§4";
            }
        }

        MyTown.sendChatToPlayer(pl, Term.TownStatusName.toString(townColor, t.name()));
        MyTown.sendChatToPlayer(pl, Term.TownStatusGeneral.toString(t.blocks().size(), String.valueOf(t.totalBlocks()), t.nation() == null ? "none" : t.nation().name()));
        
        if (blocks_list.length() > 0) {
            MyTown.sendChatToPlayer(pl, blocks_list.toString());
        }
        
        MyTown.sendChatToPlayer(pl, Term.TownStatusMayor.toString(mayors.toString()));
        MyTown.sendChatToPlayer(pl, Term.TownStatusAssistants.toString(assistants.toString()));
        MyTown.sendChatToPlayer(pl, Term.TownStatusResidents.toString(residents.toString()));
    }

    public void notifyPlayerLoggedOn(Resident r) {
        sendNotification(Level.INFO, Term.TownBroadcastLoggedIn.toString(r
                .name()));
    }

    public void notifyPlayerLoggedOff(Resident r) {
        sendNotification(Level.INFO, Term.TownBroadcastLoggedOut.toString(r
                .name()));
    }
}
