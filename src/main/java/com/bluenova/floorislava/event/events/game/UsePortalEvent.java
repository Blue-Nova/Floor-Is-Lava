package com.bluenova.floorislava.event.events.game;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.util.messages.MiniMessages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class UsePortalEvent implements Listener {

    @EventHandler
    public void onUsePortal(PlayerPortalEvent event) {
        GameLobbyManager gameLobbyManager = FloorIsLava.getGameLobbyManager();
        Player player = event.getPlayer();
        if (gameLobbyManager.getGameFromPlayer(player) != null) {
            event.setCancelled(true);
            player.sendActionBar(MiniMessages.createComponent("<red>You cannot use portals while in a game!"));
        }
    }
}
