package ee.lutsu.alpha.mc.mytown;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.MinecraftForge;
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
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection;
import ee.lutsu.alpha.mc.mytown.entities.TownSettingCollection.ISettingsSaveHandler;
import ee.lutsu.alpha.mc.mytown.event.PlayerEvents;
import ee.lutsu.alpha.mc.mytown.event.ProtectionEvents;
import ee.lutsu.alpha.mc.mytown.event.TickHandler;
import ee.lutsu.alpha.mc.mytown.event.WorldEvents;
import ee.lutsu.alpha.mc.mytown.event.tick.WorldBorder;

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES)
@NetworkMod(clientSideRequired = false, serverSideRequired = true)
public class MyTown {
    public TownSettingCollection serverWildSettings = new TownSettingCollection(true, true);
    public TownSettingCollection serverSettings = new TownSettingCollection(true, false);
    public Map<Integer, TownSettingCollection> worldWildSettings = new HashMap<Integer, TownSettingCollection>();
    public LinkedList<ItemIdRange> carts = null;
    public LinkedList<ItemIdRange> leftClickAccessBlocks = null;

    @Instance("MyTown")
    public static MyTown instance;
    public Config config = new Config();

    public List<CommandBase> commands = new ArrayList<CommandBase>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent ev) {
        Log.init();
        addCommands();
        config.loadConfig();
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
        
        try {
			Class.forName("net.minecraftforge.event.world.BlockEvent.BreakEvent");
			MinecraftForge.EVENT_BUS.register(Class.forName("ee.lutsu.alpha.mc.mytown.event.ExtraEvents").newInstance());  //A sort of compat for older versions of Forge that don't contain all the events
			Log.info("ExtraEvents loaded");
		} catch (ClassNotFoundException e) {
			Log.warning("BlockBreak event missing, no worries though!");
		} catch (InstantiationException e){
			Log.warning("BlockBreak event missing, no worries though!");
		} catch (IllegalAccessException e){
			Log.warning("BlockBreak event missing, no worries though!");
        }
        
        MinecraftForge.EVENT_BUS.register(ProtectionEvents.instance);
        TickRegistry.registerTickHandler(ProtectionEvents.instance, Side.SERVER);
        
        TickRegistry.registerTickHandler(TickHandler.instance, Side.SERVER);
        MinecraftForge.EVENT_BUS.register(WorldEvents.instance);

        try {
            config.loadCommandsConfig();
            WorldBorder.instance.continueGeneratingChunks();
        } catch (Exception ex) {
            FMLLog.log(Level.SEVERE, ex, Constants.MODNAME + " was unable to load it\'s configuration successfully", new Object[0]);
            throw new RuntimeException(ex);
        } finally {
            config.save(); // re-save to add the missing configuration variables
        }

        Log.info("Loaded");
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent ev) throws InterruptedException {
        WorldBorder.instance.stopGenerators();
    }

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

    public void reload() {
        config.loadConfig();

        ProtectionEvents.instance.reload();

        try {
            MyTownDatasource.instance.init();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
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
            Log.info(msg);
            return;
        }
        sender.sendChatToPlayer(ChatMessageComponent.createFromText(msg));
    }
}
