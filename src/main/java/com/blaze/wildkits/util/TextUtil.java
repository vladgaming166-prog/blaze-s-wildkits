package com.blaze.wildkits.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Pattern SHADOW_PREFIX = Pattern.compile(
            "^(?i)shadow:([#a-zA-Z0-9]+):(\\d+(?:\\.\\d+)?)\\s+(.*)$");

    private TextUtil() {}

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        String prepared = preprocess(input);
        if (prepared.contains("<") && prepared.contains(">")) {
            try {
                return MINI.deserialize(prepared);
            } catch (Exception ignored) {
                return LEGACY.deserialize(prepared.replace('&', '§'));
            }
        }
        return LEGACY.deserialize(ChatColor.translateAlternateColorCodes('&', prepared));
    }

    public static Component parse(String input, Map<String, String> placeholders) {
        return parse(applyPlaceholders(input, placeholders));
    }

    public static String applyPlaceholders(String input, Map<String, String> placeholders) {
        String result = input == null ? "" : input;
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                String value = e.getValue() == null ? "" : e.getValue();
                result = result.replace("{" + e.getKey() + "}", value);
                result = result.replace("%" + e.getKey() + "%", value);
            }
        }
        return result;
    }

    /**
     * Supports Chaos-style prefixes: {@code shadow:black:1 <message>}
     * and legacy gradient forms without a wrapping open tag.
     */
    public static String preprocess(String input) {
        String text = input == null ? "" : input.trim();
        Matcher shadow = SHADOW_PREFIX.matcher(text);
        if (shadow.matches()) {
            String color = normalizeShadowColor(shadow.group(1));
            String body = shadow.group(3);
            text = "<shadow:" + color + ">" + body + "</shadow>";
        }
        // Fix common Chaos/title form: gradient:#A:#B<bold>X</bold></gradient>
        if (text.startsWith("gradient:") && text.contains("<") && !text.startsWith("<")) {
            int firstLt = text.indexOf('<');
            String header = text.substring(0, firstLt);
            String rest = text.substring(firstLt);
            text = "<" + header + ">" + rest;
            if (!text.contains("</gradient>") && text.contains("</bold>")) {
                // leave as-is; MiniMessage may still parse gradient tag
            }
        }
        return text;
    }

    private static String normalizeShadowColor(String raw) {
        if (raw == null) return "#000000";
        if (raw.startsWith("#")) return raw;
        return switch (raw.toLowerCase()) {
            case "black" -> "#000000";
            case "dark_gray", "darkgray" -> "#2B2B2B";
            case "gray" -> "#555555";
            case "white" -> "#FFFFFF";
            case "red" -> "#FF0000";
            case "orange" -> "#FF4500";
            case "gold", "yellow" -> "#FFD700";
            default -> "#000000";
        };
    }

    public static void send(CommandSender sender, String message) {
        if (message == null || message.isBlank()) return;
        sender.sendMessage(parse(message));
    }

    public static void send(Player player, String message, Map<String, String> placeholders) {
        if (message == null || message.isBlank()) return;
        player.sendMessage(parse(message, placeholders));
    }

    public static String legacy(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }

    public static String strip(String input) {
        return ChatColor.stripColor(legacy(input.replaceAll("<[^>]+>", "")));
    }
}
