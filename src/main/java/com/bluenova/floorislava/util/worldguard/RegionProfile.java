package com.bluenova.floorislava.util.worldguard;

import com.bluenova.floorislava.FloorIsLava;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

import java.util.Map;

public abstract class RegionProfile {
    protected String name;
    protected String description;
    protected static ProtectedRegion region;

    public RegionProfile(String name, String description) {
        this.name = name;
        this.description = description;
        region = initRegion();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProtectedRegion initRegion() {
        // Initialize the region with default values
        BlockVector3 min = BukkitAdapter.asBlockVector(new Location(FloorIsLava.getVoidWorld(), 0.0, 0.0, 0.0));
        BlockVector3 max = BukkitAdapter.asBlockVector(new Location(FloorIsLava.getVoidWorld(), 0.0, 0.0, 0.0));
        ProtectedRegion region = new ProtectedCuboidRegion(getName(),min, max);
        region.setPriority(1); // Set a default priority
        return region;
    }

    public abstract void applyProfile();

    public Map<Flag<?>, Object> getFlags() {
        return region.getFlags();
    }
}
