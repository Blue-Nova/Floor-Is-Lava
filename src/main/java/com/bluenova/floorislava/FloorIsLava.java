package com.bluenova.floorislava;

import com.bluenova.floorislava.command.FILCommandHandler;
import com.bluenova.floorislava.config.MessageConfig;
import com.bluenova.floorislava.event.GameEventManager;
import com.bluenova.floorislava.game.object.GamePlotDivider;
import com.bluenova.floorislava.config.MainConfig;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.WorkloadRunnable;
import com.onarandombox.MultiverseCore.MultiverseCore;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


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

    @Override
    public void onEnable() {
        instance = this;
        MainConfig mainConfig = MainConfig.getInstance();
        MessageConfig mssgConfig = MessageConfig.getInstance();

        this.inviteLobbyManager = new InviteLobbyManager();
        this.gameLobbyManager = new GameLobbyManager();
        this.FILCommandHandler = new FILCommandHandler(inviteLobbyManager, gameLobbyManager);

        mainConfig.load();
        mssgConfig.load();
        registerCommands();
        registerEvents();
        setupMVC();
        this.workloadRunnable = new WorkloadRunnable();
        workloadRunnable.startWLR();

        this.gamePlotDivider = new GamePlotDivider(voidWorld, mainConfig.getPlotMargin(), mainConfig.getPlotSize(), mainConfig.getPlotAmount());
    }

    @Override
    public void onDisable() {
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
        if (!core.getMVWorldManager().isMVWorld("fil_normal_world"))
            core.getMVWorldManager().addWorld("fil_normal_world", World.Environment.NORMAL, "", WorldType.NORMAL, true, "");
        if (!core.getMVWorldManager().isMVWorld("fil_void_world"))
            core.getMVWorldManager().addWorld("fil_void_world", World.Environment.NORMAL, "", WorldType.NORMAL, true, "VoidGen");
        normalWorld = Bukkit.getWorld("fil_normal_world");
        voidWorld = Bukkit.getWorld("fil_void_world");
    }
}
