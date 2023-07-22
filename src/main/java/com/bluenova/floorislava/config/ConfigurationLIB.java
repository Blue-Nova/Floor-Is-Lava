package com.bluenova.floorislava.config;

import com.bluenova.floorislava.FloorIsLava;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public abstract class ConfigurationLIB {

    @Getter
    private static final ArrayList<String> registeredConfigs = new ArrayList<>();
    private final File file;
    @Getter
    private final HashMap<String, Object> defaults = new HashMap<>();
    @Getter
    private final JSONParser parser;
    @Getter
    private JSONObject json;

    public ConfigurationLIB(File file) {
        this.file = file;
        this.parser = new JSONParser();
        runFileLogic();
        try {
            registeredConfigs.add(file.getName());
            this.json = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public abstract void saveDefaults();

    public void handleCreation() {
        saveCache();
    }

    public void addToCache(Object object, String registerName) {
        defaults.put(registerName, object);
    }

    private void runFileLogic() {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
                PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8);
                pw.print("{");
                pw.print("}");
                pw.flush();
                pw.close();
            }
        } catch (Exception ex) {
            FloorIsLava.getInstance().getConsoleSender().sendMessage("Error Creating File");
        }
    }

    public void saveCache() {
        JSONObject toSave = new JSONObject();
        for (String s : defaults.keySet()) {
            Object o = defaults.get(s);
            if (o instanceof String) toSave.put(s, getString(s));
            if (o instanceof Double) toSave.put(s, getDouble(s));
            if (o instanceof Integer) toSave.put(s, getInteger(s));
            if (o instanceof JSONObject) toSave.put(s, getObject(s));
            if (o instanceof JSONArray) toSave.put(s, getArray(s));
        }
        TreeMap<String, Object> treeMap = new TreeMap<String, Object>(toSave);
        String prettyJsonString = FloorIsLava.getInstance().getGson().toJson(treeMap);
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(prettyJsonString);
            fw.flush();
            fw.close();
        } catch (Exception ex) {
            FloorIsLava.getInstance().getConsoleSender().sendMessage("Error Writing to File");
        }
    }

    public String getRawData(String key) {
        if (!json.containsKey(key)) return "";

        return String.valueOf(defaults.get(key));
    }

    public File getFile() {
        return file;
    }

    public String getString(String key) {
        return ChatColor.translateAlternateColorCodes('§', getRawData(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getRawData(key));
    }

    public double getDouble(String key) {
        try {
            return Double.parseDouble(getRawData(key));
        } catch (Exception ex) {
            //
        }
        return -1;
    }

    public double getInteger(String key) {
        try {
            return Integer.parseInt(getRawData(key));
        } catch (Exception ex) {
            //
        }
        return -1;
    }

    public JSONObject getObject(String key) {
        return json.containsKey(key) ? (JSONObject) json.get(key)
                : (defaults.containsKey(key) ? (JSONObject) defaults.get(key) : new JSONObject());
    }

    public JSONArray getArray(String key) {
        return json.containsKey(key) ? (JSONArray) json.get(key)
                : (defaults.containsKey(key) ? (JSONArray) defaults.get(key) : new JSONArray());
    }


    public boolean contains(String key) {
        return json.containsKey(key);
    }

    public void remove(String key) {
        json.remove(key);
    }
}
