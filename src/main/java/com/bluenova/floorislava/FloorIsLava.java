package com.bluenova.floorislava;

import co.aikar.commands.PaperCommandManager;
import com.bluenova.floorislava.command.MainCommand;
import com.bluenova.floorislava.config.InternalMessageConfiguration;
import com.bluenova.floorislava.config.Message;
import com.bluenova.floorislava.event.GameEventManager;
import com.bluenova.floorislava.game.object.GamePlotDivider;
import com.bluenova.floorislava.util.WorkloadRunnable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.onarandombox.MultiverseCore.MultiverseCore;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public final class FloorIsLava extends JavaPlugin {

    private static FloorIsLava instance;
    private final ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
    private World voidWorld;
    private World normalWorld;
    private GamePlotDivider gamePlotDivider;
    private WorkloadRunnable workloadRunnable;
    private MultiverseCore multiverseCore;
    private PaperCommandManager paperCommandManager;
    private String prefix;
    @Getter
    public Gson gson;

    public static FloorIsLava getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        prefix = ChatColor.GRAY + "[" + ChatColor.WHITE + "F" + ChatColor.YELLOW + "I" + ChatColor.RED + "L" + ChatColor.GRAY + "] ";
        registerCommands();
        registerEvents();
        setupMVC();
        setupValues();
        handleConfig();
        this.workloadRunnable = new WorkloadRunnable();
        workloadRunnable.startWLR();
        this.gamePlotDivider = new GamePlotDivider(voidWorld, 1000, 50, 10);
    }

    @Override
    public void onDisable() {
    }

    private void registerCommands() {
        paperCommandManager = new PaperCommandManager(this);
        paperCommandManager.registerCommand(new MainCommand());
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

    private void setupValues() {
        gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Message.writeToArrays();
    }

    private void handleConfig() {
        InternalMessageConfiguration msgConfig = new InternalMessageConfiguration(new File(this.getDataFolder() + File.separator, "InternalMessageConfig.json"));
        msgConfig.reload();
    }
}
