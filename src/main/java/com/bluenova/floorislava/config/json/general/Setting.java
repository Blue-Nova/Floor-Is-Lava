package com.bluenova.floorislava.config.json.general;

import lombok.Getter;

import java.util.ArrayList;

public enum Setting {

    //Warning: for the Variable 'configName' please do NOT use ANY spaces

    PLOT_MARGIN(0, "Plot_Margin", SettingGroup.GAME, 1000),
    PLOT_SIZE(1, "Plot_Size", SettingGroup.GAME, 50),
    PLOT_AMOUNT(2, "Plot_Amount", SettingGroup.GAME, 5),

    MAX_MILLIS_PER_TICK(3, "Max_Millis_Per_Tick", SettingGroup.SERVER, 10);

    private static ArrayList<Setting> serverSettings, gameSettings, otherSettings;
    private static Setting instance;
    @Getter
    private final int backUP;
    private final int id;
    private final SettingGroup messageGroup;
    @Getter
    private final String configName;
    private int fromConfig;
    private final int current;

    Setting(int ID, String configName, SettingGroup messageGroup, int backUP) {
        this.configName = configName;
        this.backUP = backUP;
        this.id = ID;
        this.messageGroup = messageGroup;
        this.current = getFromConfig();
        setInstance();
    }

    public static ArrayList<Setting> getGroup(SettingGroup messageGroup) {
        switch (messageGroup) {
            case SERVER -> {
                return serverSettings;
            }
            case GAME -> {
                return gameSettings;
            }
            case OTHER -> {
                return otherSettings;
            }
            default -> {
                return otherSettings;
            }
        }
    }

    public static Setting writeArray() {
        serverSettings = new ArrayList<>();
        gameSettings = new ArrayList<>();
        otherSettings = new ArrayList<>();
        for (Setting setting : Setting.values()) {
            switch (setting.getGroup()) {
                case SERVER -> serverSettings.add(setting);
                case GAME -> gameSettings.add(setting);
                case OTHER -> otherSettings.add(setting);
            }
        }
        return instance;
    }

    private Setting setInstance() {
        return instance = this;
    }

    public int getFromConfig() {
        return fromConfig == 0 ? getBackUP() : fromConfig;
    }

    public SettingGroup getGroup() {
        return messageGroup;
    }

}
