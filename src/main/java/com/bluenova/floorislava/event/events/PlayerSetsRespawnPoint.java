package com.bluenova.floorislava.event.events;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.config.MainConfig;
import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyStates;
import com.bluenova.floorislava.util.messages.MiniMessages;
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerSetsRespawnPoint implements Listener {

    private final GameLobbyManager gameManager;
    private final PluginLogger pluginLogger;

    public PlayerSetsRespawnPoint(GameLobbyManager gameManager, PluginLogger pluginLogger) {
        this.gameManager = gameManager;
        this.pluginLogger = pluginLogger;
    }

    @EventHandler
    public void onPlayerUsesRespawnAnchor(PlayerInteractEvent event) {
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
            event.setCancelled(true); // prevent default bed behavior

            GameLobby gameLobby = gameManager.getGameFromPlayer(player);
            if (gameLobby == null) {
                pluginLogger.warning("Player " + player.getName() + " interacted with Respawn Anchor but no GameLobby found.");
                return;
            }

            if (gameLobby.getGameState() != GameLobbyStates.STARTED) {
                 MiniMessages.send(player, "game.manual_spawn_item_interact_fail_state");
                 return;
            }

            if (gameLobby.manualSpawnItemUsed.contains(player.getUniqueId())) {
                MiniMessages.send(player, "game.manual_spawn_already_used");
                return;
            }

            Location potentialSpawnLocation = player.getLocation().clone();
            // Check if the chosen location is currently safe (e.g. above lava)
            if (!gameLobby.isLocationSafeForRespawn(potentialSpawnLocation) || potentialSpawnLocation.getBlockY() <= gameLobby.lavaHeight) {
                MiniMessages.send(player, "game.manual_spawn_target_unsafe");
                player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f);
                return;
            }

            gameLobby.manualSpawnPoints.put(player.getUniqueId(), potentialSpawnLocation);
            gameLobby.manualSpawnItemUsed.add(player.getUniqueId());

            // Consume the item
            itemInHand.setAmount(itemInHand.getAmount() - 1);

            MiniMessages.send(player, "game.manual_spawn_set");
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
        }
    }
}