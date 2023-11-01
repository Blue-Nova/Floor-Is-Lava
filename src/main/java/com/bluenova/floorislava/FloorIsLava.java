package com.bluenova.floorislava;

import com.bluenova.floorislava.command.MainCommand;
import com.bluenova.floorislava.command.TabCompletion;
import com.bluenova.floorislava.config.MessageConfig;
import com.bluenova.floorislava.event.GameEventManager;
import com.bluenova.floorislava.game.object.GamePlotDivider;
import com.bluenova.floorislava.config.MainConfig;
import com.bluenova.floorislava.util.WorkloadRunnable;
import com.onarandombox.MultiverseCore.MultiverseCore;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class FloorIsLava extends JavaPlugin {

    @Getter
    private static FloorIsLava instance;
    private final ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
    private World voidWorld;
    private World normalWorld;
    private GamePlotDivider gamePlotDivider;
    private WorkloadRunnable workloadRunnable;

    @Override
    public void onEnable() {
        instance = this;
        MainConfig mainConfig = MainConfig.getInstance();
        MessageConfig mssgConfig = MessageConfig.getInstance();
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
        getCommand("fil").setExecutor(new MainCommand());
        getCommand("fil").setTabCompleter(new TabCompletion());
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new GameEventManager(), this);
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
