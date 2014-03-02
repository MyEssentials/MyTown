package mytown.entities;

import java.util.ArrayList;
import java.util.List;

import mytown.Assert;
import mytown.Term;
import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class SavedHomeList extends ArrayList<SavedHome> {
	private static final long serialVersionUID = 5008778401802591440L;
	public static boolean defaultIsBed = true;
	public Resident owner;

	public SavedHomeList(Resident owner) {
		this.owner = owner;
	}

	public void deserialize(String str) {
		clear();

		if (str == null || str.trim().length() < 1) {
			return;
		}

		String[] a = str.split("\\|");

		for (String b : a) {
			add(SavedHome.deserialize(b));
		}
	}

	public String serialize() {
		List<String> ret = Lists.newArrayList();
		for (SavedHome h : this) {
			ret.add(h.serialize());
		}

		return Joiner.on("|").join(ret);
	}

	public SavedHome get(String name) {
		if (defaultIsBed && name == null) {
			if (!owner.isOnline()) {
				throw new RuntimeException(Term.HomeCmdOwnerNotOnline.toString());
			}

			return SavedHome.fromBed((EntityPlayerMP) owner.onlinePlayer); // bed
		} else {
			name = getHomeName(name);
		}

		for (SavedHome a : this) {
			if ((name == "default" && a.name == null) || a.name.equalsIgnoreCase(name)) {
				return a;
			}
		}

		return null;
	}

	public String getHomeName(String name) {
		if (name == null || name.trim().length() < 1) {
			return "default";
		}

		return name.replace('/', '_').replace('|', '_').replace(' ', '_');
	}

	public void assertSetHome(String name, Entity pos) throws CommandException {
		if (!owner.isOnline()) {
			throw new CommandException(Term.HomeCmdOwnerNotOnline.toString());
		}

		boolean newHome = (get(name) == null);

		if (newHome) {
			Assert.Perm(owner.onlinePlayer, "mytown.ecmd.sethome.new_" + String.valueOf(size() + 1));
		}

		if (defaultIsBed && name == null) // bed
		{
			if (pos.dimension != pos.worldObj.provider.getRespawnDimension((EntityPlayerMP) owner.onlinePlayer)) {
				throw new CommandException(Term.HomeCmdDimNotSpawnDim.toString());
			}
		} else if (!newHome) {
			Assert.Perm(owner.onlinePlayer, "mytown.ecmd.sethome.replace");
		}
	}

	public void set(String name, Entity pos) {
		if (defaultIsBed && name == null) {
			owner.onlinePlayer.setSpawnChunk(new ChunkCoordinates((int) pos.posX, (int) pos.posY, (int) pos.posZ), true);
		} else {
			SavedHome h = get(name);
			if (h == null) {
				add(new SavedHome(getHomeName(name), pos));
			} else {
				h.reset(pos);
			}

			save();
		}
	}

	public void delete(String name) throws CommandException {
		if (defaultIsBed && name == null) {
			throw new CommandException(Term.HomeCmdCannotDeleteBed.toString());
		}

		SavedHome h = get(name);

		if (h == null) {
			throw new CommandException(Term.HomeCmdNoHomeByName.toString());
		}

		if (remove(h)) {
			save();
		}
	}

	public boolean hasHomes() {
		if (size() > 0) {
			return true;
		}

		if (defaultIsBed && owner.isOnline() && owner.onlinePlayer.getBedLocation(owner.onlinePlayer.worldObj.provider.getRespawnDimension((EntityPlayerMP) owner.onlinePlayer)) != null) {
			return true;
		}

		return false;
	}

	public void save() {
		owner.save();
	}
}
