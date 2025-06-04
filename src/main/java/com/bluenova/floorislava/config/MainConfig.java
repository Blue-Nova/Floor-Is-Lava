package com.bluenova.floorislava.config;

import com.bluenova.floorislava.FloorIsLava;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainConfig {

    private final static String CONFIG_VERSION = "1.3";
    private final static MainConfig instance = new MainConfig();

    private File file;
    private YamlConfiguration config;

    private int plotMargin;
    private int plotSize;
    private int plotAmount;
    private int gameStartCountdown;
    private int lavaRiseCooldown;
    private int lavaRiseAmount;
    private int preGameCountDown;
    private boolean musicEnabled;

    // Manual Spawn Point fields
    private boolean manualSpawnEnabled;
    private String manualSpawnItemMaterial;
    private String manualSpawnItemName;
    private List<String> manualSpawnItemLore;
    private boolean manualSpawnItemIsGlowing;


    private MainConfig() {
    }
    public void load() {
        file = new File(FloorIsLava.getInstance().getDataFolder(), "config.yml");

        if (!file.exists())
            FloorIsLava.getInstance().saveResource("config.yml", false);

        config = new YamlConfiguration();
        config.options().parseComments(true);

        try {
            config.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check for version and if version is not the same, update
        if (!config.getString("config_version", "0.0").equals(CONFIG_VERSION)) { // added default "0.0" for safety
            FloorIsLava.getInstance().getLogger().warning("Config version mismatch! You have: " +
                    config.getString("config_version") + " but the plugin is: " + CONFIG_VERSION);
            FloorIsLava.getInstance().getLogger().warning("To update your config, delete the current FloorIsLava/Config.yml file and restart the server.");
        }else {
            FloorIsLava.getInstance().getLogger().info("Config version is up to date.");
        }

        plotMargin = config.getInt("Plot.Margin");
        plotSize = config.getInt("Plot.Size");
        plotAmount = config.getInt("Plot.Amount");
        gameStartCountdown = config.getInt("Game.StartCountdown");
        lavaRiseCooldown = config.getInt("Game.LavaRiseCooldown");
        lavaRiseAmount = config.getInt("Game.LavaRiseAmount");
        preGameCountDown = config.getInt("Game.PreGameCountdown");
        musicEnabled = config.getBoolean("Game.PlayMusic");
        
        // Load Manual Spawn Point settings
        manualSpawnEnabled = config.getBoolean("Game.ManualSpawnPoint.Enabled", true);
        manualSpawnItemMaterial = config.getString("Game.ManualSpawnPoint.ItemMaterial", "RED_BED");
        manualSpawnItemName = config.getString("Game.ManualSpawnPoint.ItemName", "<gold>Respawn Anchor</gold>");
        manualSpawnItemLore = config.getStringList("Game.ManualSpawnPoint.ItemLore");
        if (manualSpawnItemLore.isEmpty()) { // Default lore if not specified or empty
            manualSpawnItemLore = Arrays.asList(
               "<gray>Right-click to set your respawn point!",
                    "<gray>One-time use for this match.",
                    "<italic><dark_gray>Becomes unusable if lava reaches it.</dark_gray>"
            );
        }
        manualSpawnItemIsGlowing = config.getBoolean("Game.ManualSpawnPoint.ItemIsGlowing", false);

        // log all values
    }

    public void save(){
        try {
            config.save(file);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void set(String path, Object value){
        config.set(path,value);
        save();
    }

    public static MainConfig getInstance(){
        return instance;
    }

    public int getPlotMargin(){
        return plotMargin;
    }
    public int getPlotSize(){
        return plotSize;
    }
    public int getPlotAmount(){
        return plotAmount;
    }

    // Inside MainConfig.java
    public boolean isDevModeEnabled() {
        // Load config if null (or ensure load() called first)
        if (config == null) { load(); }
        // Default to false if missing
        return config != null && config.getBoolean("developer-mode", false);
    }

    public int getGameStartCountdown() {
        return gameStartCountdown;
    }
    public int getLavaRiseCooldown() {
        return lavaRiseCooldown;
    }
    public int getLavaRiseAmount() {
        return lavaRiseAmount;
    }

    public int getPreGameCountdown() {
        return preGameCountDown;
    }

    public boolean isGameMusicEnabled() {
        return musicEnabled;
    }

    // New Getters for Manual Spawn Point
    public boolean isManualSpawnEnabled() { return manualSpawnEnabled; }
    public String getManualSpawnItemMaterial() { return manualSpawnItemMaterial; }
    public String getManualSpawnItemName() { return manualSpawnItemName; }
    public List<String> getManualSpawnItemLore() { return manualSpawnItemLore; }
    public boolean isManualSpawnItemIsGlowing() { return manualSpawnItemIsGlowing; }
}