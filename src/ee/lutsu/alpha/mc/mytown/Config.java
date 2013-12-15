package ee.lutsu.alpha.mc.mytown;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
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
import ee.lutsu.alpha.mc.mytown.event.tick.WorldBorder;
import ee.lutsu.alpha.mc.mytown.sql.Database;

public class Config extends Configuration {
    public Config(){
        super(new File(Constants.CONFIG_FILE));
    }

    public void saveConfig() {
        save();
    }

    public void loadConfig() {
        try {
            load();

            loadGeneralConfigs();
            loadDatabaseConfigs();
            loadChatConfigs();
            loadExtraProtectionConfig();
            loadPerms();
            loadCostConfigs();

            TickHandler.instance.loadConfigs();
            WorldBorder.instance.loadConfig();
        } catch (Exception e) {
            Log.severe(Constants.MODNAME + " was unable to load it\'s configuration successfully", e);
            throw new RuntimeException(e);
        } finally {
            save(); // re-save to add the missing configuration variables
        }
    }
    
    private void loadGeneralConfigs() throws IOException {
        Property prop;
        
        prop = get("general", "TermDumpFile", "");
        prop.comment = "Filename to dump the terms to (useful for a starting translation file)";
        Term.dumpLangFile(Constants.CONFIG_FOLDER + prop.getString());

        prop = get("general", "Translations", "");
        prop.comment = "Filename in config folder with the term translations";

        if (prop.getString() != null && !prop.getString().equals("")) {
            TermTranslator.load(new File(Constants.CONFIG_FOLDER + prop.getString()), "custom", true);
        }

        prop = get("general", "NationAddsBlocks", 0);
        prop.comment = "How many town blocks the town gets for being in a nation";
        Nation.nationAddsBlocks = prop.getInt(0);

        prop = get("general", "NationAddsBlocksPerResident", 0);
        prop.comment = "How many town blocks each resident gives if the town is in a nation";
        Nation.nationAddsBlocksPerResident = prop.getInt(0);

        prop = get("general", "MinDistanceFromAnotherTown", 50);
        prop.comment = "How many blocks(chunks) apart have the town blocks be";
        Town.minDistanceFromOtherTown = prop.getInt(5);

        prop = get("general", "AllowFarawayClaims", true);
        prop.comment = "Whether players are allowed to claim chunks not connected to earlier ones";
        Town.allowFarawayClaims = prop.getBoolean(true);

        prop = get("general", "AllowTownMemberPvp", false);
        prop.comment = "First check. Can one town member hit a member of the same town? Anywhere. Also called friendlyfire";
        Resident.allowMemberToMemberPvp = prop.getBoolean(false);

        prop = get("general", "AllowPvpInTown", false);
        prop.comment = "Second check. Can anyone hit anyone in town? For PVP only. Does NOT turn friendly fire on";
        Town.allowFullPvp = prop.getBoolean(false);

        prop = get("general", "AllowMemberKillNonMember", true);
        prop.comment = "Third check. Can a member of the town kill someone who doesn't belong to his town?";
        Town.allowMemberToForeignPvp = prop.getBoolean(true);

        prop = get("general", "CartItemIds", "");
        prop.comment = "Defines the cart id's which can be placed on a rail with carts perm on. Includes all cart-types.";
        MyTown.instance.carts = ItemIdRange.parseList(Arrays.asList(prop.getString().split(";")));

        Town.pvpSafeTowns = get("general", "PVPSafeTown", "Spawn,Server", "Towns that PVP is disabled in, reguardless of the AllowPvpInTown setting.").getString().split(",");

        prop = get("general", "LeftClickAccessBlocks", "1000:2", "Which blocks should be considered as access when someone is hitting them. Like TE Barrels");
        MyTown.instance.leftClickAccessBlocks = ItemIdRange.parseList(Arrays.asList(prop.getString().split(";")));

        Resident.teleportToSpawnWaitSeconds = get("general", "SpawnTeleportTimeout", Resident.teleportToSpawnWaitSeconds, "How many seconds the /spawn teleport takes").getInt();
        Resident.teleportToHomeWaitSeconds = get("general", "HomeTeleportTimeout", Resident.teleportToHomeWaitSeconds, "How many seconds the /home teleport takes").getInt();

        SavedHomeList.defaultIsBed = get("general", "DefaultHomeIsBed", SavedHomeList.defaultIsBed, "Are the /sethome and /home commands with no home name linked to the bed location?").getBoolean(SavedHomeList.defaultIsBed);
    }
    
