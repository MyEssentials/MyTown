package mytown;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mytown.entities.Nation;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.sql.MyTownDB;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import com.google.common.base.Joiner;

public class MyTownDatasource extends MyTownDB {
	public static MyTownDatasource instance = new MyTownDatasource();

	public HashMap<String, Resident> residents;
	public HashMap<String, Town> towns;
	public HashMap<String, TownBlock> blocks;
	public HashMap<String, Nation> nations;

	public static String getTownBlockKey(int dim, int x, int z) {
		return dim + ";" + x + ";" + z;
	}

	public static String getTownBlockKey(TownBlock block) {
		return block.worldDimension() + ";" + block.x() + ";" + block.z();
	}

	public void init() throws Exception {
		residents = new HashMap<String, Resident>();
		towns = new HashMap<String, Town>();
		blocks = new HashMap<String, TownBlock>();
		nations = new HashMap<String, Nation>();

		dispose();
		connect();
		load();

		towns.putAll(loadTowns());
		residents.putAll(loadResidents()); // links to towns

		for (Town t : towns.values()) {
			for (TownBlock res : t.blocks()) {
				if (res.owner_name != null) { // map block owners
					Resident r = getResident(res.owner_name);
					res.sqlSetOwner(r);
					res.owner_name = null;
				}

				blocks.put(getTownBlockKey(res), res); // add block to global
														// list
			}
		}

		nations.putAll(loadNations());

		addAllOnlinePlayers();
	}

