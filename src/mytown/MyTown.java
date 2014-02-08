package mytown;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import mytown.cmd.CmdAdmin;
import mytown.cmd.CmdChannel;
import mytown.cmd.CmdChat;
import mytown.cmd.CmdDelHome;
import mytown.cmd.CmdEmote;
import mytown.cmd.CmdGamemode;
import mytown.cmd.CmdHome;
import mytown.cmd.CmdHomes;
import mytown.cmd.CmdMyTown;
import mytown.cmd.CmdOnline;
import mytown.cmd.CmdPrivateMsg;
import mytown.cmd.CmdReplyPrivateMsg;
import mytown.cmd.CmdSetHome;
import mytown.cmd.CmdSetSpawn;
import mytown.cmd.CmdSpawn;
import mytown.cmd.CmdTeleport;
import mytown.cmd.CmdWrk;
import mytown.cmd.api.MyTownCommand;
import mytown.entities.ItemIdRange;
import mytown.entities.TownSettingCollection;
import mytown.entities.TownSettingCollection.ISettingsSaveHandler;
import mytown.event.PlayerEvents;
import mytown.event.ProtectionEvents;
import mytown.event.TickHandler;
import mytown.event.WorldEvents;
import mytown.event.tick.WorldBorder;
import net.minecraft.command.ICommand;
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

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES)
@NetworkMod(clientSideRequired = false, serverSideRequired = true)
public class MyTown {
	public TownSettingCollection serverWildSettings = new TownSettingCollection(true, true);
	public TownSettingCollection serverSettings = new TownSettingCollection(true, false);
	public Map<Integer, TownSettingCollection> worldWildSettings = new HashMap<Integer, TownSettingCollection>();
	public LinkedList<ItemIdRange> carts = null;
	public LinkedList<ItemIdRange> leftClickAccessBlocks = null;
	public boolean isMCPC = false;
	public Method MCPCRegisterCommand = null;

	@Instance("MyTown")
	public static MyTown instance;
	public Config config = new Config();

	public List<MyTownCommand> commands = new ArrayList<MyTownCommand>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		isMCPC = MinecraftServer.getServer().getServerModName().contains("mcpc");
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
			Class.forName("net.minecraftforge.event.world.BlockEvent$BreakEvent");
			MinecraftForge.EVENT_BUS.register(Class.forName("mytown.event.ExtraEvents").newInstance()); // A
																										// sort
																										// of
																										// compat
																										// for
																										// older
																										// versions
																										// of
																										// Forge
																										// that
																										// don't
																										// contain
																										// all
																										// the
																										// events
			Log.info("ExtraEvents loaded");
		} catch (Exception e) {
			Log.info("Failed to load ExtraEvents");
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
		commands.add(new CmdAdmin());
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

		if (isMCPC) {
			try {
				MCPCRegisterCommand = net.minecraft.command.CommandHandler.class.getMethod("registerCommand", ICommand.class, String.class);
				Log.info("MCPC+ detected. Set up special command registration, just for it ;)");
			} catch (Exception e) {
				isMCPC = false; // Set isMCPC to false
			}
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
		if (sender instanceof MinecraftServer) {
			Log.info(msg);
			return;
		}
		sender.sendChatToPlayer(ChatMessageComponent.createFromText(msg));
	}
}
