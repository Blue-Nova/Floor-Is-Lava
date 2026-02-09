package com.bluenova.floorislava.event.events;

import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyStates;
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDeathEvent implements Listener {

    private final GameLobbyManager gameManager;
    private final PluginLogger pluginLogger;

    public PlayerDeathEvent(GameLobbyManager gameManager, PluginLogger pluginLogger) {
        this.gameManager = gameManager;
        this.pluginLogger = pluginLogger;
    }

    @EventHandler
    public void onPlayerTakesFatalDamageInGame(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Check if damage is actually fatal
        if (!(event.getDamage() >= player.getHealth())) {
            return;
        }

        if (gameManager.isPlayerIngame(player)) {
            GameLobby game = gameManager.getGameFromPlayer(player);
            if (game == null) { // Should ideally not happen if isPlayerIngame is true
                pluginLogger.warning("Player " + player.getName() + " is in game but GameLobby is null during EntityDamageEvent.");
                return;
            }

            if (game.getGameState() == GameLobbyStates.GENERATING) {
                pluginLogger.debug("Player " + player.getName() + " took fatal damage during GENERATING state. Ignoring.");
                event.setCancelled(true); // Still cancel to prevent vanilla death screen if possible
                return;
            }

            // Re-check fatal damage, as game state might have allowed for health changes
            if (!(event.getDamage() >= player.getHealth())) {
                return;
            }

            if ((event.getCause() == EntityDamageEvent.DamageCause.LAVA) &&
                (player.getLocation().getY() <= game.lavaHeight)) {
                game.remove(player, true, false);
            } else {
                game.playerDiedNoLava(player);
            }
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 1);
        }
    }
}