	public void addAllOnlinePlayers() {
		for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			EntityPlayer pl = (EntityPlayer) obj;
			getOrMakeResident(pl);
		}
	}

	public void addTown(Town t) {
		towns.put(t.name().toLowerCase(), t);
	}

	public void addNation(Nation n) {
		nations.put(n.name().toLowerCase(), n);
	}

	public TownBlock getOrMakeBlock(int world_dimension, int x, int z) {

		TownBlock res = blocks.get(getTownBlockKey(world_dimension, x, z));
		if (res == null) {
			res = new TownBlock(world_dimension, x, z);
			blocks.put(getTownBlockKey(world_dimension, x, z), res);
		}
		return res;
	}

	public TownBlock getBlock(int world_dimension, int x, int z) {
		return blocks.get(getTownBlockKey(world_dimension, x, z));
	}

	public TownBlock getPermBlockAtCoord(int world_dimension, int x, int y, int z) {
		return getPermBlockAtCoord(world_dimension, x, y, y, z);
	}

	public TownBlock getPermBlockAtCoord(int world_dimension, int x, int yFrom, int yTo, int z) {
		TownBlock targetBlock = getBlock(world_dimension, ChunkCoord.getCoord(x), ChunkCoord.getCoord(z));
		if (targetBlock != null && targetBlock.settings.yCheckOn) {
			if (yTo < targetBlock.settings.yCheckFrom || yFrom > targetBlock.settings.yCheckTo) {
				targetBlock = targetBlock.getFirstFullSidingClockwise(targetBlock.town());
			}
		}

		return targetBlock;
	}

	public Town getTown(String name) {
		return towns.get(name.toLowerCase());
	}

	@Override
	public Town getTown(int id) {
		for (Town res : towns.values()) {
			if (res.id() == id) {
				return res;
			}
		}
		return null;
	}

	public Nation getNation(String name) {
		return nations.get(name.toLowerCase());
	}

	public synchronized Resident getOrMakeResident(EntityPlayer player) {
		Resident res = residents.get(player.getEntityName().toLowerCase());

		if (res == null) {
			res = makeResident(player.getEntityName());
		}
		res.onlinePlayer = player;
		return res;
	}

	public Resident getResident(EntityPlayer player) {
		return residents.get(player.getEntityName().toLowerCase());
	}

	public Resident getOrMakeResident(String name) // case in-sensitive
	{
		Resident res = residents.get(name.toLowerCase());

		if (res == null) {
			res = makeResident(name);
		}

		return res;
	}

	private Resident makeResident(String name) {
		Resident res = new Resident(name);
		residents.put(name.toLowerCase(), res);

		return res;
	}

	public Resident getResident(String name) // case in-sensitive
	{
		Resident res = residents.get(name.toLowerCase());
		return res;
	}

	public List<Resident> getOnlineResidents() {
		ArrayList<Resident> ret = new ArrayList<Resident>();
		for (Resident res : residents.values()) {
			if (res.isOnline()) {
				ret.add(res);
			}
		}

		return ret;
	}

	@Override
	public void saveTown(Town town) {
		if (!town.oldName().trim().isEmpty()) {
			towns.remove(town.oldName().toLowerCase());
			town.setOldName("");
		}
		super.saveTown(town);
		towns.put(town.name().toLowerCase(), town);
	}

	@Override
	public void saveNation(Nation nation) {
		super.saveNation(nation);
		if (!nation.oldName().trim().isEmpty()) {
			nations.remove(nation.oldName().toLowerCase());
			nation.setOldName("");
		}
		nations.put(nation.name().toLowerCase(), nation);
	}

	public void unloadTown(Town t) {
		towns.remove(t.name().toLowerCase());
	}

	public void unloadNation(Nation n) {
		nations.remove(n);
	}

	public void unloadBlock(TownBlock b) {
		b.settings.setParent(null);
		blocks.remove(getTownBlockKey(b));
	}

	public int deleteAllTownBlocksInDimension(int dim) {
		int ret = 0;
		ArrayList<TownBlock> toRemove = new ArrayList<TownBlock>();
		for (TownBlock res : blocks.values()) {
			if (res.worldDimension() == dim) {
				toRemove.add(res);
			}
		}

		ArrayList<Town> townsToSave = new ArrayList<Town>();
		for (TownBlock res : toRemove) {
			if (res.town() != null) {
				townsToSave.add(res.town());
				res.town().removeBlockUnsafe(res);
				ret++;
			} else {
				unloadBlock(res);
			}
		}

		for (Town t : townsToSave) {
			t.save();
		}

		return ret;
	}

	public List<Resident> getOldResidents(Date lastLoginTimeBelow) {
		ArrayList<Resident> players = new ArrayList<Resident>();
		synchronized (residents) {
			for (Resident res : residents.values()) {
				if (res.town() != null && !res.isOnline() && res.lastLogin().compareTo(lastLoginTimeBelow) < 0) {
					players.add(res);
				}
			}
		}

		return players;
	}

	public List<Town> getOldTowns(long lastLoginTimeBelow, double plotDaysAddition) {
		ArrayList<Town> towns = new ArrayList<Town>();
		synchronized (residents) {
			for (Resident res : residents.values()) {
				Date last = new Date(lastLoginTimeBelow - (res.town() != null ? (int) (plotDaysAddition * res.town().blocks().size()) : 0));
				if (res.town() != null && !res.isOnline() && res.lastLogin().compareTo(last) < 0) {
					if (!towns.contains(res.town())) {
						boolean allOld = true;
						for (Resident r : res.town().residents()) {
							if (r.isOnline() || r.lastLogin().compareTo(last) >= 0) {
								allOld = false;
								break;
							}
						}
						if (allOld) {
							towns.add(res.town());
						}
					}
				}
			}
		}

		return towns;
	}

	/**
	 * Dumps the Database's tables to an sql formatted file
	 * 
	 * @param writer
	 * @throws SQLException
	 * @throws IOException
	 */
	public void dumpData(OutputStreamWriter writer) throws SQLException, IOException {
		dumpResidents(writer);
		dumpTowns(writer);
		dumpNations(writer);
		dumpUpdates(writer);
		writer.flush();
	}

	/**
	 * Dump user table
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void dumpResidents(OutputStreamWriter writer) throws IOException {
		if (residents.size() <= 0)
			return;
		writer.append("/**\n");
		writer.append(" * Residents\n");
		writer.append(" */\n");
		for (Resident res : residents.values()) {
			String resStr = String.format("INSERT INTO %sresidents (Id, Name, Nick, Town, Rank, Channel, Created, LastLogin, Extra, Friends, Homes) VALUES (%d, '%s', '%s', %d, '%s', '%s', '%s', '%s', '%s', '%s', '%s');\n", prefix, res.id(), res.name(), res.nick(), res.town() == null ? 0 : res
					.town().id(), res.rank().toString(), res.activeChannel.toString(), iso8601Format.format(res.created()), iso8601Format.format(res.lastLogin()), res.serializeExtra(), Joiner.on(';').join(res.friends), res.home.serialize());
			writer.append("/* User " + res.name() + "*/\n");
			writer.append(resStr);
		}
	}

	/**
	 * Dump town table
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void dumpTowns(OutputStreamWriter writer) throws IOException {
		if (towns.size() <= 0)
			return;
		writer.append("\n/**\n");
		writer.append(" * Towns\n");
		writer.append(" */\n");
		for (Town t : towns.values()) {
			String townStr = String.format("INSERT INTO %stowns (Id, Name, ExtraBlocks, Blocks, Extra) VALUES (%d, '%s', %d, '%s', '%s');\n", prefix, t.id(), t.name(), t.extraBlocks(), Joiner.on(" ").join(t.blocks()), t.serializeExtra());
			writer.append("/* Town " + t.name() + " */\n");
			writer.append(townStr);
		}
	}

	/**
	 * Dump nation table
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void dumpNations(OutputStreamWriter writer) throws IOException {
		if (nations.size() <= 0)
			return;
		writer.append("\n/**\n");
		writer.append(" * Nations\n");
		writer.append(" */\n");
		for (Nation n : nations.values()) {
			List<Integer> towns = new ArrayList<Integer>();
			for (Town r : n.towns().values()) {
				if (r.id() > 0) {
					towns.add(r.id());
				}
			}
			String sTowns = Joiner.on(';').join(towns);

			String nationStr = String.format("INSERT INTO %snations (Id, Name, Towns, Capital, Extra) VALUES (%d, '%s', '%s', '%d', '%s');\n", prefix, n.id(), n.name(), sTowns, n.capital().id(), n.serializeExtra());
			writer.append("/* Nation " + n.name() + " */\n");
			writer.append(nationStr);
		}
	}

	/**
	 * Dump update table
	 * 
	 * @param writer
	 * @throws IOException
	 * @throws SQLException
	 */
	private void dumpUpdates(OutputStreamWriter writer) throws IOException, SQLException {
		writer.append("\n/**\n");
		writer.append(" * Updates\n");
		writer.append(" */\n");
		ResultSet r = null;
		PreparedStatement s = prepare("SELECT * FROM " + prefix + "updates");
		r = s.executeQuery();
		while (r != null && r.next()) {
			String updateStr = String.format("INSERT INTO %supdates (Code) VALUES ('%s');\n", prefix, r.getString("Code"));
			writer.append(updateStr);
		}
	}
}