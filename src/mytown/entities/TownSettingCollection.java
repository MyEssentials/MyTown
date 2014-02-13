package mytown.entities;

import java.util.ArrayList;
import java.util.List;

import mytown.MyTown;
import mytown.Term;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.base.Joiner;
import com.sperion.forgeperms.ForgePerms;

/**
 * 
 * Hierarchy: Server-Wild L World-Wild Server L Town L Resident L Plot
 * 
 */
public class TownSettingCollection {
	public enum Permissions {
		None, // First char has to be different
		Enter, // Anyone given Enter can only enter the town/claim
		Loot, // Anyone given Loot can Enter and pickup items
		Access, // Anyone given Access can Enter, Loot, Attack, and Access
				// chests, doors, buttons, etc
		Build; // Anyone given Build can do everything! (Be careful with this
				// one ;))

		public String getShort() {
			return toString().substring(0, 1);
		}

		public static Permissions parse(String str) {
			for (Permissions val : values()) {
				if (val.toString().toLowerCase().startsWith(str.toLowerCase())) {
					return val;
				}
			}

			return None;
		}

		public static String getValuesDesc() {
			List<String> vals = new ArrayList<String>();
			for (Permissions val : values()) {
				vals.add(val.toString());
			}
			return Joiner.on(",").join(vals);
		}
	}

	public interface ISettingsSaveHandler {
		void save(TownSettingCollection sender, Object tag);
	}

	public boolean isWild, isRoot;
	public TownSettingCollection parent;
	public List<TownSettingCollection> childs = new ArrayList<TownSettingCollection>();

	public List<TownSetting> settings = new ArrayList<TownSetting>();

	public ISettingsSaveHandler saveHandler;
	public Object tag;

	public TownSettingCollection() {
		this(false, false);
	}

	public TownSettingCollection(boolean isRoot, boolean isWild) {
		this.isWild = isWild;
		this.isRoot = isRoot;
		reset();
	}

	public void setParent(TownSettingCollection col) {
		if (parent != null) {
			parent.removeChild(this);
		}

		parent = col;

		if (parent != null) {
			parent.addChild(this);
			refresh();
		}
	}

	protected void addChild(TownSettingCollection col) {
		childs.add(col);
	}

	protected void removeChild(TownSettingCollection col) {
		childs.remove(col);
	}

	public void unlinkAllDown() {
		setParent(null);

		for (Object child : childs.toArray()) {
			((TownSettingCollection) child).unlinkAllDown();
		}
	}

	public TownSetting getSetting(String key) {
		for (TownSetting set : settings) {
			if (set.getSerializationKey().equalsIgnoreCase(key)) {
				return set;
			}
		}

		return null;
	}

	public Object getEffValue(String key) {
		TownSetting set = getSetting(key);
		if (set == null) {
			throw new RuntimeException("Unknown setting");
		}

		return set.effectiveValue;
	}

	public void forceChildsToInherit(String perm) throws CommandException {
		for (TownSettingCollection child : childs) {
			if (perm == null || perm.equals("")) {
				child.clearValues();
			} else {
				child.setValue(perm, null);
			}

			child.refreshSelf();
			child.forceChildsToInherit(perm);
			child.save();
		}
	}

	public void save() {
		if (saveHandler != null) {
			saveHandler.save(this, tag);
		}
	}

	private void refreshSelf() {
		for (TownSetting set : settings) {
			if (set.value == null) {
				if (parent != null) {
					set.effectiveValue = parent.getEffValue(set.getSerializationKey());
				}
			} else {
				set.effectiveValue = set.value;
			}

			unnest(set);
		}
	}

	public void setValue(String key, String value) throws CommandException {
		TownSetting set = getSetting(key);
		if (set == null) {
			throw new CommandException(String.format(Term.ErrPermSettingNotFound.toString(), key), key);
		}

		if (value != null && value.equals("?")) {
			throw new CommandException(String.format(Term.ErrPermSupportedValues.toString(), set.getValueType(), set.getValueDescription()));
		}

		try {
			set.setValue(value);
		} catch (Exception e) {
			String err = e.getClass().getSimpleName() + (e.toString() != null ? ": " + e.toString() : "");
			throw new CommandException(String.format(Term.ErrPermSupportedValues.toString(), err, set.getValueDescription()));
		}

		refresh();
		save();
	}

	public void refresh() {
		refreshSelf();

		for (TownSettingCollection child : childs) {
			child.refresh();
		}
	}

	public void deserialize(String val) {
		deserializeNorefresh(val);
		refresh();
	}

	public void deserializeNorefresh(String val) // used when the parent is set
													// later
	{
		if (val == null || val.equals("")) {
			return;
		}

		String[] splits = val.split("/");
		for (String line : splits) {
			String[] v = line.split(":");
			if (v.length != 2) {
				continue;
			}

			TownSetting set = getSetting(v[0]);
			if (set != null) {
				set.setValue(v[1]);
			}
		}
	}

	public String serialize() {
		List<String> ret = new ArrayList<String>();

		for (TownSetting set : settings) {
			if (set.value != null) {
				ret.add(set.getSerializationKey() + ":" + set.getValue());
			}
		}

		return Joiner.on('/').join(ret);
	}

	public void clearValues() {
		for (TownSetting set : settings) {
			set.value = null;
		}
	}

	private void clearValuesToWild() {
		for (TownSetting set : settings) {
			set.value = set.wildValue;
		}
	}

