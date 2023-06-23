package floorIsLava;

import GameObjects.GamePlotDivider;
import Utils.WorkloadRunnable;
import com.onarandombox.MultiverseCore.MultiverseCore;
import Commands.GameCommands.StartGameCommand;
import Commands.InviteCommands.*;
import Commands.LeaveLobbyCommand;
import Events.PlayerDieEvent;
import Events.PlayerJoinEvent;
import Events.PlayerLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class FloorIsLava extends JavaPlugin {

    public static World VOID_WORLD;
    public static World NORMAL_WORLD;
    public static FloorIsLava plugin;
    public static GamePlotDivider GPD;
    public static WorkloadRunnable WLR;
    public static ConsoleCommandSender CONSOLE_SENDER = Bukkit.getConsoleSender();
    @Override
    public void onEnable() {

        this.plugin = this;

        // LOBBY COMMANDS
        this.getCommand("fillobby").setExecutor(new StartLobbyCommand());
        this.getCommand("filinvite").setExecutor(new InvitePlayersCommand());
        this.getCommand("fillist").setExecutor(new ListPlayersCommand());
        this.getCommand("filaccept").setExecutor(new AcceptInviteCommand());
        this.getCommand("filleave").setExecutor(new LeaveLobbyCommand());
        this.getCommand("filremove").setExecutor(new RemovePlayerCommand());

        // GAME COMMANDS
        this.getCommand("filstart").setExecutor(new StartGameCommand());

        // REGISTER EVENT LISTENERS
        getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), this);
        getServer().getPluginManager().registerEvents(new PlayerDieEvent(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinEvent(), this);

        // MULTIVERSE SETUP
        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if(!core.getMVWorldManager().isMVWorld("fil_normal_world"))
            core.getMVWorldManager().addWorld("fil_normal_world", World.Environment.NORMAL,"", WorldType.NORMAL,true,"");
        if(!core.getMVWorldManager().isMVWorld("fil_void_world"))
            core.getMVWorldManager().addWorld("fil_void_world", World.Environment.NORMAL,"", WorldType.NORMAL,true,"VoidGen");

        NORMAL_WORLD = Bukkit.getWorld("fil_normal_world");
        VOID_WORLD = Bukkit.getWorld("fil_void_world");

        this.WLR = new WorkloadRunnable();
        WLR.startWLR();
        this.GPD = new GamePlotDivider(VOID_WORLD,1000,50,10);



    }

    @Override
    public void onDisable() {
    }
}
