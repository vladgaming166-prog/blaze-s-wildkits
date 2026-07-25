package com.blaze.wildkits.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class TextUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private TextUtil() {}

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        String normalized = input.replace('&', '§');
        // Prefer MiniMessage when tags are present; otherwise legacy.
        if (input.contains("<") && input.contains(">")) {
            try {
                return MINI.deserialize(input);
            } catch (Exception ignored) {
                return LEGACY.deserialize(normalized);
            }
        }
        return LEGACY.deserialize(ChatColor.translateAlternateColorCodes('&', input));
    }

    public static Component parse(String input, Map<String, String> placeholders) {
        String result = input == null ? "" : input;
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                result = result.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
                result = result.replace("%" + e.getKey() + "%", e.getValue() == null ? "" : e.getValue());
            }
        }
        return parse(result);
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
        return ChatColor.stripColor(legacy(input));
    }
}
