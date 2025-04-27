package com.bluenova.floorislava.util.worldguard;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.GamePlot;
import com.bluenova.floorislava.util.messages.PluginLogger;
import com.bluenova.floorislava.util.worldguard.regionprofiles.BaseProfile;
import com.bluenova.floorislava.util.worldguard.regionprofiles.IdleProfile;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.math.BlockVector3;

import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class FILRegionManager {

    private final Map<String,ProtectedRegion> allRegionsList = new HashMap<>();
    private final FloorIsLava plugin;
    private final World voidWorld; // Use helper to get void world
    private Map<String,RegionProfile> regionProfiles = new HashMap<>();

    private final PluginLogger pluginLogger;


    public FILRegionManager(FloorIsLava plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.voidWorld = FloorIsLava.getVoidWorld(); // Use helper to get void world
        this.pluginLogger = pluginLogger;

        for (RegionProfiles profile : RegionProfiles.values()) {
            switch (profile) {
                case BASE:
                    regionProfiles.put(profile.getName(), new BaseProfile(profile.getName(), profile.getDescription()));
                    break;
                case IDLE:
                    regionProfiles.put(profile.getName(), new IdleProfile(profile.getName(), profile.getDescription()));
                    break;
                // Add other profiles here as needed
                default:
                    break;
            }
        }
    }

    public void initializeWorldGuardRegions() {

        RegionManager regionManager = plugin.getVoidWorldRegionManager(); // Use helper to get manager
        if (regionManager == null) {
            pluginLogger.severe("Cannot initialize WorldGuard regions: RegionManager for void world is null.");
            return;
        }

        // delete all regions from FloorIsLava
        // to do that get all regions from the region manager
        // and check for the region id "fil_plot_"
        // if it starts with "fil_plot_" then remove it
        for (String regionIdToRemove : regionManager.getRegions().keySet()) {
            if (regionIdToRemove.startsWith("fil_plot_")) {
                regionManager.removeRegion(regionIdToRemove);
            }
        }

        int count = 0;
        for (GamePlot plot : FloorIsLava.getGamePlotDivider().getPlotList()) { // Assumes plotList accessible
            String regionId = "fil_plot_" + plot.plotStart.getBlockX() + "_" + plot.plotStart.getBlockZ();
            plot.worldGuardRegionId = regionId;

            // Define WG region vectors (Min Y and Max Y of the plot/world)
            BlockVector3 min = BukkitAdapter.asBlockVector(plot.plotStart).withY(voidWorld.getMaxHeight());
            // If plotEnd IS inclusive, use: .withY(voidWorld.getMaxHeight() - 1); without the .add(-1,0,-1)
            BlockVector3 max = BukkitAdapter.asBlockVector(plot.plotEnd).withY(voidWorld.getMinHeight()+1);

            // Create a new region
            ProtectedRegion region = new ProtectedCuboidRegion(regionId, min, max);
            // FIRST register region in WorldGuard's RegionManager
            regionManager.addRegion(region);
            // SECOND register region in FloorIsLava's RegionManager
            allRegionsList.put(regionId, region);

            // then you can set the flags for the region to avoid region not found errors
            setRegionProfile(regionId, RegionProfiles.IDLE); // Set the idle profile
            region.setPriority(1); // Set a default priority


            count++;
        }
        pluginLogger.debug("Initialized " + count + " WorldGuard regions for FloorIsLava.");
        log_all_regions();
    }

    // dev tool
    private void log_all_regions() {
        RegionManager regionManager = plugin.getVoidWorldRegionManager(); // Use helper to get manager
        if (regionManager == null) {
            pluginLogger.severe("Cannot log WorldGuard regions: RegionManager for void world is null.");
            return;
        }
        pluginLogger.debug("Logging all WorldGuard regions in void world...");
        for (ProtectedRegion region : regionManager.getRegions().values()) {
            pluginLogger.debug("Region ID: " + region.getId());
            pluginLogger.debug("Region Type: " + region.getClass().getSimpleName());
            pluginLogger.debug("Region Min: " + region.getMinimumPoint());
            pluginLogger.debug("Region Max: " + region.getMaximumPoint());
            pluginLogger.debug("Region Flags: " + region.getFlags());
        }
    }

    public Map<String, ProtectedRegion> getAllRegionsList() {
        return allRegionsList;
    }

    public ProtectedRegion getRegionById(String id) {
        return allRegionsList.get(id);
    }

    public void setRegionProfile(String worldGuardRegionId, RegionProfiles profile) {
        pluginLogger.debug("Setting region profile for " + worldGuardRegionId + " to " + profile.getName());
        RegionManager regionManager = plugin.getVoidWorldRegionManager(); // Use helper to get manager
        if (regionManager == null) {
            pluginLogger.severe("Cannot set WorldGuard region profile: RegionManager for void world is null.");
            return;
        }
        ProtectedRegion region = regionManager.getRegion(worldGuardRegionId);
        if (region != null) {
            region.setFlags(regionProfiles.get(profile.getName()).getFlags());
            pluginLogger.debug("Set region profile for " + worldGuardRegionId + " to " + profile.getName());
        } else {
            plugin.getLogger().warning("Region " + worldGuardRegionId + " not found.");
        }
    }
}
