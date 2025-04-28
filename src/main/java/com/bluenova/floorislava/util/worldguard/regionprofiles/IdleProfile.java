package com.bluenova.floorislava.util.worldguard.regionprofiles;

import com.bluenova.floorislava.util.worldguard.RegionProfile;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Map;

public class IdleProfile extends RegionProfile {

    private final ProtectedRegion region;

    public IdleProfile(String name, String description) {
        super(name, description);
        region = initRegion();
        applyProfile();
    }

    @Override
    public void applyProfile() {
        // MISC LIST
        region.setFlag(Flags.WEATHER_LOCK, WeatherType.REGISTRY.get("clear"));

        // ALLOW LIST

        // specifically NOT spawning any entities and not allowing players to move
        region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);

        // DENY LIST
        region.setFlag(Flags.CHEST_ACCESS, StateFlag.State.DENY);
        region.setFlag(Flags.SLEEP, StateFlag.State.DENY);
        region.setFlag(Flags.RESPAWN_ANCHORS, StateFlag.State.DENY);
        region.setFlag(Flags.CHORUS_TELEPORT, StateFlag.State.DENY);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        region.setFlag(Flags.USE, StateFlag.State.DENY);
        region.setFlag(Flags.TNT, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        region.setFlag(Flags.INTERACT, StateFlag.State.DENY);
        region.setFlag(Flags.DAMAGE_ANIMALS, StateFlag.State.DENY);
        region.setFlag(Flags.MOB_DAMAGE, StateFlag.State.DENY);
        region.setFlag(Flags.ENDERPEARL, StateFlag.State.DENY);
    }

    public Map<Flag<?>, Object> getFlags() {
        return region.getFlags();
    }

    // Additional methods or properties can be added here if needed
}
