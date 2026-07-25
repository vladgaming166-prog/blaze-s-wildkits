package com.blaze.wildkits.integration;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderHook extends PlaceholderExpansion {

    private final BlazesWildKits plugin;

    public PlaceholderHook(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "wildkits";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Blaze";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        PlayerData data = plugin.getPlayerDataManager().get(player);
        return switch (params.toLowerCase()) {
            case "kills" -> String.valueOf(data.getKills());
            case "deaths" -> String.valueOf(data.getDeaths());
            case "kd" -> String.valueOf(data.getKd());
            case "coins" -> String.valueOf(data.getCoins());
            case "level" -> String.valueOf(data.getLevel());
            case "xp" -> String.valueOf(data.getXp());
            case "currentkit" -> {
                if (!plugin.getConfigManager().isShowKit()) {
                    yield plugin.getConfigManager().getConfig().getString("show-kit-hidden-text", "???");
                }
                yield data.getCurrentKit() == null ? "None" : data.getCurrentKit();
            }
            case "killstreak" -> String.valueOf(data.getKillstreak());
            case "bestkillstreak" -> String.valueOf(data.getBestKillstreak());
            case "trail" -> data.getActiveTrail() == null ? "None" : data.getActiveTrail();
            case "tag" -> data.getActiveTag() == null ? "" : data.getActiveTag();
            case "title" -> data.getActiveTitle() == null ? "" : data.getActiveTitle();
            case "playtime" -> String.valueOf(data.getPlaytimeSeconds() / 60);
            default -> null;
        };
    }
}
