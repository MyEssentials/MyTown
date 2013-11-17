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
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import ee.lutsu.alpha.mc.mytown.commands.CmdChannel;
import ee.lutsu.alpha.mc.mytown.commands.CmdChat;
import ee.lutsu.alpha.mc.mytown.commands.CmdDelHome;
import ee.lutsu.alpha.mc.mytown.commands.CmdEmote;
import ee.lutsu.alpha.mc.mytown.commands.CmdGamemode;
import ee.lutsu.alpha.mc.mytown.commands.CmdHome;
import ee.lutsu.alpha.mc.mytown.commands.CmdHomes;
import ee.lutsu.alpha.mc.mytown.commands.CmdMyTown;
import ee.lutsu.alpha.mc.mytown.commands.CmdMyTownAdmin;
import ee.lutsu.alpha.mc.mytown.commands.CmdOnline;
import ee.lutsu.alpha.mc.mytown.commands.CmdPrivateMsg;
import ee.lutsu.alpha.mc.mytown.commands.CmdReplyPrivateMsg;
import ee.lutsu.alpha.mc.mytown.commands.CmdSetHome;
import ee.lutsu.alpha.mc.mytown.commands.CmdSetSpawn;
import ee.lutsu.alpha.mc.mytown.commands.CmdSpawn;
import ee.lutsu.alpha.mc.mytown.commands.CmdTeleport;
import ee.lutsu.alpha.mc.mytown.commands.CmdWrk;
import ee.lutsu.alpha.mc.mytown.entities.ItemIdRange;
import ee.lutsu.alpha.mc.mytown.entities.Nation;
import ee.lutsu.alpha.mc.mytown.entities.Resident;
import ee.lutsu.alpha.mc.mytown.entities.SavedHomeList;
import ee.lutsu.alpha.mc.mytown.entities.Town;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.ISettingsSaveHandler;
import ee.lutsu.alpha.mc.mytown.event.PlayerEvents;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;
import ee.lutsu.alpha.mc.mytown.event.TickHandler;
import ee.lutsu.alpha.mc.mytown.event.WorldEvents;
import ee.lutsu.alpha.mc.mytown.event.tick.WorldBorder;
import ee.lutsu.alpha.mc.mytown.sql.Database;

@Mod(modid = "MyTown", name = "My Town", version = "1.6.1.10")
@NetworkMod(clientSideRequired = false, serverSideRequired = true)
public class MyTown {
    public static String MOD_NAME = "MyTown";
    public static String CONFIG_FOLDER = "config/MyTown/";
    public static String LIB_FOLDER = CONFIG_FOLDER + "lib/";
    public static String CONFIG_FILE = CONFIG_FOLDER + "MyTown.cfg";
    public static API mytownAPI = new APIHandler();

    public TownSettingCollection serverWildSettings = new TownSettingCollection(true, true);
    public TownSettingCollection serverSettings = new TownSettingCollection(true, false);
    public Map<Integer, TownSettingCollection> worldWildSettings = new HashMap<Integer, TownSettingCollection>();
    public LinkedList<ItemIdRange> carts = null;
    public LinkedList<ItemIdRange> leftClickAccessBlocks = null;

    @Instance("MyTown")
    public static MyTown instance;
    public Configuration config = new Configuration(new File(CONFIG_FILE));

    public List<CommandBase> commands = new ArrayList<CommandBase>();

