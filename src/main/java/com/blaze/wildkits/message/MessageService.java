package com.blaze.wildkits.message;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;

public final class MessageService {

    private final BlazesWildKits plugin;
    private FileConfiguration messages;
    private String prefix = "<gold><bold>WildKits</bold></gold> <dark_gray>»</dark_gray> ";

    public MessageService(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void load() {
        String lang = plugin.getConfigManager().getLanguage();
        String fileName = "messages_" + lang + ".yml";
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            if (plugin.getResource(fileName) != null) {
                plugin.saveResource(fileName, false);
            } else {
                fileName = "messages_en.yml";
                file = new File(plugin.getDataFolder(), fileName);
                if (!file.exists()) {
                    plugin.saveResource("messages_en.yml", false);
                }
            }
        }
        this.messages = YamlConfiguration.loadConfiguration(file);
        this.prefix = messages.getString("prefix", prefix);
    }

    public String raw(String key) {
        return messages.getString(key, "<red>Missing message: " + key);
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String message = raw(key);
        if (message.equalsIgnoreCase("none") || message.isBlank()) return;
        String full = (message.contains("<noprefix>") ? message.replace("<noprefix>", "") : prefix + message);
        String resolved = TextUtil.applyPlaceholders(full, placeholders);
        if (sender instanceof Player player) {
            TextUtil.send(player, resolved);
        } else {
            TextUtil.send(sender, TextUtil.strip(resolved));
        }
    }

    public void broadcast(String key, Map<String, String> placeholders) {
        String message = raw(key);
        if (message.equalsIgnoreCase("none") || message.isBlank()) return;
        String full = (message.contains("<noprefix>") ? message.replace("<noprefix>", "") : prefix + message);
        String resolved = TextUtil.applyPlaceholders(full, placeholders);
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            TextUtil.send(player, resolved);
        }
        plugin.getLogger().info(TextUtil.strip(resolved));
    }

    public String getPrefix() {
        return prefix;
    }
}
