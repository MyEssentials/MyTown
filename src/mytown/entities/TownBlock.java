package mytown.entities;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
        String key;
        SettingCollection set;
        Iterator<Entry<String, SettingCollection>> setIt = settings.entrySet().iterator();
        while(setIt.hasNext()){
        	key = setIt.next().getKey();
        	set = setIt.next().getValue();
        	set.setParent(town == null ? null : owner != null ? owner.settings.get(key) : town.settings.get("core"));
        }
    }

    public void setOwner(Resident val) {
        sqlSetOwner(val);
        save();
    }

    public void sqlSetOwner(Resident val) {
        owner = val;
        String key;
        SettingCollection set;
        Iterator<Entry<String, SettingCollection>> setIt = settings.entrySet().iterator();
        while(setIt.hasNext()){
        	key = setIt.next().getKey();
        	set = setIt.next().getValue();
        	set.setParent(town == null ? null : owner != null ? owner.settings.get(key) : town.settings.get("core"));
        }
    }

    // extra
    public Map<String, SettingCollection> settings;

    public TownBlock(int pWorld, int x, int z) {
    	settings.put("core", SettingCollection.generateCoreSettings());
    	settings.put("town", SettingCollection.generateTownMemberSettings());
    	settings.put("friend", SettingCollection.generateTownMemberSettings());
    	settings.put("out", SettingCollection.generateOutsiderSettings());
    	settings.put("nation", SettingCollection.generateOutsiderSettings());
    	
        world_dimension = pWorld;
        chunkX = x;
        chunkZ = z;
        
        ISettingsSaveHandler saveHandler = new ISettingsSaveHandler() {
            @Override
            public void save(SettingCollection sender, Object tag) {
                ((TownBlock) tag).save();
            }
        };
        

        Iterator<SettingCollection> setIt = settings.values().iterator();
        SettingCollection set;
        while(setIt.hasNext()){
        	set = setIt.next();
        	set.tag = this;
        	set.saveHandler = saveHandler;
        }
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
            Iterator<SettingCollection> setIt = t.settings.values().iterator();
            while(setIt.hasNext()){
            	setIt.next().deserialize(splits[4]);
            }
        }

        return t;
    }

    public String serialize() { // don't use space
    	String settingsSerialize = "";

        Iterator<SettingCollection> setIt = settings.values().iterator();
        while(setIt.hasNext()){
        	settingsSerialize += setIt.next().serialize();
        	if (setIt.hasNext()) settingsSerialize += "/";
        }
        
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
        if (b != null && b.town != null && b.town != notForTown && b.settings.get("core").getSetting("ycheck").getValue(String.class) != null && !b.settings.get("core").getSetting("ycheck").getValue(String.class).isEmpty()) {
            return b;
        }

        b = MyTownDatasource.instance.getBlock(world_dimension, chunkX + 1, chunkZ);
        if (b != null && b.town != null && b.town != notForTown && b.settings.get("core").getSetting("ycheck").getValue(String.class) != null && !b.settings.get("core").getSetting("ycheck").getValue(String.class).isEmpty()) {
            return b;
        }

        b = MyTownDatasource.instance.getBlock(world_dimension, chunkX, chunkZ + 1);
        if (b != null && b.town != null && b.town != notForTown && b.settings.get("core").getSetting("ycheck").getValue(String.class) != null && !b.settings.get("core").getSetting("ycheck").getValue(String.class).isEmpty()) {
            return b;
        }

        b = MyTownDatasource.instance.getBlock(world_dimension, chunkX - 1, chunkZ);
        if (b != null && b.town != null && b.town != notForTown && b.settings.get("core").getSetting("ycheck").getValue(String.class) != null && !b.settings.get("core").getSetting("ycheck").getValue(String.class).isEmpty()) {
            return b;
        }

        return null;
    }
}
