package com.bluenova.floorislava.event;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.config.MainConfig;
import com.bluenova.floorislava.config.PlayerDataManager;
import com.bluenova.floorislava.event.events.PlayerDropsRespawnAnchorItem;
import com.bluenova.floorislava.event.events.PlayerSetsRespawnPoint;
import com.bluenova.floorislava.event.events.onPlayerMove;
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

        eventsList.add(new onPlayerMove(gameManager, pluginLogger));
        eventsList.add(new PlayerSetsRespawnPoint(gameManager, pluginLogger));
        eventsList.add(new PlayerDropsRespawnAnchorItem(gameManager, pluginLogger));

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

        if (!(event.getDamage() >= player.getHealth())){
            return;
        }

        if (gameManager.isPlayerIngame(player)) {
            if (gameManager.getGameFromPlayer(player).getGameState() == GameLobbyStates.GENERATING) {
                pluginLogger.debug("Player " + player.getName() + " took fatal damage during GENERATING state. Ignoring.");
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
