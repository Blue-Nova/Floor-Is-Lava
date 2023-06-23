package Events;

import Utils.Tools;
import floorIsLava.FloorIsLava;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerJoinEvent implements Listener {

    @EventHandler
    public void playerJoin(org.bukkit.event.player.PlayerJoinEvent e){

        if (e.getPlayer().getWorld() == FloorIsLava.VOID_WORLD)
            if(e.getPlayer().getBedSpawnLocation() != null)
                e.getPlayer().teleport(e.getPlayer().getBedSpawnLocation());
        else e.getPlayer().teleport(Tools.getHighestUsableBlockAt(Bukkit.getWorlds().get(0),0,0).getLocation());
    }
}
