package com.bluenova.floorislava.config;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.util.messages.MiniMessages;
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.util.List;

public class PlayerDataManager {

    private final PluginLogger pluginLogger;
    private final FloorIsLava plugin;
    private File playerDataFolder;

    public PlayerDataManager(PluginLogger pluginLogger, FloorIsLava plugin) {
        this.pluginLogger = pluginLogger;
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
    }

    public boolean savePlayerData(Player player) {
        File playerFile = getPlayerFile(player);
        File parentDir = playerFile.getParentFile(); // This is playerDataFolder

        // --- Create Subfolder if it doesn't exist ---
        if (!parentDir.exists()) {
            plugin.getLogger().info("Creating player data directory: " + parentDir.getPath());
            if (!parentDir.mkdirs()) {
                plugin.getLogger().severe("!!! Could not create player data directory: " + parentDir.getPath());
                plugin.getLogger().severe("!!! Player state for " + player.getName() + " CANNOT BE SAVED.");
                return false;
            }
        }
        // --- Subfolder guaranteed to exist here (unless OS permissions issue) ---
        YamlConfiguration config = new YamlConfiguration();
        // Set player data...
        config.set("in-active-game", true);
        config.set("restore-location", player.getLocation());
        config.set("restore-inventory", player.getInventory().getContents());
        config.set("restore-health", player.getHealth());
        config.set("restore-food", player.getFoodLevel());
        config.set("restore-xp", player.getExp());
        config.set("restore-level", player.getLevel());
        config.set("restore-gamemode", player.getGameMode().name());
        // Add any other relevant data (potion effects? score?)

        try {
            config.save(playerFile); // Save the file
        } catch (java.io.IOException e) {
            pluginLogger.severe("Could not save "+ player.getName() +" state file: " + playerFile.getPath());
            return false;
        }
        return true;
    }

    public boolean restoreStateIfNecessary(Player player) {

        // list of scenarios for player data restoration:
        // 1. Player is rejoining server after being in a game
        // 2. Player is rejoining server after server crash
        // 3. Player is rejoining server after plugin reload
        // 4. Player is in a game while server reloaded
        // 5. Player finished a game

        // Check if player is ONLINE
        if (!player.isOnline()) {
            pluginLogger.debug("Player " + player.getName() + " is not online. No restoration needed.");
            return false;
        }

        File playerFile = getPlayerFile(player);

        if (!playerFile.exists()) {
            pluginLogger.debug("Player data file not found for " + player.getName() + ". No restoration needed.");
            return false;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        if (config.getBoolean("in-active-game", false)) {
            // Load data carefully
            Location loc = config.getLocation("restore-location");
            // Bukkit's deserialization for ItemStack[] from lists can be tricky, check this carefully
            List<?> rawList = config.getList("restore-inventory");
            ItemStack[] inv;
            if (rawList != null) {
                try {
                    // Filter out nulls potentially introduced during serialization/deserialization
                    inv = rawList.stream().filter(ItemStack.class::isInstance).map(ItemStack.class::cast).toArray(ItemStack[]::new);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to deserialize inventory for " + player.getName() + " from " + playerFile.getPath());
                    inv = new ItemStack[0]; // Fallback to empty
                }
            } else {
                plugin.getLogger().warning("Inventory list is null for " + player.getName() + " in " + playerFile.getPath());
                return false; // No inventory to restore
            }

            double health = config.getDouble("restore-health", 20.0); // Default to 20 if missing
            int food = config.getInt("restore-food", 20);
            float xp = (float) config.getDouble("restore-xp", 0.0);
            int level = config.getInt("restore-level", 0);
            String gamemodeName = config.getString("restore-gamemode", "SURVIVAL");
            GameMode gamemode;
            try {
                gamemode = GameMode.valueOf(gamemodeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                gamemode = Bukkit.getDefaultGameMode(); // Fallback to server default
            }

            // Store final variables for lambda/runnable
            final Location finalLoc = loc;
            final ItemStack[] finalInv = inv;
            final GameMode finalGamemode = gamemode;

            // --- Schedule Sync Task for Restoration ---
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().info("Restoring pre-game state for " + player.getName() + " from " + playerFile.getPath());

                // Restore state
                if (finalLoc != null) player.teleport(finalLoc); // Teleport first
                player.getInventory().setContents(finalInv);
                player.setHealth(health); // Restore health after inventory maybe?
                player.setFoodLevel(food);
                player.setExp(xp);
                player.setLevel(level);
                player.setGameMode(finalGamemode);
                player.setFireTicks(0); // Clear fire
                // Clear potions again?
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                player.setWorldBorder(null); // Clear any game border

                if (MainConfig.getInstance().isDevModeEnabled()){
                    MiniMessages.send(player, "dev.state_restored"); // Notify player
                }

                // --- Delete file AFTER successful restore ---
                if (!playerFile.delete()) {
                    plugin.getLogger().warning("Could not delete player state file after restore: " + playerFile.getPath());
                }
            });
            // --- End Sync Task ---
        } else {
            // File existed but wasn't flagged, likely leftover. Delete it.
            plugin.getLogger().warning("Found unexpected player data file for " + player.getName() + ". Deleting file: " + playerFile.getPath());
            playerFile.delete();
        }
        return true;
    }

    private File getPlayerFile(Player player) {
        return new File(playerDataFolder, player.getName() + "_" + player.getUniqueId() + ".yml");
    }
}
