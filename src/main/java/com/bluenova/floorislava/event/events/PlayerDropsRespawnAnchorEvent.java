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

public class PlayerDropsRespawnAnchorEvent implements Listener {

    private final GameLobbyManager gameManager;
    private final PluginLogger pluginLogger;

    public PlayerDropsRespawnAnchorEvent(GameLobbyManager gameManager, PluginLogger pluginLogger) {
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
                
                if (gameLobby != null && !gameLobby.manualSpawnItemUsed.contains(player.getUniqueId())) {
                    event.getItemDrop().remove(); 
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.8f);
                    pluginLogger.debug("Respawn Anchor item vanished after drop by " + player.getName());
                }
            }
        }
    }
}