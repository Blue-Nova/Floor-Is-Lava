package com.bluenova.floorislava.util.worldguard.regionprofiles;

import com.bluenova.floorislava.util.worldguard.RegionProfile;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Map;
import java.util.Set;

public class BaseProfile extends RegionProfile {

    private final ProtectedRegion region;

    public BaseProfile(String name, String description) {
        super(name, description);
        region = initRegion();
        applyProfile();
    }

    @Override
    public void applyProfile() {
        Set<EntityType> blackListedEntities = Set.of(
                /*
                BukkitAdapter.adapt(org.bukkit.entity.EntityType.CREEPER),
                BukkitAdapter.adapt(org.bukkit.entity.EntityType.ZOMBIE),
                BukkitAdapter.adapt(org.bukkit.entity.EntityType.SKELETON),
                BukkitAdapter.adapt(org.bukkit.entity.EntityType.SPIDER)
                */
        );

        // MISC LIST
        region.setFlag(Flags.WEATHER_LOCK, WeatherType.REGISTRY.get("clear"));
        region.setFlag(Flags.DENY_SPAWN, blackListedEntities);

        // ALLOW LIST
        region.setFlag(Flags.PVP, StateFlag.State.ALLOW);
        region.setFlag(Flags.USE, StateFlag.State.ALLOW);
        region.setFlag(Flags.TNT, StateFlag.State.ALLOW);
        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
        region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.ALLOW);
        region.setFlag(Flags.INTERACT, StateFlag.State.ALLOW);
        region.setFlag(Flags.DAMAGE_ANIMALS, StateFlag.State.ALLOW);
        region.setFlag(Flags.MOB_DAMAGE, StateFlag.State.ALLOW);
        region.setFlag(Flags.ENDERPEARL, StateFlag.State.ALLOW);
        region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.ALLOW);

        // DENY LIST
        region.setFlag(Flags.SLEEP, StateFlag.State.DENY);
        region.setFlag(Flags.RESPAWN_ANCHORS, StateFlag.State.DENY);
        region.setFlag(Flags.CHORUS_TELEPORT, StateFlag.State.DENY);
    }

    public Map<Flag<?>, Object> getFlags() {
        return region.getFlags();
    }

    // Additional methods or properties can be added here if needed
}