	public void show(ICommandSender cs, String title, String node, boolean all) {
		MyTown.sendChatToPlayer(cs, String.format("§6-- §ePermissions for %s§6 --", title));

		for (TownSetting set : settings) {
			if (!isWild || set.wildValue != null) {
				EntityPlayer p = (EntityPlayer) cs;
				if (all || ForgePerms.getPermissionManager().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.cmd.perm.set." + node + "." + set.getSerializationKey())
						|| ForgePerms.getPermissionManager().canAccess(p.username, p.worldObj.provider.getDimensionName(), "mytown.cmd.perm.set.*")) {
					MyTown.sendChatToPlayer(cs, String.format("§a%s §2[%s] : %s%s", set.getName(), set.getSerializationKey(), set.value == null ? "§d" : "§c", set.getVisualValue()));
				}
			}
		}
	}

	// elements
	public Permissions townMemberRights;
	public Permissions nationMemberRights;
	public Permissions outsiderRights;
	public Permissions friendRights;

	public boolean allowCartInteraction;
	public boolean allowStevecartsRailers;
	public boolean allowStevecartsMiners;
	public boolean allowRailcraftBores;
	public boolean allowBuildcraftMiners;
	public boolean allowClaimingNextTo;
	public boolean allowCCTurtles;
	public boolean allowTCBores;
	public boolean allowKillingMobsByNonResidents;

	public boolean disableCreepers;
	public boolean disableTNT;
	public boolean disableMobs;

	public boolean yCheckOn;
	public int yCheckFrom;
	public int yCheckTo;

	public boolean disableFireBall;

	protected void unnest(TownSetting set) {
		if (set.effectiveValue == null) {
			return; // wild has missing values
		}

		if (set.getSerializationKey().equals("town")) {
			townMemberRights = set.<Permissions> effValue();
		} else if (set.getSerializationKey().equals("nation")) {
			nationMemberRights = set.<Permissions> effValue();
		} else if (set.getSerializationKey().equals("out")) {
			outsiderRights = set.<Permissions> effValue();
		} else if (set.getSerializationKey().equals("friend")) {
			friendRights = set.<Permissions> effValue();
		} else if (set.getSerializationKey().equals("carts")) {
			allowCartInteraction = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("steverailer")) {
			allowStevecartsRailers = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("steveminer")) {
			allowStevecartsMiners = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("rcbore")) {
			allowRailcraftBores = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("bc")) {
			allowBuildcraftMiners = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("closeclaim")) {
			allowClaimingNextTo = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("ccturtles")) {
			allowCCTurtles = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("tcbores")) {
			allowTCBores = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("killmobs")) {
			allowKillingMobsByNonResidents = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("creepoff")) {
			disableCreepers = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("tntoff")) {
			disableTNT = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("mobsoff")) {
			disableMobs = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("yon")) {
			yCheckOn = set.getEffBoolean();
		} else if (set.getSerializationKey().equals("yfrom")) {
			yCheckFrom = set.getEffInt();
		} else if (set.getSerializationKey().equals("yto")) {
			yCheckTo = set.getEffInt();
		} else if (set.getSerializationKey().equals("fireballoff")) {
			disableFireBall = set.getEffBoolean();
		}
	}

	public void reset() {
		settings.clear();

		// label key default value wild value value limitation description value
		// conversion class
		settings.add(new TownSetting("Town member rights", "town", Permissions.Build, null, "choice:" + Permissions.getValuesDesc(), Permissions.class));
		settings.add(new TownSetting("Nation member rights", "nation", Permissions.Enter, null, "choice:" + Permissions.getValuesDesc(), Permissions.class));
		settings.add(new TownSetting("Outsider rights", "out", Permissions.Enter, Permissions.Build, "choice:" + Permissions.getValuesDesc(), Permissions.class));
		settings.add(new TownSetting("Friend rights", "friend", Permissions.Build, null, "choice:" + Permissions.getValuesDesc(), Permissions.class));

		settings.add(new TownSetting("Allow cart interaction", "carts", false, true, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Allow Stevescarts railers", "steverailer", false, true, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Allow Stevescarts miners", "steveminer", false, true, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Allow Railcraft bores", "rcbore", false, true, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Allow quarrys,filler,builders", "bc", false, true, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Allow Computercraft turtles", "ccturtles", false, true, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Allow Thaumcraft Arcane Bores", "tcbores", false, true, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Allow claiming next to", "closeclaim", false, null, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Allow everyone to kill mobs", "killmobs", true, null, "boolean:yes/no", boolean.class));
		// settings.add(new TownSetting("Allow PVP in town", "allowpvp", false,
		// null, "boolean:yes/no", boolean.class));

		settings.add(new TownSetting("Disable creeper explosion", "creepoff", false, false, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Disable TNT explosion", "tntoff", true, false, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Disable mobs", "mobsoff", false, null, "boolean:yes/no", boolean.class));

		settings.add(new TownSetting("Height enabled", "yon", false, null, "boolean:yes/no", boolean.class));
		settings.add(new TownSetting("Height check from", "yfrom", 0, null, "int:0-255", int.class));
		settings.add(new TownSetting("Height check to", "yto", 255, null, "int:0-255, below [yfrom]", int.class));

		settings.add(new TownSetting("Disable FireBall/Wither explosion", "fireballoff", true, false, "boolean:yes/no", boolean.class));

		if (!isRoot) {
			clearValues();
		} else {
			if (isWild) {
				clearValuesToWild();
			}

			refresh(); // non-roots will refresh on parent set
		}
	}
}
