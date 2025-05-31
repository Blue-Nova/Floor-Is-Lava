package com.bluenova.floorislava.game.object;

import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;

public class GamePlotDivider {

    public int plotSize;
    ArrayList<GamePlot> plotList = new ArrayList<>();
    World world;
    int plotMargin;

    public GamePlotDivider(World world, int plotMargin, int plotSize, int plotAmountWidth, PluginLogger pluginLogger) {
        this.world = world;
        this.plotMargin = plotMargin;
        this.plotSize = plotSize;
        for (int plotZ = 0; plotZ < plotAmountWidth; plotZ++) {
            for (int plotX = 0; plotX < plotAmountWidth; plotX++) {
                createNewPlot(plotX, plotZ);
            }
        }
    }

    public void createNewPlot(int x, int z) {
        int mag_x = x * plotMargin;
        int mag_z = z * plotMargin;
        GamePlot plot = new GamePlot(world,
                new Location(world, mag_x, world.getMinHeight(), mag_z),
                new Location(world, (mag_x) + plotSize, world.getMaxHeight()-1, (mag_z) + plotSize));
        plotList.add(plot);
    }

    public GamePlot prepareFirstEmptyPlot() {
        for (GamePlot gp : plotList) {
            if (!gp.getInUse()) {
                gp.setInUse(true);
                return gp;
            }
        }
        return null;
    }

    public ArrayList<GamePlot> getPlotList() {
        return plotList;
    }
}
