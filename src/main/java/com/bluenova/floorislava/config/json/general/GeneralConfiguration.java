package com.bluenova.floorislava.config.json.general;

import com.bluenova.floorislava.config.json.message.Message;
import com.bluenova.floorislava.config.lib.ConfigurationLIB;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;

import java.io.File;

public class GeneralConfiguration extends ConfigurationLIB {

    private JSONObject master;

    public GeneralConfiguration(File file) {
        super(file);
        saveDefaults();
        addToCache(master, "Settings");
        saveCache();
        handleCreation();
    }

    @Override
    public void saveDefaults() {
        master = new JSONObject();
        for (SettingGroup settingGroup : SettingGroup.values()) {
            JSONObject g = new JSONObject();
            for (Setting setting : Setting.getGroup(settingGroup)) {
                g.put(setting.getConfigName(), setting.getBackUP());
            }
            master.put(settingGroup.getConfigName(), g);
        }
    }

    public void reload() {
        master = (JSONObject) getJson().get("Settings");
        if (master == null) return;
        load();
    }

    public void load() {
        try {
            for (SettingGroup settingGroup : SettingGroup.values()) {
                for (Setting setting : Setting.getGroup(settingGroup)) {
                    JSONObject g = (JSONObject) master.get(settingGroup.getConfigName());
                    setting.setFromConfig((Integer) g.get(setting.getConfigName()));
                }
            }
        } catch (NullPointerException ex) {
            Bukkit.getConsoleSender().sendMessage(Message.PREFIX.replacePrefix().format() + ChatColor.RED + "[FATAL] Cannot Pull Main Internal Configuration. Pulling Backup, please Restart the Plugin to Use Custom Values!");
        }
    }
}
