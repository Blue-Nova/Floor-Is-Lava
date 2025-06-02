package com.bluenova.floorislava.event;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.config.MainConfig;
import com.bluenova.floorislava.config.PlayerDataManager;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

import static org.bukkit.Bukkit.getServer;

public class GameEventManager implements Listener {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;
    private final PlayerDataManager playerDataManager;

    private final ArrayList<Listener> eventsList = new ArrayList<>();

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
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!gameManager.isPlayerIngame(player) || !MainConfig.getInstance().isManualSpawnEnabled()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType().isAir() || !itemInHand.hasItemMeta()) {
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();
        if (meta.getPersistentDataContainer().has(FloorIsLava.RESPAWN_ANCHOR_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true); // Prevent default bed behavior if it's a bed item

            GameLobby gameLobby = gameManager.getGameFromPlayer(player);
            if (gameLobby == null) return; 

            if (gameLobby.getGameState() != GameLobbyStates.STARTED) {
                 MiniMessages.send(player, "game.manual_spawn_item_interact_fail_state");
                 return;
            }

            if (gameLobby.manualSpawnItemUsed.contains(player.getUniqueId())) {
                MiniMessages.send(player, "game.manual_spawn_already_used");
                return;
            }

            Location potentialSpawnLocation = player.getLocation().clone();
            // Check if the chosen location is currently safe (above lava AND generally spawnable)
            // We use a slightly stricter check here than just lava height because player is actively choosing.
            if (!gameLobby.isLocationSafeForRespawn(potentialSpawnLocation) || potentialSpawnLocation.getBlockY() <= gameLobby.lavaHeight) {
                MiniMessages.send(player, "game.manual_spawn_target_unsafe");
                player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f); // Or another failure sound
                return;
            }

            gameLobby.manualSpawnPoints.put(player.getUniqueId(), potentialSpawnLocation);
            gameLobby.manualSpawnItemUsed.add(player.getUniqueId());

            // Consume the item
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            // No need to setItemInMainHand to null, Bukkit handles if amount is 0.

            MiniMessages.send(player, "game.manual_spawn_set");
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
        }
    }

    @EventHandler
    public void onPlayerDropRespawnAnchor(PlayerDropItemEvent event) {
        if (!MainConfig.getInstance().isManualSpawnEnabled()) return;

        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (droppedItem.hasItemMeta()) {
            ItemMeta meta = droppedItem.getItemMeta();
            if (meta.getPersistentDataContainer().has(FloorIsLava.RESPAWN_ANCHOR_KEY, PersistentDataType.BYTE)) {
                Player player = event.getPlayer();
                GameLobby gameLobby = gameManager.getGameFromPlayer(player);
                
                // Only make it vanish if player is in game and hasn't used their one item yet
                if (gameLobby != null && !gameLobby.manualSpawnItemUsed.contains(player.getUniqueId())) {
                    event.getItemDrop().remove(); // Make the item entity vanish
                    // Optionally send a message: MiniMessages.send(player, "game.manual_spawn_item_dropped_vanished");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.8f);
                }
            }
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
