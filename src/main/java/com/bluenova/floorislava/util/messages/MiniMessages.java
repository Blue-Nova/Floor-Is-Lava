package com.bluenova.floorislava.util.messages;

import com.bluenova.floorislava.FloorIsLava; // Import main plugin class
import com.bluenova.floorislava.config.MessageConfig; // To get messages
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences; // Import BukkitAudiences
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver; // For later placeholder use
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MiniMessages {

    public static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static BukkitAudiences adventure = null;
    private static PluginLogger pluginLogger = null;
    private static MessageConfig messageConfig = null;

    public static void init(FloorIsLava plugin, PluginLogger logger, MessageConfig config) {
        if (adventure == null) {
            adventure = plugin.adventure();
        }
        if (pluginLogger == null) {
            pluginLogger = logger;
        }
        if (messageConfig == null) {
            messageConfig = config;
        }
    }

    public static Component getParsedComponent(String messageKey) {
        String rawMessage = getRawMessage(messageKey); // Use existing helper
        if (rawMessage == null) {
            // Return an error component or Component.empty()
            return Component.text("[MissingKey: " + messageKey + "]", NamedTextColor.RED);
        }
        // Deserialize without additional placeholders
        return miniMessage.deserialize(rawMessage);
    }

    // --- Existing methods for Player ---
    public static void send(Player player, String messageKey) {
        send(player, messageKey, TagResolver.empty());
    }

    public static void send(Player player, String messageKey, TagResolver placeholders) {
        if (adventure == null) { return; }

        String rawPrefix = messageConfig.getRawPrefix();

        String rawMessage = getRawMessage(messageKey);
        if (rawMessage == null) { return; }

        Component prefixComponent = Component.empty(); // Default to nothing
        if (rawPrefix != null && !rawPrefix.isEmpty()) {
            prefixComponent = miniMessage.deserialize(rawPrefix);
            // OPTIONAL: Add a space if your prefix doesn't end with one
            if (!rawPrefix.endsWith(" ")) {
                prefixComponent = prefixComponent.append(Component.space());
            }
        }

        // Parse
        Component messageComponent = miniMessage.deserialize(rawMessage, placeholders);

        Component finalComponent = prefixComponent.append(messageComponent);

        // Send using player audience
        adventure.player(player).sendMessage(finalComponent);
    }

    // --- NEW methods for CommandSender ---
    public static void send(CommandSender sender, String messageKey) {
        send(sender, messageKey, TagResolver.empty()); // Call overload below
    }

    public static void send(CommandSender sender, String messageKey, TagResolver placeholders) {
        if (adventure == null) {
            // Fallback for console/non-player senders if Adventure isn't ready
            sender.sendMessage(ChatColor.RED + "[FIL] Message system error.");
            pluginLogger.severe("[FloorIsLava] MiniMessages not initialized! adventure API is null.");
            return;
        }

        String rawMessage = getRawMessage(messageKey);
        if (rawMessage == null) {
            // Send plain text error to console/non-player
            sender.sendMessage(ChatColor.RED + "[FloorIsLava] Missing message key: " + messageKey);
            pluginLogger.warning("Missing message key in MessageConfig.yml: " + messageKey);
            return;
        }

        // Parse (same as before)
        Component parsedComponent = miniMessage.deserialize(rawMessage, placeholders);

        // Get the generic Audience for the sender (works for Player AND Console)
        Audience audience = adventure.sender(sender);

        // Send the component
        audience.sendMessage(parsedComponent);
    }


    // Helper to get raw message strings (ensure this is implemented)
    public static String getRawMessage(String key) {
        // ... your logic using MessageConfig ...
        String raw = messageConfig.getRawString(key); // Assumes MessageConfig has getRawString(key)
        if (raw == null) {
            // Return null or a default error string, handled above
            return null;
        }
        return raw;
    }
}
