package Events;

import Utils.Tools;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;


public class PlayerDieEvent implements Listener {
    @EventHandler
    public void deathEvent(EntityDamageEvent e){
        if(!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if((e.getDamage() >= player.getHealth()))
            if(Tools.checkPlayerInGame(player)){
                e.setCancelled(true);
                Tools.getGameFromPlayer(player).remove(player,true);
                player.setHealth(20);
            };
    }
}