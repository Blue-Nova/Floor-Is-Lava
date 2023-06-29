package com.bluenova.floorislava.util;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

@SuppressWarnings("deprecated")
public class MessageUtil {

    public static void sendHoverableMessage(Player player, String message, String displayOnHover) {
        TextComponent msg = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(displayOnHover)));
    }

    public static void sendClickableMessage(Player player, String message, String command) {
        TextComponent msg = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
    }

    public static void sendModifiedMessage(Player player, String actionPart, boolean clickAble, @Nullable String command, boolean hoverAble, @Nullable String hoverText) {
        TextComponent clickAbleComp = new TextComponent(ChatColor.translateAlternateColorCodes('&', actionPart));
        if (clickAble) clickAbleComp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        if (hoverAble)
            clickAbleComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', hoverText))));
        player.spigot().sendMessage(clickAbleComp);
    }

    public static TextComponent getClickableMessage(String message, String command) {
        TextComponent msg = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        return msg;
    }

    public static TextComponent getHoverableMessage(String message, String displayOnHover) {
        TextComponent msg = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(displayOnHover)));
        return msg;
    }

    public static TextComponent getClickableHoverableText(String message, String command, String hoverText) {
        TextComponent msg = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', hoverText))));
        return msg;
    }

    public static TextComponent getNormalText(String message) {
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendModifiedMessage(Player player, String defaultText, String actionPart, @Nullable String command, @Nullable String hoverText) {
        TextComponent rawMSG = new TextComponent(ChatColor.translateAlternateColorCodes('&', defaultText));
        TextComponent clickAbleComp = new TextComponent(ChatColor.translateAlternateColorCodes('&', actionPart));
        clickAbleComp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        clickAbleComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', hoverText))));
        rawMSG.addExtra(clickAbleComp);
        player.spigot().sendMessage(rawMSG);
    }

    public static void sendTextComponent(Player player, TextComponent... textComponent) {
        TextComponent mergedComponent = new TextComponent();
        for (TextComponent component : textComponent) {
            mergedComponent.addExtra(component);
        }
        player.spigot().sendMessage(mergedComponent);
    }
}
