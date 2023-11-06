package com.bluenova.floorislava.util.tasks;

import com.bluenova.floorislava.FloorIsLava;
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
import org.bukkit.Material;

public class MakeBarrierWall implements Workload {

    int x_start;
    int z_start;
    int x_end;
    int z_end;
    int y_level;

    public MakeBarrierWall(int x_start, int z_start, int x_end, int z_end , int y_level) {
        this.x_start = x_start;
        this.z_start = z_start;
        this.x_end = x_end;
        this.z_end = z_end;
        this.y_level = y_level;
    }

    @Override
    public void compute() {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(FloorIsLava.getVoidWorld()))) {
            Region region = new CuboidRegion(BlockVector3.at(x_start, y_level, z_start),
                    BlockVector3.at(x_end, y_level, z_end));
            Pattern lavaPattern = BukkitAdapter.adapt(Material.BARRIER.createBlockData());
            RegionFunction lavaFunction = new BlockReplace(editSession, lavaPattern);
            RegionVisitor lavaVisitor = new RegionVisitor(region, lavaFunction);
            Operations.complete(lavaVisitor);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
    }
}
