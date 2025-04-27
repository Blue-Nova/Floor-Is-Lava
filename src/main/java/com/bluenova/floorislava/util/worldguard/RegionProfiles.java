package com.bluenova.floorislava.util.worldguard;

import com.sk89q.worldguard.protection.flags.Flag;

import java.util.Map;

public enum RegionProfiles {
    BASE("base", "Base profile for the region"),
    IDLE("idle", "Idle profile for the region"),
    LAVA_SLOW("LavaSlow", "Lava slow profile for the region"),
    LAVA_FAST("LavaFast", "Lava fast profile for the region"),
    LAVA_VERY_FAST("LavaVeryFast", "Lava very fast profile for the region");

    private final String name;
    private final String description;

    // Scalable way to add flags to each profile from one place
    //private final Map<Flag<?>, Object> flags;

    RegionProfiles(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
