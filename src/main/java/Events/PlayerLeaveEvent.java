package Events;

import Utils.Tools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveEvent implements Listener {

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        Player player = e.getPlayer();
        if(Tools.checkPlayerInGame(player)) Tools.getGameFromPlayer(player).remove(player,false);
        if(Tools.checkOwnerInLobby(player)) Tools.getLobbyFromOwner(player).removePlayer(player);
        if(Tools.checkPlayerInLobby(player)) Tools.getLobbyFromPlayer(player).removePlayer(player);
    }
}
