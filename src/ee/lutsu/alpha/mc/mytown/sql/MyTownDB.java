package ee.lutsu.alpha.mc.mytown.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import ee.lutsu.alpha.mc.mytown.ChatChannel;
import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.entities.Nation;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.Resident.Rank;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;

public abstract class MyTownDB extends Database {

    public boolean loaded = false;
    private Object lock = new Object();
    public static DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void load() {
        if (loaded) {
            return;
        }

        synchronized (lock) {
            try {
                tryDoUpdates();
            } catch (Exception e) {
                throw new RuntimeException("Error in making/updating tables", e);
            }
        }

        loaded = true;
    }

    public void tryDoUpdates() throws SQLException {
        ResultSet r = null;
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "updates");
            r = s.executeQuery();
        } catch (SQLException e) {}

        List<String> lst = Lists.newArrayList();
        while (r != null && r.next()) {
            lst.add(r.getString("Code"));
        }

        doUpdates(lst);
    }

    private void doUpdates(List<String> codes) throws SQLException {
        doUpdate(codes, "26.02.2013", "Creates 'Updates' table");

        doUpdate(codes, "20.11.2012", "Creates 'Towns' table");
        doUpdate(codes, "21.11.2012", "Adds 'Extra' field to 'Towns' table");
        doUpdate(codes, "13.12.2012", "Creates 'Residents' table");
        doUpdate(codes, "14.12.2012", "Adds 'Friends' field to 'Residents' table");
        doUpdate(codes, "16.12.2012", "Creates 'Nations' table");
        doUpdate(codes, "27.02.2013", "Adds 'Homes' field to 'Residents' table");
    }

    private void doUpdateSwitch(String code) throws SQLException {
        if (code.equals("20.11.2012")) {
            update_20_11_2012();
        } else if (code.equals("21.11.2012")) {
            update_21_11_2012();
        } else if (code.equals("13.12.2012")) {
            update_13_12_2012();
        } else if (code.equals("14.12.2012")) {
            update_14_12_2012();
        } else if (code.equals("16.12.2012")) {
            update_16_12_2012();
        } else if (code.equals("26.02.2013")) {
            update_26_02_2013();
        } else if (code.equals("27.02.2013")) {
            update_27_02_2013();
        }
    }

    // ////////////////////////// start updates ////////////////////////////

    private void update_20_11_2012() throws SQLException {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "towns");
            s.executeQuery();
        } catch (SQLException e) {
            Table towns = new Table(this, "towns");
            {
                towns.add("Id", "INTEGER", true, true);
                towns.add("Name", "VARCHAR(255)");
                towns.add("ExtraBlocks", "INTEGER");
                towns.add("Residents", "TEXT");
                towns.add("Blocks", "TEXT");
            }
            towns.execute();
        }
    }

    private void update_21_11_2012() throws SQLException {
        try {
            PreparedStatement s = prepare("SELECT Extra FROM " + prefix + "towns");
            s.executeQuery();
        } catch (SQLException e) {
            PreparedStatement statement = prepare("alter table " + prefix + "towns ADD Extra varchar(2000) null");
            statement.executeUpdate();
        }
    }

    private void update_13_12_2012() throws SQLException {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "residents");
            s.executeQuery();
        } catch (SQLException e) {
            Table residents = new Table(this, "residents");
            {
                residents.add("Id", "INTEGER", true, true);
                residents.add("Name", "VARCHAR(255)");
                residents.add("Town", "INTEGER");
                residents.add("Rank", "VARCHAR(255)");
                residents.add("Channel", "VARCHAR(255)");
                residents.add("Created", "VARCHAR(255)");
                residents.add("LastLogin", "VARCHAR(255)");
                residents.add("Extra", "TEXT");
            }
            residents.execute();

            PreparedStatement statementTown = prepare("SELECT * FROM " + prefix + "towns");
            ResultSet setTown = statementTown.executeQuery();

            HashMap<Integer, String> towns = new HashMap<Integer, String>();
            while (setTown.next()) {
                towns.put(setTown.getInt("Id"), setTown.getString("Residents"));
            }
            setTown.close();

            for (Entry<Integer, String> town : towns.entrySet()) {
                int tid = town.getKey();
                String res = town.getValue();
                if (res != null && res != "") {
                    for (String split : res.split(" ")) {
                        if (split.trim().length() > 0) {
                            String[] opt = split.trim().split(";");

                            String rName = opt[0];
                            Rank rRank = Rank.parse(opt[1]);
                            ChatChannel rChannel = ChatChannel.parse(opt[2]);

                            PreparedStatement statement = prepare("DELETE FROM " + prefix + "residents where Name = ?");
                            statement.setString(1, rName);
                            statement.executeUpdate();

                            statement = prepare("INSERT INTO " + prefix + "residents (Name, Town, Rank, Channel, Created, LastLogin, Extra) VALUES (?, ?, ?, ?, ?, ?, ?)");
                            statement.setString(1, rName);
                            statement.setInt(2, tid);
                            statement.setString(3, rRank.toString());
                            statement.setString(4, rChannel.toString());
                            statement.setString(5, iso8601Format.format(new Date(System.currentTimeMillis())));
                            statement.setString(6, iso8601Format.format(new Date(System.currentTimeMillis())));
                            statement.setString(7, "");

                            statement.executeUpdate();
                        }
                    }
                }
            }

            PreparedStatement statement = prepare("UPDATE " + prefix + "towns SET Residents = NULL");
            statement.executeUpdate();
        }
    }

    private void update_14_12_2012() throws SQLException {
        try {
            PreparedStatement s = prepare("SELECT Friends FROM " + prefix + "residents");
            s.executeQuery();
        } catch (SQLException e) {
            PreparedStatement statement = prepare("alter table " + prefix + "residents ADD Friends TEXT");
            statement.executeUpdate();
        }
    }

    private void update_16_12_2012() throws SQLException {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "nations");
            s.executeQuery();
        } catch (SQLException e) {
            Table nations = new Table(this, "nations");
            {
                nations.add("Id", "INTEGER", true, true);
                nations.add("Name", "VARCHAR(255)");
                nations.add("Towns", "TEXT");
                nations.add("Capital", "INTEGER");
                nations.add("Extra", "TEXT");
            }
            nations.execute();
        }
    }

    private void update_26_02_2013() throws SQLException {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "updates");
            s.executeQuery();
        } catch (SQLException e) {
            Table updates = new Table(this, "updates");
            {
                updates.add("Id", "INTEGER", true, true);
                updates.add("Code", "VARCHAR(255)");
            }
            updates.execute();
        }
    }

    private void update_27_02_2013() throws SQLException {
        PreparedStatement statement = prepare("alter table " + prefix + "residents ADD Homes TEXT");
        statement.executeUpdate();
    }

    // ////////////////////////// end updates ////////////////////////////

    private void doUpdate(List<String> codes, String code, String desc) throws SQLException {
        if (codes.contains(code)) {
            return;
        }

        Log.info("[DB]Doing update '%s' - %s", code, desc);

        doUpdateSwitch(code);

        PreparedStatement statement = prepare("insert into " + prefix + "updates (Code) values ('" + code + "')");
        statement.executeUpdate();
    }

    public Map<String, Nation> loadNations() // has to happen after town load
    {
        synchronized (lock) {
            ResultSet set = null;
            Map<String, Nation> nations = new HashMap<String, Nation>();
            try {
                PreparedStatement statement = prepare("SELECT * FROM " + prefix + "nations");
                set = statement.executeQuery();

                while (set.next()) {
                    Nation nation = Nation.sqlLoad(set.getInt("Id"), set.getString("Name"), set.getInt("Capital"), set.getString("Towns"), set.getString("Extra"));
                    nations.put(nation.name().toLowerCase(), nation);
                }
            } catch (Exception e) {
                printException(e);
            } finally {
                if (set != null) {
                    try {
                        set.close();
                    } catch (Exception e) {}
                }
            }

            return nations;
        }
    }

    public void saveNation(Nation nation) {
        List<Integer> towns = new ArrayList<Integer>();
        for (Town r : nation.towns()) {
            if (r.id() > 0) {
                towns.add(r.id());
            }
        }

        String sTowns = Joiner.on(';').join(towns);

        synchronized (lock) {
            try {
                if (nation.id() > 0) {
                    PreparedStatement statement = prepare("UPDATE " + prefix + "nations SET Name = ?, Towns = ?, Capital = ?, Extra = ? WHERE id = ?");
                    statement.setString(1, nation.name());
                    statement.setString(2, sTowns);
                    statement.setInt(3, nation.capital().id());
                    statement.setString(4, nation.serializeExtra());

                    statement.setInt(5, nation.id());
                    statement.executeUpdate();
                } else {
                    PreparedStatement statement = prepare("INSERT INTO " + prefix + "nations (Name, Towns, Capital, Extra) VALUES (?, ?, ?, ?)", true);
                    statement.setString(1, nation.name());
                    statement.setString(2, sTowns);
                    statement.setInt(3, nation.capital().id());
                    statement.setString(4, nation.serializeExtra());

                    statement.executeUpdate();

                    ResultSet rs = statement.getGeneratedKeys();
                    if (!rs.next()) {
                        throw new RuntimeException("Id wasn't returned for new nation " + nation.name());
                    }

                    nation.setId(rs.getInt(1));
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in nation saving", e);
            }
        }
    }

    public void deleteNation(Nation nation) {
        synchronized (lock) {
            try {
                if (nation.id() > 0) {
                    PreparedStatement statement = prepare("DELETE FROM " + prefix + "nations WHERE id = ?");
                    statement.setInt(1, nation.id());
                    statement.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in nation deleting", e);
            }
        }
    }

    public void deleteTown(Town town) {
        synchronized (lock) {
            try {
                if (town.id() > 0) {
                    PreparedStatement statement = prepare("DELETE FROM " + prefix + "towns WHERE id = ?");
                    statement.setInt(1, town.id());
                    statement.executeUpdate();

                    statement = prepare("UPDATE " + prefix + "residents SET Town = 0, Rank = ? WHERE Town = ?");
                    statement.setString(1, Rank.Resident.toString());
                    statement.setInt(2, town.id());
                    statement.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in town deleting", e);
            }
        }
    }

    public void saveResident(Resident res) {
        List<Integer> friends = new ArrayList<Integer>();
        for (Resident r : res.friends) {
            if (r.id() > 0) {
                friends.add(r.id());
            }
        }

        String sFriends = Joiner.on(';').join(friends);

        synchronized (lock) {
            try {
                if (res.id() > 0) {
                    PreparedStatement statement = prepare("UPDATE " + prefix + "residents SET Name = ?, Town = ?, Rank = ?, Channel = ?, LastLogin = ?, Extra = ?, Friends = ?, Homes = ? WHERE id = ?");
                    statement.setString(1, res.name());
                    statement.setInt(2, res.town() == null ? 0 : res.town().id());
                    statement.setString(3, res.rank().toString());
                    statement.setString(4, res.activeChannel.toString());
                    statement.setString(5, iso8601Format.format(res.lastLogin()));
                    statement.setString(6, res.serializeExtra());
                    statement.setString(7, sFriends);
                    statement.setString(8, res.home.serialize());

                    statement.setInt(9, res.id());
                    statement.executeUpdate();
                } else {
                    PreparedStatement statement = prepare("INSERT INTO " + prefix + "residents (Name, Town, Rank, Channel, Created, LastLogin, Extra, Friends, Homes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", true);
                    statement.setString(1, res.name());
                    statement.setInt(2, res.town() == null ? 0 : res.town().id());
                    statement.setString(3, res.rank().toString());
                    statement.setString(4, res.activeChannel.toString());
                    statement.setString(5, iso8601Format.format(res.created()));
                    statement.setString(6, iso8601Format.format(res.lastLogin()));
                    statement.setString(7, res.serializeExtra());
                    statement.setString(8, sFriends);
                    statement.setString(9, res.home.serialize());

                    statement.executeUpdate();

                    ResultSet rs = statement.getGeneratedKeys();
                    if (!rs.next()) {
                        throw new RuntimeException("Id wasn't returned for new resident " + res.name());
                    }

                    res.setId(rs.getInt(1));
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in town saving", e);
            }
        }
    }

    @Deprecated
    /*
     * Don't use this. We want to keep info about all residents
     */
    public void deleteResident(Resident res) {
        synchronized (lock) {
            try {
                if (res.id() > 0) {
                    PreparedStatement statement = prepare("DELETE FROM " + prefix + "residents WHERE id = ?");
                    statement.setInt(1, res.id());
                    statement.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in resident deleting", e);
            }
        }
    }

    public void saveTown(Town town) {
        StringBuilder blocks = new StringBuilder();

        int i = 0;
        for (TownBlock block : town.blocks()) {
            blocks.append(block.serialize());

            if (++i < town.blocks().size()) {
                blocks.append(" ");
            }
        }

        synchronized (lock) {
            try {
                if (town.id() > 0) {
                    PreparedStatement statement = prepare("UPDATE " + prefix + "towns SET Name = ?, ExtraBlocks = ?, Blocks = ?, Extra = ? WHERE id = ?");
                    // PreparedStatement statement = prepare("UPDATE " + prefix
                    // +
                    // "towns SET Name = ?, ExtraBlocks = ?, Blocks = ?, Extra = ?, PVP = ? WHERE id = ?");
                    statement.setString(1, town.name());
                    statement.setInt(2, town.extraBlocks());
                    statement.setString(3, blocks.toString());
                    statement.setString(4, town.serializeExtra());
                    // statement.setBoolean(5, town.isPvp());

                    statement.setInt(5, town.id());
                    statement.executeUpdate();
                } else {
                    PreparedStatement statement = prepare("INSERT INTO " + prefix + "towns (Name, ExtraBlocks, Blocks, Extra) VALUES (?, ?, ?, ?)", true);
                    // PreparedStatement statement = prepare("INSERT INTO " +
                    // prefix +
                    // "towns (Name, ExtraBlocks, Blocks, Extra, PVP) VALUES (?, ?, ?, ?, ?)",
                    // true);
                    statement.setString(1, town.name());
                    statement.setInt(2, town.extraBlocks());
                    statement.setString(3, blocks.toString());
                    statement.setString(4, town.serializeExtra());
                    // statement.setBoolean(5, town.isPvp());

                    statement.executeUpdate();

                    ResultSet rs = statement.getGeneratedKeys();
                    if (!rs.next()) {
                        throw new RuntimeException("Id wasn't returned for new town " + town.name());
                    }

                    town.setId(rs.getInt(1));
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in town saving", e);
            }
        }
    }

    public abstract Town getTown(int id);

    public Map<String, Resident> loadResidents() {
        synchronized (lock) {
            ResultSet set = null;
            Map<String, Resident> residents = new HashMap<String, Resident>();
            HashMap<Resident, String> friends = new HashMap<Resident, String>();
            try {
                PreparedStatement statement = prepare("SELECT * FROM " + prefix + "residents");
                set = statement.executeQuery();

                while (set.next()) {
                    int tid = set.getInt("Town");
                    Town town = tid > 0 ? getTown(tid) : null;

                    Resident r = Resident.loadFromDB(set.getInt("Id"), set.getString("Name"), town, Rank.parse(set.getString("Rank")), ChatChannel.parse(set.getString("Channel")), iso8601Format.parse(set.getString("Created")), iso8601Format.parse(set.getString("LastLogin")), set.getString("Extra"),
                            set.getString("Homes"));

                    residents.put(r.name().toLowerCase(), r);

                    String f = set.getString("Friends");
                    if (f != null && f.length() > 0) {
                        friends.put(r, f);
                    }
                }
            } catch (Exception e) {
                printException(e);
            } finally {
                if (set != null) {
                    try {
                        set.close();
                    } catch (Exception e) {}
                }
            }

            // link friends
            for (Entry<Resident, String> entry : friends.entrySet()) {
                String[] ids = entry.getValue().split(";");
                for (String sfid : ids) {
                    int fid = Integer.parseInt(sfid);
                    Resident friend = null;
                    for (Resident r2 : residents.values()) {
                        if (r2.id() == fid) {
                            friend = r2;
                            break;
                        }
                    }

                    if (friend == null) {
                        continue; // not found, just skip
                    }

                    entry.getKey().friends.add(friend);
                }
            }

            return residents;
        }
    }

    public Map<String, Town> loadTowns() {
        synchronized (lock) {
            ResultSet set = null;
            Map<String, Town> towns = new HashMap<String, Town>();
            try {
                PreparedStatement statement = prepare("SELECT * FROM " + prefix + "towns");
                set = statement.executeQuery();

                while (set.next()) {
                    Town town = loadFromSQL(set.getInt("Id"), set.getString("Name"), set.getInt("ExtraBlocks"), set.getString("Blocks"), set.getString("Extra"));
                    towns.put(town.name().toLowerCase(), town);
                }
            } catch (Exception e) {
                printException(e);
            } finally {
                if (set != null) {
                    try {
                        set.close();
                    } catch (Exception e) {}
                }
            }

            return towns;
        }
    }

    public Town loadFromSQL(int pId, String pName, int pExtraBlocks, String pBlocks, String pExtra) {
        List<TownBlock> blocks = new ArrayList<TownBlock>();

        if (pBlocks != null && pBlocks != "") {
            for (String split : pBlocks.split(" ")) {
                if (split.trim().length() > 0) {
                    blocks.add(TownBlock.deserialize(split));
                }
            }
        }

        return new Town(pId, pName, pExtraBlocks, blocks, pExtra);
    }
}
