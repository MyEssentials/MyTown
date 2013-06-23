package ee.lutsu.alpha.mc.mytown;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import ee.lutsu.alpha.mc.mytown.commands.*;
import ee.lutsu.alpha.mc.mytown.entities.ItemIdRange;
import ee.lutsu.alpha.mc.mytown.entities.Nation;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.SavedHomeList;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.ISettingsSaveHandler;
import ee.lutsu.alpha.mc.mytown.event.*;
import ee.lutsu.alpha.mc.mytown.event.tick.WorldBorder;
import ee.lutsu.alpha.mc.mytown.sql.Database;

@Mod(
        modid = "MyTown",
        name = "My Town",
        version = "1.5.0.0"
)
@NetworkMod(
        clientSideRequired = false,
        serverSideRequired = true
)
public class MyTown
{
	public static String MOD_NAME = "MyTown";
	public static String CONFIG_FOLDER = "config/MyTown/";
	public static String LIB_FOLDER = CONFIG_FOLDER +"lib/";
	public static String CONFIG_FILE = CONFIG_FOLDER + "MyTown.cfg";
	
	public TownSettingCollection serverWildSettings = new TownSettingCollection(true, true);
	public TownSettingCollection serverSettings = new TownSettingCollection(true, false);
	public Map<Integer, TownSettingCollection> worldWildSettings = new HashMap<Integer, TownSettingCollection>();
	public LinkedList<ItemIdRange> carts = null;
	public LinkedList<ItemIdRange> leftClickAccessBlocks = null;
	
    @Mod.Instance("MyTown")
    public static MyTown instance;
    public Configuration config = new Configuration(new File(CONFIG_FILE));
    
	public List<CommandBase> commands = new ArrayList<CommandBase>();
	
