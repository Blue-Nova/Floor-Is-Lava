package com.bluenova.floorislava.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;

import java.io.File;

public class InternalMessageConfiguration extends ConfigurationLIB {

    private JSONObject master;

    public InternalMessageConfiguration(File file) {
        super(file);
        saveDefaults();
        addToCache(master, "Settings");
        saveCache();
        handleCreation();
    }

    @Override
    public void saveDefaults() {
        master = new JSONObject();
        for (Group group : Group.values()) {
            JSONObject g = new JSONObject();
            for (Message message : Message.getGroup(group)) {
                g.put(message.name(), message.getBackUP());
            }
            master.put(group.name(), g);
        }
    }

    public void reload() {
        master = (JSONObject) getJson().get("Settings");
        if (master == null) return;
        load();
    }

    public void load() {
        try {
            for (Group group : Group.values()) {
                for (Message message : Message.getGroup(group)) {
                    JSONObject g = (JSONObject) master.get(group.name());
                    message.setFromConfig((String) g.get(message.name()));
                }
            }
        } catch (NullPointerException ex) {
            Bukkit.getConsoleSender().sendMessage(Message.PREFIX.replacePrefix().format() + ChatColor.RED + "[FATAL] Cannot Pull Main Internal Configuration. Pulling Backup, please Restart the Plugin to Use Custom Values!");
        }
    }
}
