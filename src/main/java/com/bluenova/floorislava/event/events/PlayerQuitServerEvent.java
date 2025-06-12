package com.bluenova.floorislava.event.events;

import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitServerEvent implements Listener {

    private final GameLobbyManager gameManager;
    private final InviteLobbyManager lobbyManager;
    private final PluginLogger pluginLogger;

    public PlayerQuitServerEvent(GameLobbyManager gameManager, InviteLobbyManager lobbyManager, PluginLogger pluginLogger) {
        this.gameManager = gameManager;
        this.lobbyManager = lobbyManager;
        this.pluginLogger = pluginLogger;
    }

    @EventHandler
    public void onPlayerQuitServer(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        pluginLogger.debug("Player " + player.getName() + " quit. Checking game/lobby status.");

        if (gameManager.isPlayerIngame(player)) {
            GameLobby game = gameManager.getGameFromPlayer(player);
            if (game != null) {
                game.remove(player, false, true);
            } else {
                pluginLogger.warning("Player " + player.getName() + " was in game but GameLobby is null during PlayerQuitEvent.");
            }
        }
        if (lobbyManager.isPlayerInLobby(player)) {
            InviteLobby lobby = lobbyManager.getLobbyFromPlayer(player);
            if (lobby != null) {
                lobby.removePlayer(player);
            } else {
                pluginLogger.warning("Player " + player.getName() + " was in lobby but InviteLobby is null during PlayerQuitEvent.");
            }
        }
    }
}