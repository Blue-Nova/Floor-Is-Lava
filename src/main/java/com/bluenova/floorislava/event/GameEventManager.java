package com.bluenova.floorislava.event;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.config.MainConfig;
import com.bluenova.floorislava.config.PlayerDataManager;
import com.bluenova.floorislava.event.events.PlayerDropsRespawnAnchorEvent;
import com.bluenova.floorislava.event.events.PlayerDeathEvent;
import com.bluenova.floorislava.event.events.PlayerMovesEvent;
import com.bluenova.floorislava.event.events.PlayerJoinServerEvent;
import com.bluenova.floorislava.event.events.PlayerQuitServerEvent;
import com.bluenova.floorislava.event.events.PlayerSetsRespawnPointEvent;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyStates;
import com.bluenova.floorislava.util.messages.MiniMessages;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.messages.PluginLogger;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

import static org.bukkit.Bukkit.getServer;

public class GameEventManager implements Listener {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;
    private final PlayerDataManager playerDataManager;
    private final PluginLogger pluginLogger;

    private final ArrayList<Listener> eventsList = new ArrayList<>();

    public GameEventManager(InviteLobbyManager lobbyManager, GameLobbyManager gameManager, PlayerDataManager playerDataManager, PluginLogger pluginLogger) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
        this.playerDataManager = playerDataManager;
        this.pluginLogger = pluginLogger;

        eventsList.add(new PlayerMovesEvent(gameManager, pluginLogger));
        eventsList.add(new PlayerSetsRespawnPointEvent(gameManager, pluginLogger));
        eventsList.add(new PlayerDropsRespawnAnchorEvent(gameManager, pluginLogger));
        eventsList.add(new PlayerJoinServerEvent(playerDataManager, pluginLogger));
        eventsList.add(new PlayerDeathEvent(gameManager, pluginLogger));
        eventsList.add(new PlayerQuitServerEvent(gameManager, lobbyManager, pluginLogger));

        for (Listener event : eventsList) {
            pluginLogger.debug("Registering event: " + event.getClass().getSimpleName());
            getServer().getPluginManager().registerEvents(event, FloorIsLava.getInstance());
        }
    }



    public ArrayList<Listener> getEventsList() {
        return eventsList;
    }
}