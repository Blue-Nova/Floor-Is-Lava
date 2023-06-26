package floorIsLava.gameobject;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;

public class GamePlotDivider {

    public int plotSize;
    ArrayList<GamePlot> plotList = new ArrayList<>();
    World world;
    int plotMargin;

    public GamePlotDivider(World world, int plotMargin, int plotSize, int plotAmountWidth) {
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
                new Location(world, mag_x, -64, mag_z),
                new Location(world, (mag_x) + plotSize, 319, (mag_z) + plotSize));

        plotList.add(plot);
    }

    public GamePlot getFirstEmptyPlot() {
        for (GamePlot gp : plotList) {
            if (!gp.inUse) {
                return gp;
            }
        }
        return null;
    }

}