	private void addCommands()
	{
		commands.add(new CmdMyTown());
		commands.add(new CmdMyTownAdmin());
		commands.add(new CmdChannel());
		commands.add(new CmdGamemode());
		commands.add(new CmdWrk());
		commands.add(new CmdSpawn());
		commands.add(new CmdTeleport());
		commands.add(new CmdSetSpawn());
		commands.add(new CmdOnline());
		commands.add(new CmdEmote());
		commands.add(new CmdPrivateMsg());
		commands.add(new CmdReplyPrivateMsg());
		commands.add(new CmdHomes());
		commands.add(new CmdHome());
		commands.add(new CmdSetHome());
		commands.add(new CmdDelHome());
		
		for(ChatChannel c : ChatChannel.values())
			commands.add(new CmdChat(c));
	}

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent ev)
    {
    	addCommands();
        loadConfig();
    }

    @Mod.ServerStarted
    public void modsLoaded(FMLServerStartedEvent var1)
    {
    	try
    	{
    		MyTownDatasource.instance.init();
    	}
    	catch(Exception ex)
    	{
    		throw new RuntimeException(ex.getMessage(), ex);
    	}
    	
    	PlayerEvents events = new PlayerEvents();
    	MinecraftForge.EVENT_BUS.register(events);
    	GameRegistry.registerPlayerTracker(events);
    	MinecraftForge.EVENT_BUS.register(ProtectionEvents.instance);
    	TickRegistry.registerTickHandler(ProtectionEvents.instance, Side.SERVER);
    	TickRegistry.registerTickHandler(TickHandler.instance, Side.SERVER);
    	MinecraftForge.EVENT_BUS.register(WorldEvents.instance);
    	
        try
        {
            loadCommandsConfig(config);
            WorldBorder.instance.continueGeneratingChunks();
        }
        catch (Exception var8)
        {
            FMLLog.log(Level.SEVERE, var8, MOD_NAME + " was unable to load it\'s configuration successfully", new Object[0]);
            throw new RuntimeException(var8);
        }
        finally
        {
            config.save(); // re-save to add the missing configuration variables
        }

		Log.info("Loaded");
    }
    
    @Mod.ServerStopping
    public void serverStopping(FMLServerStoppingEvent ev) throws InterruptedException
    {
    	WorldBorder.instance.stopGenerators();
    }

    public void saveConfig()
    {
    	config.save();
    }
    
    public void loadConfig()
    {
        try
        {
            config.load();
            
            loadGeneralConfigs(config);
            loadDatabaseConfigs(config);
            loadChatConfigs(config);
            loadExtraProtectionConfig(config);
            loadPerms(config);
            loadCostConfigs(config);
            
            TickHandler.instance.loadConfigs();
            WorldBorder.instance.loadConfig();
        }
        catch (Exception e)
        {
            Log.severe(MOD_NAME + " was unable to load it\'s configuration successfully", e);
            throw new RuntimeException(e);
        }
        finally
        {
            config.save(); // re-save to add the missing configuration variables
        }
    }
    
    public void reload()
    {
    	loadConfig();

    	ProtectionEvents.instance.reload();
    	
    	try
    	{
    		MyTownDatasource.instance.init();
    	}
    	catch(Exception ex)
    	{
    		throw new RuntimeException(ex.getMessage(), ex);
    	}
    }
    
    private void loadGeneralConfigs(Configuration config) throws IOException
    {
    	Property prop; 
    	
        prop = config.get("General", "Translations", "");
        prop.comment = "Filename in config folder with the term translations";
        
        if (prop.getString() != null && !prop.getString().equals(""))
        	TermTranslator.load(new File(CONFIG_FOLDER + prop.getString()), "custom", true);

        prop = config.get("General", "NationAddsBlocks", 0);
        prop.comment = "How many town blocks the town gets for being in a nation";
        Nation.nationAddsBlocks = prop.getInt(0);
        
        prop = config.get("General", "NationAddsBlocksPerResident", 0);
        prop.comment = "How many town blocks each resident gives if the town is in a nation";
        Nation.nationAddsBlocksPerResident = prop.getInt(0);
        
        prop = config.get("General", "MinDistanceFromAnotherTown", 50);
        prop.comment = "How many blocks(chunks) apart have the town blocks be";
        Town.minDistanceFromOtherTown = prop.getInt(5);
        
        prop = config.get("General", "AllowTownMemberPvp", false);
        prop.comment = "First check. Can one town member hit a member of the same town? Anywhere. Also called friendlyfire";
        Resident.allowMemberToMemberPvp = prop.getBoolean(false);
        
        prop = config.get("General", "AllowPvpInTown", false);
        prop.comment = "Second check. Can anyone hit anyone in town? For PVP only. Does NOT turn friendly fire on";
        Town.allowFullPvp = prop.getBoolean(false);
        
        prop = config.get("General", "AllowMemberKillNonMember", true);
        prop.comment = "Third check. Can a member of the town kill someone who doesn't belong to his town?";
        Town.allowMemberToForeignPvp = prop.getBoolean(true);

        prop = config.get("General", "CartItemIds", "");
        prop.comment = "Defines the cart id's which can be placed on a rail with carts perm on. Includes all cart-types.";
        carts = ItemIdRange.parseList(Arrays.asList(prop.getString().split(";")));
        
        Town.pvpSafeTowns = config.get("General", "PVPSafeTown", "Spawn,Server", "Towns that PVP is disabled in, reguardless of the AllowPvpInTown setting.").getString().split(",");
        
        prop = config.get("General", "LeftClickAccessBlocks", "1000:2", "Which blocks should be considered as access when someone is hitting them. Like TE Barrels");
        leftClickAccessBlocks = ItemIdRange.parseList(Arrays.asList(prop.getString().split(";")));

        Resident.teleportToSpawnWaitSeconds = config.get("General", "SpawnTeleportTimeout", Resident.teleportToSpawnWaitSeconds, "How many seconds the /spawn teleport takes").getInt();
        Resident.teleportToHomeWaitSeconds = config.get("General", "HomeTeleportTimeout", Resident.teleportToHomeWaitSeconds, "How many seconds the /home teleport takes").getInt();
        
        SavedHomeList.defaultIsBed = config.get("General", "DefaultHomeIsBed", SavedHomeList.defaultIsBed, "Are the /sethome and /home commands with no home name linked to the bed location?").getBoolean(SavedHomeList.defaultIsBed);
    }
    
    private void loadCostConfigs(Configuration config)
    {
    	config.addCustomCategoryComment("cost", "MyTown item based economy");
    	config.addCustomCategoryComment("cost.list", "Defines what and how much costs. Set the amount to 0 to disable the cost. Syntax: [amount]x[item id]:[sub id]");
    	for (Cost c : Cost.values())
    		c.item = getItemStackConfig(config, "cost.list", c.name(), c.item, c.description);
    	
    	if (!config.get("cost", "Enabled", true, "Enable the so called economy module?").getBoolean(true))
    		Cost.disable();
    	
    	Cost.homeSetNewAdditional = config.get("cost", "HomeCostAdditionPerHome", Cost.homeSetNewAdditional, "How much of the /sethome cost item is requested more for every home the player has when the player is creating a new home location. Ex. with 2 homes = /sethome cost + this * 2").getInt();
    }
    
    private static ItemStack getItemStackConfig(Configuration config, String cat, String node, ItemStack def, String comment)
    {
    	String sDef = "";
    	if (def != null)
    		sDef = def.stackSize + "x" + def.itemID + (def.getItemDamage() != 0 ? ":" + def.getItemDamage() : "");
    	
    	String v = config.get(cat, node, sDef, comment).getString();
    	if (v == null || v.trim().length() < 1)
    		return null;
    	
    	int cnt, id, sub;
    	String[] s1 = v.split("x");
    	cnt = s1.length > 1 ? Integer.parseInt(s1[0]) : 1;
    	
    	String[] s2 = s1[s1.length - 1].split(":");
    	id = Integer.parseInt(s2[0]);
    	sub = s2.length > 1 ? Integer.parseInt(s2[1]) : 0;
    	
    	return new ItemStack(id, cnt, sub);
    }
    
    private void loadDatabaseConfigs(Configuration config)
    {
        Property prop; 
        
        prop = config.get("Database", "Type", "SQLite");
        prop.comment = "Database type to connect to";
        MyTownDatasource.instance.currentType = Database.Type.matchType(prop.getString());
    	
        prop = config.get("Database", "Prefix", "");
        prop.comment = "Table name prefix to use. <pre>_towns etc..";
        MyTownDatasource.instance.prefix = prop.getString();
    	
        prop = config.get("Database", "Username", "");
        prop.comment = "Username to use when connecting. Used by MySQL";
        MyTownDatasource.instance.username = prop.getString();
    	
        prop = config.get("Database", "Password", "");
        prop.comment = "Password to use when connecting. Used by MySQL";
        MyTownDatasource.instance.password = prop.getString();
    	
        prop = config.get("Database", "Host", "");
        prop.comment = "Hostname:Port of the db server. Used by MySQL";
        MyTownDatasource.instance.host = prop.getString();
    	
        prop = config.get("Database", "Database", "");
        prop.comment = "The database name. Used by MySQL";
        MyTownDatasource.instance.dbname = prop.getString();
    	
        prop = config.get("Database", "Path", CONFIG_FOLDER + "data.db");
        prop.comment = "The database file path. Used by SQLite";
        MyTownDatasource.instance.dbpath = prop.getString();
    }
    
    private void loadChatConfigs(Configuration config)
    {
        Property prop; 
        
        prop = config.get("Chat", "FormatChat", true);
        prop.comment = "Should the chat be formatted";
        Formatter.formatChat = prop.getBoolean(true);
        
        prop = config.get("Chat", "ChatFormat", Term.ChatFormat.defaultVal);
        prop.comment = "Chat format to be used";
        Term.ChatFormat.defaultVal = prop.getString();
        
        prop = config.get("Chat", "EmoteFormat", Term.EmoteFormat.defaultVal);
        prop.comment = "Emote format to be used";
        Term.EmoteFormat.defaultVal = prop.getString();
        
        prop = config.get("Chat", "PrivMsgInFormat", Term.PrivMsgFormatIn.defaultVal);
        prop.comment = "Private message format to be used when receiving. Vars starting with $s mean sender";
        Term.PrivMsgFormatIn.defaultVal = prop.getString();
        
        prop = config.get("Chat", "PrivMsgOutFormat", Term.PrivMsgFormatOut.defaultVal);
        prop.comment = "Private message format to be used when sending. Vars starting with $s mean sender";
        Term.PrivMsgFormatOut.defaultVal = prop.getString();
        
        prop = config.get("Chat", "LocalDistance", 160);
        prop.comment = "How many blocks far does the local chat sound";
        ChatChannel.localChatDistance = prop.getInt(160);
        
        prop = config.get("Chat", "MaxChatLength", 32767);
        prop.comment = "How many characters can one chat packet contain. It's global.";
        Packet3Chat.maxChatLength = prop.getInt(32767);
        
        prop = config.get("Chat", "DefaultChannel", ChatChannel.defaultChannel.name);
        prop.comment = "Default chat channel for newcomers";
        ChatChannel.defaultChannel = ChatChannel.parse(prop.getString());
        
        for (ChatChannel ch : ChatChannel.values())
        {
            prop = config.get("Chat", "Channel_" + ch.toString(), "");
            prop.comment = "<enabled>;<name>;<abbrevation>;<color>;<inlineswitch> like " + String.format("%s;%s;%s;%s", ch.enabled ? 1 : 0, ch.name, ch.abbrevation, ch.color);
            ch.load(prop.getString());
        }
    }
    
    private void loadExtraProtectionConfig(Configuration config)
    {
        ProtectionEvents.instance.enabled = config.get("ProtEx", "Enabled", true, "Run the extra protections").getBoolean(true);
        ProtectionEvents.instance.dynamicEnabling = config.get("ProtEx", "DynamicEnabling", true, "Load all modules for which mods are present").getBoolean(true);
        
        if (ProtectionEvents.instance.dynamicEnabling) // delete nodes
        {
        	config.getCategory("ProtEx").clear();
        	
            config.get("ProtEx", "Enabled", true, "Run the extra protections?").set(ProtectionEvents.instance.enabled);
            config.get("ProtEx", "DynamicEnabling", true, "Load all modules for which mods are present").set(ProtectionEvents.instance.dynamicEnabling);
        }
        else
        {
	        for (ProtBase prot : ProtectionEvents.getProtections())
	            prot.enabled = config.get("ProtEx", prot.getMod(), prot.defaultEnabled(), prot.getComment()).getBoolean(false);
        }
    }
    
    private void loadCommandsConfig(Configuration config)
    {
        Property prop; 
    	ServerCommandManager mgr = (ServerCommandManager)MinecraftServer.getServer().getCommandManager();
    	
    	for (CommandBase cmd : commands)
    	{
            prop = config.get("Commands", "Enable_" + cmd.getCommandName(), true);
            prop.comment = String.format("Should the %s [/%s] command be used?", cmd.getClass().getSimpleName(), cmd.getCommandName());
            
            if (prop.getBoolean(true))
            	mgr.registerCommand(cmd);
    	}
    }
    
    private void loadPerms(Configuration config)
    {
        Property prop; 
        
        prop = config.get("ServerPerms", "Server", "");
        serverSettings.deserialize(prop.getString());
        
        serverSettings.saveHandler = new ISettingsSaveHandler()
        {
			public void save(TownSettingCollection sender, Object tag) 
			{
				MyTown.instance.config.get("ServerPerms", "Server", "").set(sender.serialize());
				MyTown.instance.config.save();
			}
        };
        
        prop = config.get("WildPerms", "Server", "");
        serverWildSettings.deserialize(prop.getString());
        
        serverWildSettings.saveHandler = new ISettingsSaveHandler()
        {
			public void save(TownSettingCollection sender, Object tag) 
			{
				MyTown.instance.config.get("WildPerms", "Server", "").set(sender.serialize());
				MyTown.instance.config.save();
			}
        };
        
        ConfigCategory cat = config.getCategory("WildPerms");

        for (Property p : cat.values())
        {
        	if (!p.getName().startsWith("Dim_"))
        		continue;

    		int dim = Integer.parseInt(p.getName().substring(4));
    		TownSettingCollection set = getWorldWildSettings(dim);
    		set.deserialize(p.getString());
        }
    }
    
    public TownSettingCollection getWorldWildSettings(int w)
    {
    	for (Entry<Integer, TownSettingCollection> set : worldWildSettings.entrySet())
    	{
    		if (set.getKey() == w)
    			return set.getValue();
    	}
    	
    	TownSettingCollection set = new TownSettingCollection(false, true);
    	set.tag = new Integer(w);
    	set.setParent(serverWildSettings);
    	set.saveHandler = new ISettingsSaveHandler()
        {
			public void save(TownSettingCollection sender, Object tag) 
			{
				int w = (Integer)tag;
				MyTown.instance.config.get("WildPerms", "Dim_" + String.valueOf(w), "").set(sender.serialize());
				MyTown.instance.config.save();
			}
        };
        
        worldWildSettings.put(w, set);
        
        return set;
    }
   
}
