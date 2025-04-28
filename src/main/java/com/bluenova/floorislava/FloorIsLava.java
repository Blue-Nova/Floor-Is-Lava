package com.bluenova.floorislava;

import com.bluenova.floorislava.command.FILCommandHandler;
import com.bluenova.floorislava.config.MessageConfig;
import com.bluenova.floorislava.config.PlayerDataManager;
import com.bluenova.floorislava.event.GameEventManager;
import com.bluenova.floorislava.game.object.GamePlotDivider;
import com.bluenova.floorislava.config.MainConfig;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.WorkloadRunnable;
import com.bluenova.floorislava.util.messages.MiniMessages;
import com.bluenova.floorislava.util.messages.PluginLogger;
import com.bluenova.floorislava.util.worldguard.FILRegionManager;
import com.onarandombox.MultiverseCore.MultiverseCore;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;


public final class FloorIsLava extends JavaPlugin {

    private static World voidWorld;
    private static World normalWorld;
    private static GamePlotDivider gamePlotDivider;
    private static WorkloadRunnable workloadRunnable;
    private static FloorIsLava instance;

    // Logger for the plugin
    private PluginLogger pluginLogger;

    // Player Data Manager
    private PlayerDataManager playerDataManager;

    // --- WorldGuard Integration Fields ---
    private boolean worldGuardAvailable = false;
    private WorldGuard worldGuardAPI = null;
    private RegionContainer regionContainer = null;
    private static FILRegionManager worldGuardRegionManager = null;
    // ---

    // Manager Instances for the Game Lobby and Invite Lobby
    private InviteLobbyManager inviteLobbyManager;
    private GameLobbyManager gameLobbyManager;
    private FILCommandHandler FILCommandHandler;

    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        instance = this;

        // Load the MainConfig
        MainConfig mainConfig = MainConfig.getInstance();
        mainConfig.load();
        boolean devMode = MainConfig.getInstance().isDevModeEnabled();
        pluginLogger = new PluginLogger(this, devMode);
        if (devMode) {
            pluginLogger.info("Developer Mode Enabled - Debug messages will be shown.");
        }

        // Load the MessageConfig
        MessageConfig mssgConfig = new MessageConfig(pluginLogger); // instantiate MessageConfig will load it as well
        this.adventure = BukkitAudiences.create(this);
        MiniMessages.init(this, pluginLogger, mssgConfig);


        this.playerDataManager = new PlayerDataManager(pluginLogger, this);
        this.inviteLobbyManager = new InviteLobbyManager(pluginLogger);
        this.gameLobbyManager = new GameLobbyManager(pluginLogger, playerDataManager);
        this.FILCommandHandler = new FILCommandHandler(inviteLobbyManager, gameLobbyManager);

        registerCommands();
        registerEvents();
        setupMVC();
        // start gamePlotDivider first
        gamePlotDivider = new GamePlotDivider(voidWorld, mainConfig.getPlotMargin(), mainConfig.getPlotSize(), mainConfig.getPlotAmount(), pluginLogger);
        setupWorldGuard();
        if (isWorldGuardAvailable()){
            worldGuardRegionManager = new FILRegionManager(this, pluginLogger);
            worldGuardRegionManager.initializeWorldGuardRegions();
        }
        workloadRunnable = new WorkloadRunnable(pluginLogger);
        workloadRunnable.startWLR();
    }

    @Override
    public void onDisable() {
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        // Clean up games, lobbies, tasks etc.
        if (gameLobbyManager != null) {
            gameLobbyManager.shutdownAllGames(); // Need method in manager to force end games (WIP)
        }
        // Stop workload runnable? (May not be necessary if tasks are self-cancelling)
        pluginLogger.info(getName() + " Disabled.");
    }

    private void registerCommands() {
        getCommand("fil").setExecutor(FILCommandHandler);
        getCommand("fil").setTabCompleter(FILCommandHandler);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new GameEventManager(inviteLobbyManager, gameLobbyManager, playerDataManager,pluginLogger), this);
    }

    // Multiverse setup
    private void setupMVC() {
        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        assert core != null;
        if (!core.getMVWorldManager().isMVWorld("fil_normal_world"))
            core.getMVWorldManager().addWorld("fil_normal_world", World.Environment.NORMAL, "", WorldType.NORMAL, true, "");
        if (!core.getMVWorldManager().isMVWorld("fil_void_world"))
            core.getMVWorldManager().addWorld("fil_void_world", World.Environment.NORMAL, "", WorldType.NORMAL, true, "VoidGen");
        normalWorld = Bukkit.getWorld("fil_normal_world");
        voidWorld = Bukkit.getWorld("fil_void_world");
        voidWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        voidWorld.setTime(1000);
    }

    // WorldGuard setup
    private void setupWorldGuard() {
        // --- WorldGuard Detection ---
        Plugin wgPlugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if (wgPlugin != null && wgPlugin.isEnabled()) {
            try {
                this.worldGuardAPI = WorldGuard.getInstance();
                this.regionContainer = this.worldGuardAPI.getPlatform().getRegionContainer();
                this.worldGuardAvailable = true;
                pluginLogger.info("WorldGuard found and hooked! Enabling region flag support.");
            } catch (Exception e) {
                pluginLogger.severe("Error hooking into WorldGuard! Region features disabled.");
                pluginLogger.warning("Check if the correct version of WorldGuard is installed. (according to your server version)");
                this.worldGuardAvailable = false;
                this.worldGuardAPI = null;
                this.regionContainer = null;
            }
        } else {
            this.worldGuardAvailable = false;
            getLogger().warning("WorldGuard not found or disabled. Using Bukkit fallbacks for control.");
        }
        // --- WorldGuard Check Done ---
    }

    // Helper method to initialize WG regions on startup

    public RegionManager getVoidWorldRegionManager() {
        if (this.worldGuardAvailable && this.regionContainer != null) {
            RegionManager regionManager = this.regionContainer.get(BukkitAdapter.adapt(getVoidWorld()));
            if (regionManager == null) {
                getLogger().warning("RegionManager for void world is null. WorldGuard may not be set up correctly.");
            }
            return regionManager;
        }
        return null;
    }

    public boolean isWorldGuardAvailable() {
        return worldGuardAvailable;
    }
    // WorldGuard setup end

    // GETTERS
    public @NonNull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static World getVoidWorld() {
        return voidWorld;
    }

    public static World getNormalWorld(){
        return normalWorld;
    }

    public static WorkloadRunnable getWorkloadRunnable(){
        return workloadRunnable;
    }

    public static GamePlotDivider getGamePlotDivider(){
        return gamePlotDivider;
    }

    public static InviteLobbyManager getInviteLobbyManager() {
        return instance.inviteLobbyManager;
    }

    public static GameLobbyManager getGameLobbyManager() {
        return instance.gameLobbyManager;
    }

    public static FILRegionManager getFILRegionManager() {
        return worldGuardRegionManager;
    }
}
