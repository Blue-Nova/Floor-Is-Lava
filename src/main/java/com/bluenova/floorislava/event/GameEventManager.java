package com.bluenova.floorislava.event;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.config.PlayerDataManager;
import com.bluenova.floorislava.event.events.onPlayerMove;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyStates;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

import static org.bukkit.Bukkit.getServer;

public class GameEventManager implements Listener {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;
    private final PlayerDataManager playerDataManager;

    private ArrayList<Listener> eventsList = new ArrayList<>();

    public GameEventManager(InviteLobbyManager lobbyManager, GameLobbyManager gameManager, PlayerDataManager playerDataManager, PluginLogger pluginLogger) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
        this.playerDataManager = playerDataManager;

        eventsList.add(new onPlayerMove(gameManager, pluginLogger));

        for (Listener event : eventsList) {
            pluginLogger.debug("Registering event: " + event.getClass().getSimpleName());
            getServer().getPluginManager().registerEvents(event, FloorIsLava.getInstance());
        }

    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        playerDataManager.restoreStateIfNecessary(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeathEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (gameManager.isPlayerIngame(player)) {
            if (gameManager.getGameFromPlayer(player).getGameState() == GameLobbyStates.GENERATING) {
                return;
            }
            if (!(event.getDamage() >= player.getHealth())){
                return;
            }
            if((event.getCause() == EntityDamageEvent.DamageCause.LAVA) && (player.getLocation().getY()
                    <= gameManager.getGameFromPlayer(player).lavaHeight)){
                gameManager.getGameFromPlayer(player).remove(player, true, false);
            }
            else {
                gameManager.getGameFromPlayer(player).playerDiedNoLava(player);
            }
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK,1,1);
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (gameManager.isPlayerIngame(player)) gameManager.getGameFromPlayer(player).remove(player, false, true);
        if (lobbyManager.isPlayerInLobby(player)) lobbyManager.getLobbyFromPlayer(player).removePlayer(player);
    }

    public ArrayList<Listener> getEventsList() {
        return eventsList;
    }
}
