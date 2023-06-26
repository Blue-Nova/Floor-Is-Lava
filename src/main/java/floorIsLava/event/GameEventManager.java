package floorIsLava.event;

import floorIsLava.FloorIsLava;
import floorIsLava.util.Tools;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEventManager implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (event.getPlayer().getWorld() == FloorIsLava.getInstance().getVoidWorld())
            if (event.getPlayer().getBedSpawnLocation() != null)
                event.getPlayer().teleport(event.getPlayer().getBedSpawnLocation());
            else
                event.getPlayer().teleport(Tools.getHighestUsableBlockAt(Bukkit.getWorlds().get(0), 0, 0).getLocation());
    }

    @EventHandler
    public void onPlayerDeathEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if ((event.getDamage() >= player.getHealth()))
            if (Tools.checkPlayerInGame(player)) {
                event.setCancelled(true);
                Tools.getGameFromPlayer(player).remove(player, true);
                player.setHealth(20);
            }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (Tools.checkPlayerInGame(player)) Tools.getGameFromPlayer(player).remove(player, false);
        if (Tools.checkOwnerInLobby(player)) Tools.getLobbyFromOwner(player).removePlayer(player);
        if (Tools.checkPlayerInLobby(player)) Tools.getLobbyFromPlayer(player).removePlayer(player);
    }
}