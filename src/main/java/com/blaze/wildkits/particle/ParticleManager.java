package com.blaze.wildkits.particle;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ParticleManager {

    private final BlazesWildKits plugin;
    private final Map<String, ParticleTrail> trails = new LinkedHashMap<>();
    private final Set<UUID> toggledOff = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Double> stepCounter = new ConcurrentHashMap<>();
    private BukkitTask task;

    public ParticleManager(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void load() {
        trails.clear();
        ConfigurationSection section = plugin.getConfigManager().getParticles().getConfigurationSection("trails");
        if (section == null) {
            registerDefaults();
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection trail = section.getConfigurationSection(id);
            if (trail == null) continue;
            Particle particle;
            try {
                particle = Particle.valueOf(trail.getString("particle", "FLAME").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                particle = Particle.FLAME;
            }
            trails.put(id.toLowerCase(Locale.ROOT), new ParticleTrail(
                    id.toLowerCase(Locale.ROOT),
                    trail.getString("display-name", id),
                    particle,
                    trail.getInt("amount", 4),
                    trail.getDouble("spacing", 0.35),
                    trail.getDouble("speed", 0.01),
                    trail.getBoolean("enabled", true)
            ));
        }
        if (trails.isEmpty()) {
            registerDefaults();
        }
    }

    private void registerDefaults() {
        add("flame", "Flame Trail", Particle.FLAME, 5, 0.3, 0.01);
        add("rainbow", "Rainbow Trail", Particle.DUST, 6, 0.25, 0.0);
        add("cloud", "Cloud Trail", Particle.CLOUD, 3, 0.4, 0.01);
        add("water", "Water Trail", Particle.DRIPPING_WATER, 4, 0.35, 0.0);
        add("lava", "Lava Trail", Particle.LAVA, 2, 0.45, 0.0);
        add("soul", "Soul Trail", Particle.SOUL, 4, 0.3, 0.01);
        add("magic", "Magic Trail", Particle.ENCHANT, 8, 0.25, 0.5);
        add("dragon", "Dragon Breath Trail", Particle.DRAGON_BREATH, 4, 0.35, 0.02);
        add("heart", "Heart Trail", Particle.HEART, 2, 0.5, 0.0);
        add("electric", "Electric Trail", Particle.ELECTRIC_SPARK, 5, 0.3, 0.05);
        add("cherry", "Cherry Trail", Particle.CHERRY_LEAVES, 4, 0.35, 0.0);
        add("leaf", "Leaf Trail", Particle.HAPPY_VILLAGER, 3, 0.4, 0.0);
        add("snow", "Snow Trail", Particle.SNOWFLAKE, 5, 0.3, 0.0);
        add("totem", "Totem Trail", Particle.TOTEM_OF_UNDYING, 4, 0.35, 0.2);
        add("end", "End Trail", Particle.PORTAL, 10, 0.25, 0.4);
    }

    private void add(String id, String name, Particle particle, int amount, double spacing, double speed) {
        trails.put(id, new ParticleTrail(id, name, particle, amount, spacing, speed, true));
    }

    public void startTask() {
        shutdown();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 2L, 2L);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (toggledOff.contains(player.getUniqueId())) continue;
            if (!player.isOnline() || player.isInvisible() || player.isDead()) continue;
            PlayerData data = plugin.getPlayerDataManager().get(player);
            String trailId = data.getActiveTrail();
            if (trailId == null || trailId.isBlank()) continue;
            ParticleTrail trail = trails.get(trailId.toLowerCase(Locale.ROOT));
            if (trail == null || !trail.isEnabled()) continue;

            double walked = stepCounter.getOrDefault(player.getUniqueId(), 0.0) + 0.2;
            if (walked < trail.getSpacing()) {
                stepCounter.put(player.getUniqueId(), walked);
                continue;
            }
            stepCounter.put(player.getUniqueId(), 0.0);
            spawn(player, trail);
        }
    }

    public void spawn(Player player, ParticleTrail trail) {
        Location loc = player.getLocation().add(0, 0.2, 0);
        if (trail.getParticle() == Particle.DUST) {
            Particle.DustOptions options = new Particle.DustOptions(
                    Color.fromRGB(
                            (int) ((System.currentTimeMillis() / 20) % 255),
                            (int) ((System.currentTimeMillis() / 40) % 255),
                            (int) ((System.currentTimeMillis() / 60) % 255)
                    ), 1.2f);
            player.getWorld().spawnParticle(Particle.DUST, loc, trail.getAmount(), 0.15, 0.1, 0.15, trail.getSpeed(), options);
        } else {
            player.getWorld().spawnParticle(trail.getParticle(), loc, trail.getAmount(), 0.15, 0.1, 0.15, trail.getSpeed());
        }
    }

    public void preview(Player player, String trailId) {
        getTrail(trailId).ifPresent(trail -> {
            for (int i = 0; i < 12; i++) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> spawn(player, trail), i * 2L);
            }
            plugin.getMessageService().send(player, "trail-preview", Map.of("trail", trail.getDisplayName()));
        });
    }

    public void toggle(Player player) {
        UUID id = player.getUniqueId();
        if (toggledOff.remove(id)) {
            plugin.getMessageService().send(player, "trail-enabled");
        } else {
            toggledOff.add(id);
            plugin.getMessageService().send(player, "trail-disabled");
        }
    }

    public void setActive(Player player, String trailId) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (trailId == null || trailId.equalsIgnoreCase("none")) {
            data.setActiveTrail(null);
            plugin.getMessageService().send(player, "trail-cleared");
            return;
        }
        Optional<ParticleTrail> trail = getTrail(trailId);
        if (trail.isEmpty()) {
            plugin.getMessageService().send(player, "trail-not-found");
            return;
        }
        if (!data.hasCosmetic(trailId) && !player.hasPermission("wildkits.admin")) {
            plugin.getMessageService().send(player, "trail-locked");
            return;
        }
        data.setActiveTrail(trail.get().getId());
        toggledOff.remove(player.getUniqueId());
        plugin.getMessageService().send(player, "trail-selected", Map.of("trail", trail.get().getDisplayName()));
        plugin.getPlayerDataManager().saveAsync(player.getUniqueId());
    }

    public void playDeathEffect(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        String effect = data.getActiveDeathEffect();
        if (effect == null) return;
        Location loc = player.getLocation().add(0, 1, 0);
        switch (effect.toLowerCase(Locale.ROOT)) {
            case "blood" -> player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 30, 0.4, 0.5, 0.4, 0.02);
            case "smoke" -> player.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 40, 0.5, 0.6, 0.5, 0.02);
            case "soul_burst" -> player.getWorld().spawnParticle(Particle.SOUL, loc, 40, 0.5, 0.6, 0.5, 0.05);
            case "explosion" -> player.getWorld().spawnParticle(Particle.EXPLOSION, loc, 2);
            default -> player.getWorld().spawnParticle(Particle.CLOUD, loc, 25, 0.4, 0.5, 0.4, 0.02);
        }
    }

    public void playVictoryEffect(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        String effect = data.getActiveVictoryEffect();
        if (effect == null) return;
        Location loc = player.getLocation().add(0, 1, 0);
        switch (effect.toLowerCase(Locale.ROOT)) {
            case "fireworks" -> player.getWorld().spawnParticle(Particle.FIREWORK, loc, 40, 0.5, 0.8, 0.5, 0.1);
            case "totem" -> player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 50, 0.5, 0.8, 0.5, 0.3);
            case "hearts" -> player.getWorld().spawnParticle(Particle.HEART, loc, 20, 0.6, 0.8, 0.6, 0.0);
            default -> player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 25, 0.5, 0.7, 0.5, 0.0);
        }
    }

    public Optional<ParticleTrail> getTrail(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(trails.get(id.toLowerCase(Locale.ROOT)));
    }

    public Collection<ParticleTrail> getTrails() {
        return Collections.unmodifiableCollection(trails.values());
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
