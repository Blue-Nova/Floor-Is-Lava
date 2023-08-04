package com.bluenova.floorislava.config.json.general;

import lombok.Getter;

@Getter
public enum SettingGroup {
    GAME("Game"),
    SERVER("Server"),
    OTHER("Other");

    private final String configName;

    SettingGroup(String configName) {
        this.configName = configName;
    }

}
