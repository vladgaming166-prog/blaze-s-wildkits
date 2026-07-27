package com.blaze.wildkits.kit;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.util.TextUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;

/**
 * Premium kit reveal — when showkit is false, absolutely nothing appears.
 */
public final class KitAnnounceService {

    private final BlazesWildKits plugin;

    public KitAnnounceService(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void announce(Player player, KitDefinition kit, GearBand band) {
        if (!plugin.getConfigManager().isShowKit()) {
            return; // silent — no chat, title, sound, particles, scoreboard kit
        }

        String kitName = TextUtil.strip(kit.getDisplayName());
        KitRarity rarity = kit.getRarity();
        Map<String, String> placeholders = Map.of(
                "kit", kitName,
                "rarity", TextUtil.strip(rarity.getDisplay()),
                "band", band.name().replace('_', ' ')
        );

        plugin.getMessageService().send(player, "kit-received", placeholders);

        if (plugin.getConfigManager().getConfig().getBoolean("show-kit-effects.title", true)) {
            String main = rarity.getColor() + "<bold>" + rarity.name() + "</bold>";
            String sub = plugin.getMessageService().raw("kit-subtitle");
            if (sub == null || sub.isBlank()) sub = "<white>{kit}</white>";
            player.showTitle(Title.title(
                    TextUtil.parse(main),
                    TextUtil.parse(sub, placeholders),
                    Title.Times.times(Duration.ofMillis(80), Duration.ofMillis(1600), Duration.ofMillis(250))
            ));
        }

        if (plugin.getConfigManager().getConfig().getBoolean("show-kit-effects.actionbar", true)) {
            String action = plugin.getMessageService().raw("kit-actionbar");
            player.sendActionBar(TextUtil.parse(action, placeholders));
        }

        if (plugin.getConfigManager().getConfig().getBoolean("show-kit-effects.sound", true)) {
            Sound sound = switch (rarity) {
                case COMMON, UNCOMMON -> Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                case RARE -> Sound.ENTITY_PLAYER_LEVELUP;
                case EPIC -> Sound.UI_TOAST_CHALLENGE_COMPLETE;
                case LEGENDARY -> Sound.ITEM_TOTEM_USE;
                case MYTHIC -> Sound.ENTITY_ENDER_DRAGON_GROWL;
                case GODLY, ULTIMATE -> Sound.UI_TOAST_CHALLENGE_COMPLETE;
            };
            float pitch = switch (rarity) {
                case GODLY, ULTIMATE -> 0.7f;
                case MYTHIC -> 0.85f;
                default -> 1.15f;
            };
            player.playSound(player.getLocation(), sound, 0.85f, pitch);
        }

        if (plugin.getConfigManager().getConfig().getBoolean("show-kit-effects.particles", true)) {
            Particle particle = switch (rarity) {
                case COMMON, UNCOMMON -> Particle.CRIT;
                case RARE -> Particle.ENCHANT;
                case EPIC -> Particle.END_ROD;
                case LEGENDARY -> Particle.TOTEM_OF_UNDYING;
                case MYTHIC -> Particle.SOUL_FIRE_FLAME;
                case GODLY, ULTIMATE -> Particle.FIREWORK;
            };
            int amount = 12 + rarity.getTier() * 6;
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), amount, 0.4, 0.6, 0.4, 0.05);
        }

        if (plugin.getConfigManager().getConfig().getBoolean("show-kit-effects.bossbar", false)
                || rarity.getTier() >= KitRarity.LEGENDARY.getTier()) {
            String barText = rarity.getColor() + "<bold>" + kitName + "</bold>";
            BossBar.Color color = switch (rarity) {
                case LEGENDARY, GODLY, ULTIMATE -> BossBar.Color.YELLOW;
                case MYTHIC -> BossBar.Color.RED;
                case EPIC -> BossBar.Color.PURPLE;
                default -> BossBar.Color.WHITE;
            };
            BossBar bar = BossBar.bossBar(TextUtil.parse(barText), 1.0f, color, BossBar.Overlay.PROGRESS);
            player.showBossBar(bar);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> player.hideBossBar(bar), 50L);
        }

        // Server broadcast for Godly+
        if (rarity == KitRarity.GODLY || rarity == KitRarity.ULTIMATE
                || (rarity == KitRarity.MYTHIC && plugin.getConfigManager().getConfig()
                .getBoolean("show-kit-effects.broadcast-mythic", true))) {
            plugin.getMessageService().broadcast("kit-broadcast", Map.of(
                    "player", player.getName(),
                    "kit", kitName,
                    "rarity", TextUtil.strip(rarity.getDisplay())
            ));
        }
    }
}
