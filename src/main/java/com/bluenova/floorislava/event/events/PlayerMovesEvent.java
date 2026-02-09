package com.bluenova.floorislava.event.events;

import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyStates;
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovesEvent implements Listener {

    private final GameLobbyManager gameManager;
    private final PluginLogger pluginLogger;

    public PlayerMovesEvent(GameLobbyManager gameManager, PluginLogger pluginLogger) {
        this.gameManager = gameManager;
        this.pluginLogger = pluginLogger;
    }

    @EventHandler
    public void onPlayerMoves(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (gameManager.isPlayerIngame(player)) {
            GameLobby game = gameManager.getGameFromPlayer(player);
            if (game.getGameState() == GameLobbyStates.STARTING) {
                event.setCancelled(true);
            }
        }
    }
}