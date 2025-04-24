package com.bluenova.floorislava.event;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.Tools;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEventManager implements Listener {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;

    public GameEventManager(InviteLobbyManager lobbyManager, GameLobbyManager gameManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (event.getPlayer().getWorld() == FloorIsLava.getVoidWorld())
            if (event.getPlayer().getBedSpawnLocation() != null)
                event.getPlayer().teleport(event.getPlayer().getBedSpawnLocation());
            else
                event.getPlayer().teleport(Tools.getHighestUsableBlockAt(Bukkit.getWorlds().get(0), 0, 0).getLocation());
    }

    @EventHandler
    public void onPlayerDeathEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (event.getDamage() >= player.getHealth() && gameManager.isPlayerIngame(player)) {
            if((event.getCause() == EntityDamageEvent.DamageCause.LAVA) && (player.getLocation().getY()
                    <= gameManager.getGameFromPlayer(player).lavaHeight))
                gameManager.getGameFromPlayer(player).remove(player, true);
            else {
                gameManager.getGameFromPlayer(player).playerDiedNoLava(player);
            }
            event.setCancelled(true);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setExp(0);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK,1,1);
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (gameManager.isPlayerIngame(player)) gameManager.getGameFromPlayer(player).remove(player, false);
        if (lobbyManager.isLobbyOwner(player)) lobbyManager.getLobbyFromOwner(player).removePlayer(player);
        if (lobbyManager.isPlayerInLobby(player)) lobbyManager.getLobbyFromPlayer(player).removePlayer(player);
    }
}