    private void addCommands() {
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

        for (ChatChannel c : ChatChannel.values()) {
            commands.add(new CmdChat(c));
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent ev) {
        Log.init();
        addCommands();
        loadConfig();
    }

    @EventHandler
    public void modsLoaded(FMLServerStartedEvent var1) {
        try {
            MyTownDatasource.instance.init();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        PlayerEvents events = new PlayerEvents();
        MinecraftForge.EVENT_BUS.register(events);
        GameRegistry.registerPlayerTracker(events);
        
        //MinecraftForge.EVENT_BUS.register(ProtectionEvents.instance);
        //TickRegistry.registerTickHandler(ProtectionEvents.instance, Side.SERVER);
        
        TickRegistry.registerTickHandler(TickHandler.instance, Side.SERVER);
        MinecraftForge.EVENT_BUS.register(WorldEvents.instance);

        try {
            loadCommandsConfig(config);
            WorldBorder.instance.continueGeneratingChunks();
        } catch (Exception var8) {
            FMLLog.log(Level.SEVERE, var8, MOD_NAME + " was unable to load it\'s configuration successfully", new Object[0]);
            throw new RuntimeException(var8);
        } finally {
            config.save(); // re-save to add the missing configuration variables
        }

        Log.info("Loaded");
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent ev) throws InterruptedException {
        WorldBorder.instance.stopGenerators();
    }

    public void saveConfig() {
        config.save();
    }

    public void loadConfig() {
        try {
            config.load();

            loadGeneralConfigs(config);
            loadDatabaseConfigs(config);
            loadChatConfigs(config);
            loadExtraProtectionConfig(config);
            loadPerms(config);
            loadCostConfigs(config);

            TickHandler.instance.loadConfigs();
            WorldBorder.instance.loadConfig();
        } catch (Exception e) {
            Log.severe(MOD_NAME + " was unable to load it\'s configuration successfully", e);
            throw new RuntimeException(e);
        } finally {
            config.save(); // re-save to add the missing configuration variables
        }
    }

    public void reload() {
        loadConfig();

        ProtectionEvents.instance.reload();

        try {
            MyTownDatasource.instance.init();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private void loadGeneralConfigs(Configuration config) throws IOException {
        Property prop;

        prop = config.get("general", "Translations", "");
        prop.comment = "Filename in config folder with the term translations";

        if (prop.getString() != null && !prop.getString().equals("")) {
            TermTranslator.load(new File(CONFIG_FOLDER + prop.getString()), "custom", true);
        }

        prop = config.get("general", "NationAddsBlocks", 0);
        prop.comment = "How many town blocks the town gets for being in a nation";
        Nation.nationAddsBlocks = prop.getInt(0);

        prop = config.get("general", "NationAddsBlocksPerResident", 0);
        prop.comment = "How many town blocks each resident gives if the town is in a nation";
        Nation.nationAddsBlocksPerResident = prop.getInt(0);

        prop = config.get("general", "MinDistanceFromAnotherTown", 50);
        prop.comment = "How many blocks(chunks) apart have the town blocks be";
        Town.minDistanceFromOtherTown = prop.getInt(5);

        prop = config.get("general", "AllowFarawayClaims", true);
        prop.comment = "Whether players are allowed to claim chunks not connected to earlier ones";
        Town.allowFarawayClaims = prop.getBoolean(true);

        prop = config.get("general", "AllowTownMemberPvp", false);
        prop.comment = "First check. Can one town member hit a member of the same town? Anywhere. Also called friendlyfire";
        Resident.allowMemberToMemberPvp = prop.getBoolean(false);

        prop = config.get("general", "AllowPvpInTown", false);
        prop.comment = "Second check. Can anyone hit anyone in town? For PVP only. Does NOT turn friendly fire on";
        Town.allowFullPvp = prop.getBoolean(false);

        prop = config.get("general", "AllowMemberKillNonMember", true);
        prop.comment = "Third check. Can a member of the town kill someone who doesn't belong to his town?";
        Town.allowMemberToForeignPvp = prop.getBoolean(true);

        prop = config.get("general", "CartItemIds", "");
        prop.comment = "Defines the cart id's which can be placed on a rail with carts perm on. Includes all cart-types.";
        carts = ItemIdRange.parseList(Arrays.asList(prop.getString().split(";")));

        Town.pvpSafeTowns = config.get("general", "PVPSafeTown", "Spawn,Server", "Towns that PVP is disabled in, reguardless of the AllowPvpInTown setting.").getString().split(",");

        prop = config.get("general", "LeftClickAccessBlocks", "1000:2", "Which blocks should be considered as access when someone is hitting them. Like TE Barrels");
        leftClickAccessBlocks = ItemIdRange.parseList(Arrays.asList(prop.getString().split(";")));

        Resident.teleportToSpawnWaitSeconds = config.get("general", "SpawnTeleportTimeout", Resident.teleportToSpawnWaitSeconds, "How many seconds the /spawn teleport takes").getInt();
        Resident.teleportToHomeWaitSeconds = config.get("general", "HomeTeleportTimeout", Resident.teleportToHomeWaitSeconds, "How many seconds the /home teleport takes").getInt();

        SavedHomeList.defaultIsBed = config.get("general", "DefaultHomeIsBed", SavedHomeList.defaultIsBed, "Are the /sethome and /home commands with no home name linked to the bed location?").getBoolean(SavedHomeList.defaultIsBed);
    }

    private void loadCostConfigs(Configuration config) {
        config.addCustomCategoryComment("cost", "MyTown item based economy");
        config.addCustomCategoryComment("cost.list", "Defines what and how much costs. Set the amount to 0 to disable the cost. Syntax: [amount]x[item id]:[sub id]");
        for (Cost c : Cost.values()) {
            c.item = getItemStackConfig(config, "cost.list", c.name(), c.item, c.description);
        }

        if (!config.get("cost", "Enabled", true, "Enable the so called economy module?").getBoolean(true)) {
            Cost.disable();
        }

        Cost.homeSetNewAdditional = config.get("cost", "HomeCostAdditionPerHome", Cost.homeSetNewAdditional, "How much of the /sethome cost item is requested more for every home the player has when the player is creating a new home location. Ex. with 2 homes = /sethome cost + this * 2").getInt();
    }

    private static ItemStack getItemStackConfig(Configuration config, String cat, String node, ItemStack def, String comment) {
        String sDef = "";
        if (def != null) {
            sDef = def.stackSize + "x" + def.itemID + (def.getItemDamage() != 0 ? ":" + def.getItemDamage() : "");
        }

        String v = config.get(cat, node, sDef, comment).getString();
        if (v == null || v.trim().length() < 1) {
            return null;
        }

        int cnt, id, sub;
        String[] s1 = v.split("x");
        cnt = s1.length > 1 ? Integer.parseInt(s1[0]) : 1;

        String[] s2 = s1[s1.length - 1].split(":");
        id = Integer.parseInt(s2[0]);
        sub = s2.length > 1 ? Integer.parseInt(s2[1]) : 0;

        return new ItemStack(id, cnt, sub);
    }

    private void loadDatabaseConfigs(Configuration config) {
        Property prop;

        prop = config.get("database", "Type", "SQLite");
        prop.comment = "Database type to connect to";
        MyTownDatasource.instance.currentType = Database.Type.matchType(prop.getString());

        prop = config.get("database", "Prefix", "");
        prop.comment = "Table name prefix to use. <pre>_towns etc..";
        MyTownDatasource.instance.prefix = prop.getString();

        prop = config.get("database", "Username", "");
        prop.comment = "Username to use when connecting. Used by MySQL";
        MyTownDatasource.instance.username = prop.getString();

        prop = config.get("database", "Password", "");
        prop.comment = "Password to use when connecting. Used by MySQL";
        MyTownDatasource.instance.password = prop.getString();

        prop = config.get("database", "Host", "");
        prop.comment = "Hostname:Port of the db server. Used by MySQL";
        MyTownDatasource.instance.host = prop.getString();

        prop = config.get("database", "Database", "");
        prop.comment = "The database name. Used by MySQL";
        MyTownDatasource.instance.dbname = prop.getString();

        prop = config.get("database", "Path", CONFIG_FOLDER + "data.db");
        prop.comment = "The database file path. Used by SQLite";
        MyTownDatasource.instance.dbpath = prop.getString();
    }

    private void loadChatConfigs(Configuration config) {
        Property prop;

        prop = config.get("chat", "DisableAutomaticChannelUse", false);
        prop.comment = "Setting this stops player messages from using the MyTown channel functionality.\n";
        prop.comment += "Explicit call of channel commands (/g, /h, etc.) still works unless disabled separatedly";
        PlayerEvents.disableAutoChatChannelUsage = prop.getBoolean(false);

        prop = config.get("chat", "TextColoringPrefix", "$");
        prop.comment = "This is the prefix used for color codes in chat. Default value $\n";
        prop.comment += "When using with Bukkit plugins, it's recommended to change this to &";
        Formatter.colorPrefix = prop.getString();
        Formatter.generateColorPattern();

        prop = config.get("chat", "FormatChat", true);
        prop.comment = "Should the chat be formatted";
        Formatter.formatChat = prop.getBoolean(true);

        prop = config.get("chat", "ChatFormat", Term.ChatFormat.defaultVal);
        prop.comment = "Chat format to be used";
        Term.ChatFormat.defaultVal = prop.getString();

        prop = config.get("chat", "EmoteFormat", Term.EmoteFormat.defaultVal);
        prop.comment = "Emote format to be used";
        Term.EmoteFormat.defaultVal = prop.getString();

        prop = config.get("chat", "PrivMsgInFormat", Term.PrivMsgFormatIn.defaultVal);
        prop.comment = "Private message format to be used when receiving. Vars starting with $s mean sender";
        Term.PrivMsgFormatIn.defaultVal = prop.getString();

        prop = config.get("chat", "PrivMsgOutFormat", Term.PrivMsgFormatOut.defaultVal);
        prop.comment = "Private message format to be used when sending. Vars starting with $s mean sender";
        Term.PrivMsgFormatOut.defaultVal = prop.getString();

        prop = config.get("chat", "LocalDistance", 160);
        prop.comment = "How many blocks far does the local chat sound";
        ChatChannel.localChatDistance = prop.getInt(160);

        //prop = config.get("chat", "MaxChatLength", 32767);
        //prop.comment = "How many characters can one chat packet contain. It's global.";
        //Packet3Chat.maxChatLength = prop.getInt(32767);

        prop = config.get("chat", "DefaultChannel", ChatChannel.defaultChannel.name);
        prop.comment = "Default chat channel for newcomers";
        ChatChannel.defaultChannel = ChatChannel.parse(prop.getString());

        for (ChatChannel ch : ChatChannel.values()) {
            prop = config.get("chat", "Channel_" + ch.toString(), "");
            prop.comment = "<enabled>;<name>;<abbrevation>;<color>;<inlineswitch> like " + String.format("%s;%s;%s;%s", ch.enabled ? 1 : 0, ch.name, ch.abbrevation, ch.color);
            ch.load(prop.getString());
        }
    }

    private void loadExtraProtectionConfig(Configuration config) {
        ProtectionEvents.instance.enabled = config.get("protex", "Enabled", true, "Run the extra protections").getBoolean(true);
        ProtectionEvents.instance.dynamicEnabling = config.get("protex", "DynamicEnabling", true, "Load all modules for which mods are present").getBoolean(true);

        if (ProtectionEvents.instance.dynamicEnabling) {
            config.getCategory("protex").clear();

            config.get("protex", "Enabled", true, "Run the extra protections?").set(ProtectionEvents.instance.enabled);
            config.get("protex", "DynamicEnabling", true, "Load all modules for which mods are present").set(ProtectionEvents.instance.dynamicEnabling);
        } else {
            for (ProtBase prot : ProtectionEvents.getProtections()) {
                prot.enabled = config.get("protex", prot.getMod(), prot.defaultEnabled(), prot.getComment()).getBoolean(false);
            }
        }
    }

    private void loadCommandsConfig(Configuration config) {
        Property prop;
        ServerCommandManager mgr = (ServerCommandManager) MinecraftServer.getServer().getCommandManager();

        for (CommandBase cmd : commands) {
            prop = config.get("commands", "Enable_" + cmd.getCommandName(), true);
            prop.comment = String.format("Should the %s [/%s] command be used?", cmd.getClass().getSimpleName(), cmd.getCommandName());

            if (prop.getBoolean(true)) {
                mgr.registerCommand(cmd);
            }
        }
    }

    private void loadPerms(Configuration config) {
        Property prop;

        prop = config.get("serverperms", "Server", "");
        serverSettings.deserialize(prop.getString());

        serverSettings.saveHandler = new ISettingsSaveHandler() {
            @Override
            public void save(TownSettingCollection sender, Object tag) {
                MyTown.instance.config.get("serverperms", "Server", "").set(sender.serialize());
                MyTown.instance.config.save();
            }
        };

        prop = config.get("wildperms", "Server", "");
        serverWildSettings.deserialize(prop.getString());

        serverWildSettings.saveHandler = new ISettingsSaveHandler() {
            @Override
            public void save(TownSettingCollection sender, Object tag) {
                MyTown.instance.config.get("wildperms", "Server", "").set(sender.serialize());
                MyTown.instance.config.save();
            }
        };

        ConfigCategory cat = config.getCategory("wildperms");

        for (Property p : cat.values()) {
            if (!p.getName().startsWith("Dim_")) {
                continue;
            }

            int dim = Integer.parseInt(p.getName().substring(4));
            TownSettingCollection set = getWorldWildSettings(dim);
            set.deserialize(p.getString());
        }
    }

    public TownSettingCollection getWorldWildSettings(int w) {
        for (Entry<Integer, TownSettingCollection> set : worldWildSettings.entrySet()) {
            if (set.getKey() == w) {
                return set.getValue();
            }
        }

        TownSettingCollection set = new TownSettingCollection(false, true);
        set.tag = new Integer(w);
        set.setParent(serverWildSettings);
        set.saveHandler = new ISettingsSaveHandler() {
            @Override
            public void save(TownSettingCollection sender, Object tag) {
                int w = (Integer) tag;
                MyTown.instance.config.get("wildperms", "Dim_" + String.valueOf(w), "").set(sender.serialize());
                MyTown.instance.config.save();
            }
        };

        worldWildSettings.put(w, set);

        return set;
    }
    
    public static void sendChatToPlayer(ICommandSender sender, String msg) {
        if (sender instanceof MinecraftServer){
            Log.info("Sending msg...");
            Log.info(msg);
            return;
        }
        sender.sendChatToPlayer(ChatMessageComponent.createFromText(msg));
    }
}
