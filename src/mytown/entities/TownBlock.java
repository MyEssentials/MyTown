package mytown.entities;

import mytown.MyTownDatasource;
import mytown.entities.SettingCollection.ISettingsSaveHandler;

public class TownBlock {
    private int world_dimension;
    private int chunkX;
    private int chunkZ;
    private Town town;
    private Resident owner;
    public String owner_name; // only for sql loading. Don't use.

    public int x() {
        return chunkX;
    }

    public int z() {
        return chunkZ;
    }

    public int worldDimension() {
        return world_dimension;
    }

    public Town town() {
        return town;
    }

    public Resident owner() {
        return owner;
    }

    public String ownerDisplay() {
        return owner == null ? "-" : owner.name();
    }

    public void setTown(Town val) {
        town = val;
        coreSettings.setParent(town == null ? null : owner != null ? owner.coreSettings : town.coreSettings);
        townSettings.setParent(town == null ? null : owner != null ? owner.townSettings : town.townSettings);
        friendSettings.setParent(town == null ? null : owner != null ? owner.friendSettings : town.friendSettings);
        outSettings.setParent(town == null ? null : owner != null ? owner.outSettings : town.outSettings);
        nationSettings.setParent(town == null ? null : owner != null ? owner.nationSettings : town.nationSettings);
    }

    public void setOwner(Resident val) {
        sqlSetOwner(val);
        save();
    }

    public void sqlSetOwner(Resident val) {
        owner = val;
        coreSettings.setParent(town == null ? null : owner != null ? owner.coreSettings : town.coreSettings);
        townSettings.setParent(town == null ? null : owner != null ? owner.townSettings : town.townSettings);
        friendSettings.setParent(town == null ? null : owner != null ? owner.friendSettings : town.friendSettings);
        outSettings.setParent(town == null ? null : owner != null ? owner.outSettings : town.outSettings);
        nationSettings.setParent(town == null ? null : owner != null ? owner.nationSettings : town.nationSettings);
    }

    // extra
    public SettingCollection coreSettings = SettingCollection.generateCoreSettings();
    public SettingCollection townSettings = SettingCollection.generateTownMemberSettings();
    public SettingCollection friendSettings = SettingCollection.generateTownMemberSettings();
    public SettingCollection outSettings = SettingCollection.generateOutsiderSettings();
    public SettingCollection nationSettings = SettingCollection.generateOutsiderSettings();

    public TownBlock(int pWorld, int x, int z) {
        world_dimension = pWorld;
        chunkX = x;
        chunkZ = z;

        coreSettings.tag = this;
        townSettings.tag = this;
        friendSettings.tag = this;
        outSettings.tag = this;
        nationSettings.tag = this;
        ISettingsSaveHandler saveHandler = new ISettingsSaveHandler() {
            @Override
            public void save(SettingCollection sender, Object tag) {
                ((TownBlock) tag).save();
            }
        };
        coreSettings.saveHandler = saveHandler;
        townSettings.saveHandler = saveHandler;
        friendSettings.saveHandler = saveHandler;
        outSettings.saveHandler = saveHandler;
        nationSettings.saveHandler = saveHandler;
    }

    public static TownBlock deserialize(String info) {
        String[] splits = info.split(";");
        if (splits.length < 3) {
            throw new RuntimeException("Error in block info : " + info);
        }

        TownBlock t = new TownBlock(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), Integer.parseInt(splits[2]));

        if (splits.length > 3) {
            t.owner_name = splits[3];
        }
        if (splits.length > 4) {
            t.coreSettings.deserialize(splits[4]);
            t.townSettings.deserialize(splits[4]);
            t.friendSettings.deserialize(splits[4]);
            t.outSettings.deserialize(splits[4]);
            t.nationSettings.deserialize(splits[4]);
        }

        return t;
    }

    public String serialize() { // don't use space
    	String settingsSerialize = "";
    	String coreSettingsSerialize = coreSettings.serialize();
    	String outSettingsSerialize = outSettings.serialize();
    	String townSettingsSerialize = townSettings.serialize();
    	String friendSettingsSerialize = friendSettings.serialize();
    	String nationSettingsSerialize = nationSettings.serialize();

    	settingsSerialize += (coreSettingsSerialize.isEmpty() ? "" : coreSettingsSerialize + "/");
    	settingsSerialize += (outSettingsSerialize.isEmpty() ? "" : outSettingsSerialize + "/");
    	settingsSerialize += (townSettingsSerialize.isEmpty() ? "" : townSettingsSerialize + "/");
    	settingsSerialize += (friendSettingsSerialize.isEmpty() ? "" : friendSettingsSerialize + "/");
    	settingsSerialize += (nationSettingsSerialize.isEmpty() ? "" : nationSettingsSerialize);
        return worldDimension() + ";" + String.valueOf(x()) + ";" + String.valueOf(z()) + ";" + (owner == null ? "" : owner.name()) + ";" + settingsSerialize;
    }

    public boolean equals(TownBlock block) {
        return chunkX == block.chunkX && chunkZ == block.chunkZ && world_dimension == block.world_dimension;
    }

    public boolean equals(int dim, int x, int z) {
        return chunkX == x && chunkZ == z && world_dimension == dim;
    }

    public int squaredDistanceTo(TownBlock b) {
        if (world_dimension != b.world_dimension) {
            throw new RuntimeException("Cannot measure distance to ");
        }

        return Math.abs((chunkX - b.chunkX) * (chunkX - b.chunkX) + (chunkZ - b.chunkZ) * (chunkZ - b.chunkZ));
    }

    public void save() {
        if (town != null) {
            town.save();
        }
    }

    public TownBlock getFirstFullSidingClockwise(Town notForTown) {
        TownBlock b;

        b = MyTownDatasource.instance.getBlock(world_dimension, chunkX, chunkZ - 1);
        if (b != null && b.town != null && b.town != notForTown && !b.coreSettings.getSetting("yon").getValue(Boolean.class)) {
            return b;
        }

        b = MyTownDatasource.instance.getBlock(world_dimension, chunkX + 1, chunkZ);
        if (b != null && b.town != null && b.town != notForTown && !b.coreSettings.getSetting("yon").getValue(Boolean.class)) {
            return b;
        }

        b = MyTownDatasource.instance.getBlock(world_dimension, chunkX, chunkZ + 1);
        if (b != null && b.town != null && b.town != notForTown && !b.coreSettings.getSetting("yon").getValue(Boolean.class)) {
            return b;
        }

        b = MyTownDatasource.instance.getBlock(world_dimension, chunkX - 1, chunkZ);
        if (b != null && b.town != null && b.town != notForTown && !b.coreSettings.getSetting("yon").getValue(Boolean.class)) {
            return b;
        }

        return null;
    }
}
