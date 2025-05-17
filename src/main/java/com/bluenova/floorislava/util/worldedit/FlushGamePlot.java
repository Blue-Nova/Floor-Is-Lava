package com.bluenova.floorislava.util.worldedit;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.GamePlot;
import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.util.Workload;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class FlushGamePlot extends Workload {

    private final GamePlot gp;
    private final int y;
    private final boolean last;
    private final GameLobby gameLobby;

    public FlushGamePlot(GameLobby gameLobby, int y, boolean last) {
        this.gameLobby = gameLobby;
        this.y = y;
        this.last = last;
        // add getter
        this.gp = gameLobby.gamePlot;
    }

    @Override
    public void compute() {
        Bukkit.getScheduler().runTaskAsynchronously(FloorIsLava.getInstance(), () -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(FloorIsLava.getVoidWorld()))) {
                Region region = new CuboidRegion(BlockVector3.at(gp.plotStart.getX(), y, gp.plotStart.getZ()),
                        BlockVector3.at(gp.plotEnd.getX() - 1, y, gp.plotEnd.getZ() - 1));
                Pattern airPattern = BukkitAdapter.adapt(Material.AIR.createBlockData());
                RegionFunction airFunction = new BlockReplace(editSession, airPattern);
                RegionVisitor airVisitor = new RegionVisitor(region, airFunction);

                Operations.complete(airVisitor);
                if (last){
                    gameLobby.flushDone();
                }
            } catch (WorldEditException e) {
                throw new RuntimeException(e);
            }
        });

    }
}
