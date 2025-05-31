package com.bluenova.floorislava.util;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;

public class GenerateGameTerrain extends Workload {

    int x_copy_start;
    int x_copy_end;
    int z_copy_start;
    int z_copy_end;
    int x_paste;
    int z_paste;

    public boolean complete = false;

    public GenerateGameTerrain(int x_copy_start,int x_copy_end, int z_copy_start, int z_copy_end, int x_paste, int z_paste) {

        this.x_copy_start = x_copy_start;
        this.z_copy_start = z_copy_start;
        this.x_copy_end = x_copy_end;
        this.z_copy_end = z_copy_end;
        this.x_paste = x_paste;
        this.z_paste = z_paste;
    }

    @Override
    public void compute() {
        FloorIsLava.getInstance().getPluginLogger().debug("Generating game terrain at " + x_paste + ", " + z_paste);
        try {
            Clipboard clipboard = Tools.createClipboard(FloorIsLava.getNormalWorld(),
                    new CuboidRegion(
                            BlockVector3.at(x_copy_start, FloorIsLava.getVoidWorld().getMinHeight(), z_copy_start),
                            BlockVector3.at(x_copy_end, FloorIsLava.getVoidWorld().getMaxHeight() - 1, z_copy_end)));
          
            Tools.pasteClipboard(clipboard, new Location(FloorIsLava.getVoidWorld(), x_paste,
                                 FloorIsLava.getVoidWorld().getMinHeight(), z_paste));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            complete = true;
            FloorIsLava.getInstance().getPluginLogger().debug("Finished generating part of game terrain at " + x_paste + ", " + z_paste);
        }
    }

    public boolean isComplete() {
        return complete;
    }
}
