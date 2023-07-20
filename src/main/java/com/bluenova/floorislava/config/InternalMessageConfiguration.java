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


    }

    public void reload() {

    }

    public void load() {

    }
}
