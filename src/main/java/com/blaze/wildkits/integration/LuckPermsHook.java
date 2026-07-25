package com.blaze.wildkits.integration;

import com.blaze.wildkits.BlazesWildKits;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class LuckPermsHook {

    private final BlazesWildKits plugin;
    private LuckPerms api;
    private boolean enabled;

    public LuckPermsHook(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().info("LuckPerms not found. Using Bukkit permissions.");
            return;
        }
        try {
            this.api = LuckPermsProvider.get();
            this.enabled = true;
            plugin.getLogger().info("LuckPerms hooked.");
        } catch (IllegalStateException ex) {
            plugin.getLogger().warning("LuckPerms present but API unavailable.");
        }
    }

    public boolean isEnabled() {
        return enabled && api != null;
    }

    public boolean hasPermission(Player player, String permission) {
        if (player.hasPermission(permission)) return true;
        if (!isEnabled()) return false;
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return false;
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public String getPrefix(Player player) {
        if (!isEnabled()) return "";
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        String prefix = user.getCachedData().getMetaData().getPrefix();
        return prefix == null ? "" : prefix;
    }
}
