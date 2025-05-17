package com.bluenova.floorislava.util;

import com.bluenova.floorislava.FloorIsLava;
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
import org.bukkit.World;

public class MakeBarrierWall extends Workload {

    private BlockVector3 start;
    private BlockVector3 end;
    private World world;

    public MakeBarrierWall(World world, BlockVector3 start, BlockVector3 end) {
        this.start = start;
        this.end = end;
        this.world = world;
    }

    @Override
    public void compute() {
        Bukkit.getScheduler().runTaskAsynchronously(FloorIsLava.getInstance(), () -> {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                Region region = new CuboidRegion(start, end);
                Pattern BarrierPattern = BukkitAdapter.adapt(Material.BARRIER.createBlockData());
                RegionFunction lavaFunction = new BlockReplace(editSession, BarrierPattern);
                RegionVisitor regionVisitor = new RegionVisitor(region, lavaFunction);
                Operations.complete(regionVisitor);
            } catch (WorldEditException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
