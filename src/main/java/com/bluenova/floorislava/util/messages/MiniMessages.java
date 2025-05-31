package com.bluenova.floorislava.util.messages;

import com.bluenova.floorislava.FloorIsLava; // Import main plugin class
import com.bluenova.floorislava.config.MessageConfig; // To get messages
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MiniMessages {

    public static final MiniMessage miniM = MiniMessage.miniMessage();
    private static PluginLogger pluginLogger = null;
    private static MessageConfig messageConfig = null;

    public static void init(FloorIsLava plugin, PluginLogger logger, MessageConfig config) {
        if (pluginLogger == null) {
            pluginLogger = logger;
        }
        if (messageConfig == null) {
            messageConfig = config;
        }
    }

    public static Component createComponent(String string) {
        // Serialize and deserialize a message
        return miniM.deserialize(string);
    }

    public static Component getParsedComponent(String messageKey) {
        String rawMessage = getRawMessage(messageKey); // Use existing helper
        if (rawMessage == null) {
            // Return an error component or Component.empty()
            return Component.text("[MissingKey: " + messageKey + "]", NamedTextColor.RED);
        }
        // Deserialize without additional placeholders
        return miniM.deserialize(rawMessage);
    }

    // --- Existing methods for Player ---
    public static void send(Player player, String messageKey) {
        send(player, messageKey, TagResolver.empty());
    }

    public static void send(Player player, String messageKey, TagResolver placeholders) {

        String rawPrefix = messageConfig.getRawPrefix();

        String rawMessage = getRawMessage(messageKey);
        if (rawMessage == null) { return; }

        Component prefixComponent = Component.empty(); // Default to nothing
        if (rawPrefix != null && !rawPrefix.isEmpty()) {
            prefixComponent = miniM.deserialize(rawPrefix);
            // OPTIONAL: Add a space if your prefix doesn't end with one
            if (!rawPrefix.endsWith(" ")) {
                prefixComponent = prefixComponent.append(Component.space());
            }
        }

        // Parse
        Component messageComponent = miniM.deserialize(rawMessage, placeholders);

        Component finalComponent = prefixComponent.append(messageComponent);

        // Send using player audience
        player.sendMessage(finalComponent);
    }

    // --- NEW methods for CommandSender ---
    public static void send(CommandSender sender, String messageKey) {
        send(sender, messageKey, TagResolver.empty()); // Call overload below
    }

    public static void send(CommandSender sender, String messageKey, TagResolver placeholders) {

        String rawMessage = getRawMessage(messageKey);
        if (rawMessage == null) {
            // Send plain text error to console/non-player
            sender.sendMessage(ChatColor.RED + "[FloorIsLava] Missing message key: " + messageKey);
            pluginLogger.warning("Missing message key in MessageConfig.yml: " + messageKey);
            return;
        }

        // Parse (same as before)
        Component parsedComponent = miniM.deserialize(rawMessage, placeholders);

        // Send the component
        sender.sendMessage(parsedComponent);
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
