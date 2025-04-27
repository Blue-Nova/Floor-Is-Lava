package com.bluenova.floorislava.util.messages; // Or a dedicated logging package

import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;
import java.util.function.Supplier; // For deferred message creation

public class PluginLogger {

    private final JavaPlugin plugin;
    private final boolean devModeEnabled;
    private final String debugPrefix = "[DEBUG] "; // Prefix for debug messages

    public PluginLogger(JavaPlugin plugin, boolean devModeEnabled) {
        this.plugin = plugin;
        this.devModeEnabled = devModeEnabled;
    }

    // Standard log levels - Delegate directly to plugin logger
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    public void warning(String message) {
        plugin.getLogger().warning(message);
    }

    public void severe(String message) {
        plugin.getLogger().severe(message);
    }

    public void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    public void log(Level level, String message, Throwable thrown) {
        plugin.getLogger().log(level, message, thrown);
    }

    // --- Developer/Debug Logging ---

    /**
     * Logs a message with INFO level, but only if developer-mode is enabled in config.
     * Prefixed with [DEBUG].
     * @param message The message string to log.
     */
    public void debug(String message) {
        if (devModeEnabled) {
            // Log using INFO level but add a prefix to distinguish it
            plugin.getLogger().info(debugPrefix + message);
        }
    }

    /**
     * Logs a message with INFO level, but only if developer-mode is enabled.
     * Uses a Supplier, so the message string is only constructed if needed.
     * Prefixed with [DEBUG].
     * @param messageSupplier A Supplier that provides the message string.
     */
    public void debug(Supplier<String> messageSupplier) {
        if (devModeEnabled) {
            // Message is only generated if devMode is true
            plugin.getLogger().info(debugPrefix + messageSupplier.get());
        }
    }
}