    private void loadDatabaseConfigs() {
        Property prop;

        prop = get("database", "Type", "SQLite");
        prop.comment = "Database type to connect to";
        MyTownDatasource.instance.currentType = Database.Type.matchType(prop.getString());

        prop = get("database", "Prefix", "");
        prop.comment = "Table name prefix to use. <pre>_towns etc..";
        MyTownDatasource.instance.prefix = prop.getString();

        prop = get("database", "Username", "");
        prop.comment = "Username to use when connecting. Used by MySQL";
        MyTownDatasource.instance.username = prop.getString();

        prop = get("database", "Password", "");
        prop.comment = "Password to use when connecting. Used by MySQL";
        MyTownDatasource.instance.password = prop.getString();

        prop = get("database", "Host", "");
        prop.comment = "Hostname:Port of the db server. Used by MySQL";
        MyTownDatasource.instance.host = prop.getString();

        prop = get("database", "Database", "");
        prop.comment = "The database name. Used by MySQL";
        MyTownDatasource.instance.dbname = prop.getString();

        prop = get("database", "Path", Constants.CONFIG_FOLDER + "data.db");
        prop.comment = "The database file path. Used by SQLite";
        MyTownDatasource.instance.dbpath = prop.getString();
    }
    
    private void loadChatConfigs() {
        Property prop;

        prop = get("chat", "DisableAutomaticChannelUse", false);
        prop.comment = "Setting this stops player messages from using the MyTown channel functionality.\n";
        prop.comment += "Explicit call of channel commands (/g, /h, etc.) still works unless disabled separatedly";
        PlayerEvents.disableAutoChatChannelUsage = prop.getBoolean(false);

        prop = get("chat", "TextColoringPrefix", "$");
        prop.comment = "This is the prefix used for color codes in chat. Default value $\n";
        prop.comment += "When using with Bukkit plugins, it's recommended to change this to &";
        Formatter.colorPrefix = prop.getString();
        Formatter.generateColorPattern();

        prop = get("chat", "FormatChat", true);
        prop.comment = "Should the chat be formatted";
        Formatter.formatChat = prop.getBoolean(true);

        prop = get("chat", "ChatFormat", Term.ChatFormat.defaultVal);
        prop.comment = "Chat format to be used";
        Term.ChatFormat.defaultVal = prop.getString();

        prop = get("chat", "EmoteFormat", Term.EmoteFormat.defaultVal);
        prop.comment = "Emote format to be used";
        Term.EmoteFormat.defaultVal = prop.getString();

        prop = get("chat", "PrivMsgInFormat", Term.PrivMsgFormatIn.defaultVal);
        prop.comment = "Private message format to be used when receiving. Vars starting with $s mean sender";
        Term.PrivMsgFormatIn.defaultVal = prop.getString();

        prop = get("chat", "PrivMsgOutFormat", Term.PrivMsgFormatOut.defaultVal);
        prop.comment = "Private message format to be used when sending. Vars starting with $s mean sender";
        Term.PrivMsgFormatOut.defaultVal = prop.getString();

        prop = get("chat", "LocalDistance", 160);
        prop.comment = "How many blocks far does the local chat sound";
        ChatChannel.localChatDistance = prop.getInt(160);

        prop = get("chat", "DefaultChannel", ChatChannel.defaultChannel.name);
        prop.comment = "Default chat channel for newcomers";
        ChatChannel.defaultChannel = ChatChannel.parse(prop.getString());

        for (ChatChannel ch : ChatChannel.values()) {
            prop = get("chat", "Channel_" + ch.toString(), "");
            prop.comment = "<enabled>;<name>;<abbrevation>;<color>;<inlineswitch> like " + String.format("%s;%s;%s;%s", ch.enabled ? 1 : 0, ch.name, ch.abbrevation, ch.color);
            ch.load(prop.getString());
        }
    }
    
