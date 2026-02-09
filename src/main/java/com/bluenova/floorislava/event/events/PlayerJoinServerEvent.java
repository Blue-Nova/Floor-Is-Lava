package com.bluenova.floorislava.event.events;

import com.bluenova.floorislava.config.PlayerDataManager;
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinServerEvent implements Listener {

    private final PlayerDataManager playerDataManager;
    private final PluginLogger pluginLogger;

    public PlayerJoinServerEvent(PlayerDataManager playerDataManager, PluginLogger pluginLogger) {
        this.playerDataManager = playerDataManager;
        this.pluginLogger = pluginLogger;
    }

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        pluginLogger.debug("Player " + event.getPlayer().getName() + " joined, checking for data restoration.");
        playerDataManager.restoreStateIfNecessary(event.getPlayer());
    }
}