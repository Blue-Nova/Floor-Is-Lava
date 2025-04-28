package com.bluenova.floorislava.event.events;

import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyStates;
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class onPlayerMove implements org.bukkit.event.Listener {

    private final GameLobbyManager gameManager;
    private final PluginLogger pluginLogger;

    public onPlayerMove(GameLobbyManager gameManager,
                        PluginLogger pluginLogger) {
        this.gameManager = gameManager;
        this.pluginLogger = pluginLogger;
    }

    @EventHandler
    public void PlayerMoveEvent(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (gameManager.isPlayerIngame(player)) {
            GameLobby game = gameManager.getGameFromPlayer(player);
            if (game.getGameState() == GameLobbyStates.STARTING) {
                event.setCancelled(true);
            }
        }
    }
}
