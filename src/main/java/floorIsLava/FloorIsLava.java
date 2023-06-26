package floorIsLava;

import com.onarandombox.MultiverseCore.MultiverseCore;
import floorIsLava.command.LeaveLobbyCommand;
import floorIsLava.command.game.StartGameCommand;
import floorIsLava.command.invite.*;
import floorIsLava.event.GameEventManager;
import floorIsLava.gameobject.GamePlotDivider;
import floorIsLava.util.WorkloadRunnable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class FloorIsLava extends JavaPlugin {

    private static FloorIsLava instance;
    private final ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();
    private World voidWorld;
    private World normalWorld;
    private GamePlotDivider gamePlotDivider;
    private WorkloadRunnable workloadRunnable;

    public static FloorIsLava getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        registerCommands();
        registerEvents();
        setupMVC();
        this.workloadRunnable = new WorkloadRunnable();
        workloadRunnable.startWLR();
        this.gamePlotDivider = new GamePlotDivider(voidWorld, 1000, 50, 10);
    }

    @Override
    public void onDisable() {
    }

    private void registerCommands() {
        /*Lobby floorIsLava.Commands*/
        this.getCommand("fillobby").setExecutor(new StartLobbyCommand());
        this.getCommand("filinvite").setExecutor(new InvitePlayersCommand());
        this.getCommand("fillist").setExecutor(new ListPlayersCommand());
        this.getCommand("filaccept").setExecutor(new AcceptInviteCommand());
        this.getCommand("filleave").setExecutor(new LeaveLobbyCommand());
        this.getCommand("filremove").setExecutor(new RemovePlayerCommand());

        /*Game floorIsLava.Commands*/
        this.getCommand("filstart").setExecutor(new StartGameCommand());
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
