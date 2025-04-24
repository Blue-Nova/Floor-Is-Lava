package com.bluenova.floorislava;

import com.bluenova.floorislava.command.FILCommandHandler;
import com.bluenova.floorislava.config.MessageConfig;
import com.bluenova.floorislava.event.GameEventManager;
import com.bluenova.floorislava.game.object.GamePlotDivider;
import com.bluenova.floorislava.config.MainConfig;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.WorkloadRunnable;
import com.bluenova.floorislava.util.messages.MiniMessages;
import com.onarandombox.MultiverseCore.MultiverseCore;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
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

    // Manager Instances for the Game Lobby and Invite Lobby
    private InviteLobbyManager inviteLobbyManager;
    private GameLobbyManager gameLobbyManager;
    private FILCommandHandler FILCommandHandler;

    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        instance = this;
        MainConfig mainConfig = MainConfig.getInstance();
        MessageConfig mssgConfig = MessageConfig.getInstance();

        this.adventure = BukkitAudiences.create(this);
        MiniMessages.init(this);

        this.inviteLobbyManager = new InviteLobbyManager();
        this.gameLobbyManager = new GameLobbyManager();
        this.FILCommandHandler = new FILCommandHandler(inviteLobbyManager, gameLobbyManager);

        mainConfig.load();
        mssgConfig.load();
        registerCommands();
        registerEvents();
        setupMVC();
        workloadRunnable = new WorkloadRunnable();
        workloadRunnable.startWLR();

        gamePlotDivider = new GamePlotDivider(voidWorld, mainConfig.getPlotMargin(), mainConfig.getPlotSize(), mainConfig.getPlotAmount());
    }

    @Override
    public void onDisable() {
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        getLogger().info("FloorIsLava Disabled!");
    }

    private void registerCommands() {
        getCommand("fil").setExecutor(FILCommandHandler);
        getCommand("fil").setTabCompleter(FILCommandHandler);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new GameEventManager(inviteLobbyManager, gameLobbyManager), this);
    }

    private void setupMVC() {
        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        assert core != null;
        if (!core.getMVWorldManager().isMVWorld("fil_normal_world"))
            core.getMVWorldManager().addWorld("fil_normal_world", World.Environment.NORMAL, "", WorldType.NORMAL, true, "");
        if (!core.getMVWorldManager().isMVWorld("fil_void_world"))
            core.getMVWorldManager().addWorld("fil_void_world", World.Environment.NORMAL, "", WorldType.NORMAL, true, "VoidGen");
        normalWorld = Bukkit.getWorld("fil_normal_world");
        voidWorld = Bukkit.getWorld("fil_void_world");
    }

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

}
