package com.blaze.wildkits.util;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chaos-style animation frames from animations.yml.
 * Supports placeholders like {@code %animation:title%} and {@code %animation:server%}.
 */
public final class AnimationService {

    private static final Pattern ANIMATION_PATTERN = Pattern.compile("%animation:([a-zA-Z0-9_-]+)%");

    private final BlazesWildKits plugin;
    private final Map<String, Animation> animations = new LinkedHashMap<>();
    private final AtomicInteger globalTick = new AtomicInteger();

    public AnimationService(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void load() {
        animations.clear();
        FileConfiguration yaml = plugin.getConfigManager().getAnimations();
        ConfigurationSection root = yaml.getConfigurationSection("animations");
        if (root == null) {
            // Backwards-compatible defaults
            animations.put("title", new Animation(List.of(
                    "<gradient:#FF4500:#FFD700><bold>Blaze's WildKits</bold></gradient>",
                    "<gradient:#FFD700:#FF4500><bold>Blaze's WildKits</bold></gradient>",
                    "<gradient:#FF4500:#FFAA00><bold>WildKits</bold></gradient>"
            ), 8));
            animations.put("server", new Animation(List.of(
                    "<gradient:#FF4500:#FFD700>play.blaze.network</gradient>",
                    "<gray>play.blaze.network</gray>"
            ), 20));
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) continue;
            List<String> frames = section.getStringList("frames");
            if (frames.isEmpty()) {
                frames = section.getStringList("animation");
            }
            if (frames.isEmpty()) continue;
            int interval = Math.max(1, section.getInt("interval-ticks", 10));
            animations.put(key.toLowerCase(Locale.ROOT), new Animation(List.copyOf(frames), interval));
        }
    }

    public void tick() {
        globalTick.incrementAndGet();
    }

    public String currentFrame(String name) {
        if (name == null) return "";
        Animation animation = animations.get(name.toLowerCase(Locale.ROOT));
        if (animation == null || animation.frames.isEmpty()) return "";
        int idx = Math.floorDiv(globalTick.get(), animation.intervalTicks) % animation.frames.size();
        return animation.frames.get(idx);
    }

    public String apply(String input) {
        if (input == null || input.isEmpty() || !input.contains("%animation:")) {
            return input == null ? "" : input;
        }
        Matcher matcher = ANIMATION_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer(input.length() + 32);
        while (matcher.find()) {
            String frame = Matcher.quoteReplacement(currentFrame(matcher.group(1)));
            matcher.appendReplacement(buffer, frame);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public Map<String, Animation> getAnimations() {
        return Collections.unmodifiableMap(animations);
    }

    public record Animation(List<String> frames, int intervalTicks) {}
}
