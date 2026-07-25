package com.blaze.wildkits.kit;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.util.TextUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;

/**
 * Controls whether kit identity is revealed (chat/title/actionbar/bossbar/scoreboard).
 */
public final class KitAnnounceService {

    private final BlazesWildKits plugin;

    public KitAnnounceService(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void announce(Player player, KitDefinition kit, GearBand band) {
        if (!plugin.getConfigManager().isShowKit()) {
            return;
        }

        String kitName = TextUtil.strip(kit.getDisplayName());
        Map<String, String> placeholders = Map.of(
                "kit", kitName,
                "rarity", kit.getRarity().name(),
                "band", band.name()
        );

        // Chat
        plugin.getMessageService().send(player, "kit-received", placeholders);

        // Title
        if (plugin.getConfigManager().getConfig().getBoolean("show-kit-effects.title", true)) {
            String main = plugin.getMessageService().raw("kit-title");
            String sub = plugin.getMessageService().raw("kit-subtitle");
            player.showTitle(Title.title(
                    TextUtil.parse(main, placeholders),
                    TextUtil.parse(sub, placeholders),
                    Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(1400), Duration.ofMillis(200))
            ));
        }

        // Action bar
        if (plugin.getConfigManager().getConfig().getBoolean("show-kit-effects.actionbar", true)) {
            String action = plugin.getMessageService().raw("kit-actionbar");
            player.sendActionBar(TextUtil.parse(action, placeholders));
        }

        // Optional short bossbar flash
        if (plugin.getConfigManager().getConfig().getBoolean("show-kit-effects.bossbar", false)) {
            String barText = plugin.getMessageService().raw("kit-bossbar");
            BossBar bar = BossBar.bossBar(
                    TextUtil.parse(barText, placeholders),
                    1.0f,
                    BossBar.Color.YELLOW,
                    BossBar.Overlay.PROGRESS
            );
            player.showBossBar(bar);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> player.hideBossBar(bar), 40L);
        }
    }
}