    private void loadExtraProtectionConfig() {
        ProtectionEvents.instance.enabled = get("protex", "Enabled", true, "Run the extra protections").getBoolean(true);
        ProtectionEvents.instance.dynamicEnabling = get("protex", "DynamicEnabling", true, "Load all modules for which mods are present").getBoolean(true);

        if (ProtectionEvents.instance.dynamicEnabling) {
            getCategory("protex").clear();

            get("protex", "Enabled", true, "Run the extra protections?").set(ProtectionEvents.instance.enabled);
            get("protex", "DynamicEnabling", true, "Load all modules for which mods are present").set(ProtectionEvents.instance.dynamicEnabling);
        } else {
            for (ProtBase prot : ProtectionEvents.getProtections()) {
                prot.enabled = get("protex", prot.getMod(), prot.defaultEnabled(), prot.getComment()).getBoolean(false);
            }
        }
    }

    private void loadPerms() {
        Property prop;

        prop = get("serverperms", "Server", "");
        MyTown.instance.serverSettings.deserialize(prop.getString());

        MyTown.instance.serverSettings.saveHandler = new ISettingsSaveHandler() {
            @Override
            public void save(TownSettingCollection sender, Object tag) {
                MyTown.instance.config.get("serverperms", "Server", "").set(sender.serialize());
                MyTown.instance.config.save();
            }
        };

        prop = get("wildperms", "Server", "");
        MyTown.instance.serverWildSettings.deserialize(prop.getString());

        MyTown.instance.serverWildSettings.saveHandler = new ISettingsSaveHandler() {
            @Override
            public void save(TownSettingCollection sender, Object tag) {
                get("wildperms", "Server", "").set(sender.serialize());
                MyTown.instance.config.save();
            }
        };

        ConfigCategory cat = getCategory("wildperms");

        for (Property p : cat.values()) {
            if (!p.getName().startsWith("Dim_")) {
                continue;
            }

            int dim = Integer.parseInt(p.getName().substring(4));
            TownSettingCollection set = MyTown.instance.getWorldWildSettings(dim);
            set.deserialize(p.getString());
        }
    }

    private void loadCostConfigs() {
        addCustomCategoryComment("cost", "MyTown item based economy");
        addCustomCategoryComment("cost.list", "Defines what and how much costs. Set the amount to 0 to disable the cost. Syntax: [amount]x[item id]:[sub id]");
        for (Cost c : Cost.values()) {
            c.item = getItemStackConfig("cost.list", c.name(), c.item, c.description);
        }

        if (!get("cost", "Enabled", true, "Enable the so called economy module?").getBoolean(true)) {
            Cost.disable();
        }

        Cost.homeSetNewAdditional = get("cost", "HomeCostAdditionPerHome", Cost.homeSetNewAdditional, "How much of the /sethome cost item is requested more for every home the player has when the player is creating a new home location. Ex. with 2 homes = /sethome cost + this * 2").getInt();
    }

    public void loadCommandsConfig() {
        Property prop;
        ServerCommandManager mgr = (ServerCommandManager) MinecraftServer.getServer().getCommandManager();

        for (CommandBase cmd : MyTown.instance.commands) {
            prop = get("commands", "Enable_" + cmd.getCommandName(), true);
            prop.comment = String.format("Should the %s [/%s] command be used?", cmd.getClass().getSimpleName(), cmd.getCommandName());

            if (prop.getBoolean(true)) {
                mgr.registerCommand(cmd);
            }
        }
    }

    private ItemStack getItemStackConfig(String cat, String node, ItemStack def, String comment) {
        String sDef = "";
        if (def != null) {
            sDef = def.stackSize + "x" + def.itemID + (def.getItemDamage() != 0 ? ":" + def.getItemDamage() : "");
        }

        String v = get(cat, node, sDef, comment).getString();
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
}