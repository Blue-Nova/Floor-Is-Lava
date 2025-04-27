package com.bluenova.floorislava.game.object;

import org.bukkit.Location;
import org.bukkit.World;

public class GamePlot {
    public Location plotStart;
    public Location plotEnd;
    public World plotWorld;
    public String worldGuardRegionId;

    private boolean inUse = false;
    private boolean hasBorders = false;

    public GamePlot(World world, Location start, Location end) {
        plotWorld = world;
        plotStart = start;
        plotEnd = end;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public boolean getInUse() {
        return inUse;
    }

    public void setHasBorders(boolean hasBorders) {
        this.hasBorders = hasBorders;
    }

    public boolean hasBorders() {
        return hasBorders;
    }
}
