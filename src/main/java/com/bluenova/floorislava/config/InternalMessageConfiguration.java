package com.bluenova.floorislava.config;

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
        for (Group group : Group.values()) {
            for (Message message : Message.getGroup(group)) {
                JSONObject g = (JSONObject) master.get(group.name());
                message.setFromConfig((String) g.get(message.name()));
            }
        }
    }
}
