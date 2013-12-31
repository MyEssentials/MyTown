package ee.lutsu.alpha.mc.mytown.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.base.Joiner;
import com.sperion.forgeperms.ForgePerms;

import ee.lutsu.alpha.mc.mytown.MyTown;

public class SettingCollection {
	public Object tag;
    public ISettingsSaveHandler saveHandler;
	private SettingCollection parent;
	private List<SettingCollection> children = new ArrayList<SettingCollection>();
	private Map<String, Setting> settings = new HashMap<String, Setting>();
	
	public void setParent(SettingCollection parent){
		if (this.parent != null) this.parent.removeChild(this);
		this.parent = parent;
		if (this.parent != null) this.parent.addChild(this);
	}
	
	protected void addChild(SettingCollection child){
		children.add(child);
	}
	
	protected void removeChild(SettingCollection child){
		children.remove(child);
	}
	
	public List<SettingCollection> getChildren(){
		return children;
	}
	
	public Map<String, Setting> getSettings(){
		return settings;
	}

    public void show(ICommandSender cs, String title, String node, boolean all) {
        EntityPlayer p = (EntityPlayer) cs;
    	Iterator<Setting> it = settings.values().iterator();
    	Setting set;
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("§6-- §ePermissions for %s§6 --\n", title));
        
        while (it.hasNext()){
        	set = it.next();
            if (all || ForgePerms.getPermissionManager().canAccess(p.getCommandSenderName(), p.worldObj.provider.getDimensionName(), "mytown.cmd.perm.set." + node + "." + set.getName())) {
            	builder.append(set.getDisplay());
            }
        }
        builder.append("§6----------------------------");
        
        MyTown.sendChatToPlayer(cs, builder.toString());
    }
	
	public Setting getSetting(String key){
		return settings.get(key);
	}

    public void unlinkAllDown() {
        setParent(null);

        for (SettingCollection child : children) {
            child.unlinkAllDown();
        }
    }
	
	public String serialize(){
		List<String> ret = new ArrayList<String>();
		Iterator<Setting> it = settings.values().iterator();
		Setting set;
		while (it.hasNext()){
			set = it.next();
            if (set.getValue() != null || set.getValue() != set.getDefaultValue()) {
                ret.add(set.serialize());
            }
		}
		return Joiner.on("/").join(ret);
	}

    public void save() {
        if (saveHandler != null) {
            saveHandler.save(this, tag);
        }
    }
	
	public void deserialize(String val){
        if (val == null || val.isEmpty() || val.equals("")) {
            return;
        }

        String[] splits = val.split("/");
        for (String line : splits) {
            String[] v = line.split(":");
            if (v.length != 2) {
                continue;
            }

            Setting set = getSetting(v[0]);
            if (set != null) {
                set.deserialize(v[1]);
            }
        }
	}
	
	public void refresh(){
    	Iterator<Setting> it = settings.values().iterator();
    	Setting set;
    	while (it.hasNext()){
    		set = it.next();
            if (set.getValue() == null) {
                if (parent != null) {
                	set.setValue(parent.getSetting(set.getName()));
                }
            } else {
                set.resetValue();
            }
    	}
    	
    	refreshChildren();
	}
	
	public void refreshChildren(){
		for(SettingCollection child : children){
			child.refresh();
		}
	}

    public interface ISettingsSaveHandler {
        void save(SettingCollection sender, Object tag);
    }
    
    public static SettingCollection generateCoreSettings(){
    	SettingCollection s = new SettingCollection();
        s.settings.put("carts", new Setting("carts", "Allow cart interaction", false, boolean.class));
        s.settings.put("steverailer", new Setting("steverailer", "Allow Stevescarts railers", false, boolean.class));
        s.settings.put("steveminer", new Setting("steveminer", "Allow Stevescarts miners", false, boolean.class));
        s.settings.put("rcbore", new Setting("rcbore", "Allow Railcraft bores", false, boolean.class));
        s.settings.put("bc", new Setting("bc", "Allow quarrys,filler,builders", false, boolean.class));
        s.settings.put("ccturtles", new Setting("ccturtles", "Allow Computercraft turtles", false, boolean.class));
        s.settings.put("tcbores", new Setting("tcbores", "Allow Thaumcraft Arcane Bores", false, boolean.class));
        s.settings.put("gravigunoff", new Setting("gravigunoff", "Disable Throwing of Blocks", true, boolean.class));
        s.settings.put("closeclaim", new Setting("closeclaim", "Allow claiming next to", false, boolean.class));
//        s.settings.put("killmobs", new Setting("killmobs", "Allow everyone to kill mobs", true, boolean.class));
        s.settings.put("fireballoff", new Setting("fireballoff", "Disable FireBall/Wither explosion", true, boolean.class));
        s.settings.put("creepoff", new Setting("creepoff", "Disable creeper explosion", false, boolean.class));
        s.settings.put("tntoff", new Setting("tntoff", "Disable TNT explosion", true, boolean.class));
        s.settings.put("mobsoff", new Setting("mobsoff", "Disable mobs", false, boolean.class));
        s.settings.put("yon", new Setting("yon", "Height enabled", false, boolean.class));
        s.settings.put("yfrom", new Setting("yfrom", "Height check from", 0, int.class));
        s.settings.put("yto", new Setting("yto", "Height check to", 255, int.class));
    	return s;
    }
    
    public static SettingCollection generateOutsiderSettings(){
    	SettingCollection s = new SettingCollection();
    	s.settings.put("roam", new Setting("roam", "Allows them to enter", true, boolean.class));
    	s.settings.put("build", new Setting("build", "Allows them to build", false, boolean.class));
    	s.settings.put("open", new Setting("open", "Allows them to open doors, trapdoors, etc", false, boolean.class));
    	s.settings.put("activate", new Setting("activate", "Allows them to activate buttons, switches, etc", false, boolean.class));
    	s.settings.put("container", new Setting("container", "Allows them to open containers (chests, machines, etc)", false, boolean.class));
    	s.settings.put("attackmobs", new Setting("attackmobs", "allows them to attack mobs", false, boolean.class));
    	s.settings.put("attackcreatures", new Setting("attackcreatures", "allows them to attack creatures", false, boolean.class));
    	return s;
    }
    
    public static SettingCollection generateTownMemberSettings(){
    	SettingCollection s = new SettingCollection();
    	s.settings.put("roam", new Setting("roam", "Allows them to enter", true, boolean.class));
    	s.settings.put("build", new Setting("build", "Allows them to build", true, boolean.class));
    	s.settings.put("open", new Setting("open", "Allows them to open doors, trapdoors, etc", true, boolean.class));
    	s.settings.put("activate", new Setting("activate", "Allows them to activate buttons, switches, etc", true, boolean.class));
    	s.settings.put("container", new Setting("container", "Allows them to open containers (chests, machines, etc)", true, boolean.class));
    	s.settings.put("attackmobs", new Setting("attackmobs", "allows them to attack mobs", true, boolean.class));
    	s.settings.put("attackcreatures", new Setting("attackcreatures", "allows them to attack creatures", true, boolean.class));
    	return s;
    }
}