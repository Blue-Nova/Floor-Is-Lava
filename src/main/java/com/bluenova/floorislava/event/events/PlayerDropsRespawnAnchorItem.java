package com.bluenova.floorislava.event.events;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.config.MainConfig;
import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerDropsRespawnAnchorItem implements Listener {

    private final GameLobbyManager gameManager;
    private final PluginLogger pluginLogger;

    public PlayerDropsRespawnAnchorItem(GameLobbyManager gameManager, PluginLogger pluginLogger) {
        this.gameManager = gameManager;
        this.pluginLogger = pluginLogger;
    }

    @EventHandler
    public void onPlayerDropsAnchor(PlayerDropItemEvent event) {
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
                    pluginLogger.debug("Respawn Anchor item vanished after drop by " + player.getName());
                }
            }
        }
    }
}