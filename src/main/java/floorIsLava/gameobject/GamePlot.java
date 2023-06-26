package floorIsLava.gameobject;

import org.bukkit.Location;
import org.bukkit.World;

public class GamePlot {

    public Location plotStart;
    public Location plotEnd;
    public World plotWorld;

    boolean inUse = false;
    boolean hasBordeers = false;

    public GamePlot(World world, Location start, Location end) {
        plotWorld = world;
        plotStart = start;
        plotEnd = end;
    }


}
