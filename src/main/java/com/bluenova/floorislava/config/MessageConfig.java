package com.bluenova.floorislava.config;

import com.bluenova.floorislava.FloorIsLava; // Your main plugin class
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class MessageConfig {
    private File file;
    private YamlConfiguration config = null; // Initialize as null
    private final PluginLogger pluginLogger;

    public MessageConfig(PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
        load(); // Load the config when the class is instantiated
    }

    /**
     * Loads the MessageConfig.yml file, creating it from resources if it doesn't exist.
     */
    public void load() {
        FloorIsLava plugin = (FloorIsLava) FloorIsLava.getInstance();
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                pluginLogger.severe("Could not create plugin data folder!");
            }
        }

        file = new File(dataFolder, "MessageConfig.yml");

        if (!file.exists()) {
            pluginLogger.info("MessageConfig.yml not found, creating default...");
            try {
                // Ensure MessageConfig.yml is in src/main/resources
                plugin.saveResource("MessageConfig.yml", false);
            } catch (IllegalArgumentException e) {
                pluginLogger.severe("Could not save default MessageConfig.yml! Make sure it's in your JAR's src/main/resources folder." + e);
                this.config = null; // Ensure config is null on failure
                return;
            } catch (Exception e) {
                pluginLogger.severe("An unexpected error occurred saving default MessageConfig.yml!" + e);
                this.config = null; // Ensure config is null on failure
                return;
            }
        }

        // Load the configuration from the file
        config = YamlConfiguration.loadConfiguration(file);

        // Optional: Check if loading actually worked (e.g., check if a known key exists)
        if (config.getKeys(false).isEmpty() && file.length() > 0) {
            pluginLogger.severe("Failed to load MessageConfig.yml properly! It might be corrupted.");
            // Keep config non-null but potentially empty, getters will return defaults/errors.
        } else {
            pluginLogger.info("MessageConfig.yml loaded successfully!.");
        }
    }

    /**
     * Gets the raw message string for the given key from the config.
     * Returns a default error message if the config failed to load or the key is missing.
     * @param key The message key (e.g., "lobby.created")
     * @return The raw message string or an error string.
     */
    public String getRawString(String key) {
        if (config == null) { // Check if config failed to load in load()
            FloorIsLava.getInstance().getLogger().severe("Attempted to get message key '" + key + "' but MessageConfig failed to load!");
            return "<bold><red>ERROR: CFG NULL</red></bold>"; // Return noticeable error
        }
        // Provide default value to getString to handle missing keys gracefully
        return config.getString(key, "<bold><red>MissingKey: " + key + "</red></bold>"); // Return missing key error
    }

    /**
     * Gets the raw prefix string from the config.
     * Returns a hardcoded default if config failed to load or key is missing.
     * @return The raw prefix string.
     */
    public String getRawPrefix() {
        String defaultPrefix = "<white>[<red>F</red><yellow>I</yellow><gold>L</gold><white>] </white>"; // Hardcoded fallback
        if (config == null) { // Check if config failed to load
            FloorIsLava.getInstance().getLogger().severe("Attempted to get prefix but MessageConfig failed to load!");
            return defaultPrefix;
        }
        // Provide default value
        return config.getString("general.prefix", defaultPrefix);
    }

    /**
     * Saves the current configuration back to the file.
     * Use only if you modify config programmatically.
    public void save() {
        if (config != null && file != null) {
            try {
                config.save(file);
            } catch (java.io.IOException e) {
                FloorIsLava.getInstance().getLogger().log(Level.SEVERE, "Could not save MessageConfig.yml to " + file, e);
            }
        } else {
            FloorIsLava.getInstance().getLogger().severe("Cannot save MessageConfig, config or file is null!");
        }
    }
     */
}