package com.blaze.wildkits.kit;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import com.blaze.wildkits.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class KitManager {

    private final BlazesWildKits plugin;
    private final SmartKitGenerator generator;
    private final KitAnnounceService announceService;
    private final Map<String, KitDefinition> kits = new LinkedHashMap<>();

    public KitManager(BlazesWildKits plugin) {
        this.plugin = plugin;
        this.generator = new SmartKitGenerator(plugin);
        this.announceService = new KitAnnounceService(plugin);
    }

    public void load() {
        kits.clear();
        for (KitDefinition def : DefaultKits.createAll()) {
            kits.put(def.getId().toLowerCase(Locale.ROOT), def);
        }

        FileConfiguration yaml = plugin.getConfigManager().getKits();
        ConfigurationSection section = yaml.getConfigurationSection("kits");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection kit = section.getConfigurationSection(id);
                if (kit == null) continue;
                kits.put(id.toLowerCase(Locale.ROOT), fromSection(id, kit));
            }
        }
        plugin.getLogger().info("Loaded " + kits.size() + " kit templates.");
    }

    private KitDefinition fromSection(String id, ConfigurationSection kit) {
        String display = kit.getString("display-name", id);
        String description = kit.getString("description", "");
        KitRarity rarity = KitRarity.fromString(kit.getString("rarity", "COMMON"));
        Material icon = Material.matchMaterial(kit.getString("icon", "CHEST"));
        String permission = kit.getString("permission", "wildkits.kit." + id);
        String category = kit.getString("category", "General");
        boolean unlocked = kit.getBoolean("unlocked-by-default", false);
        int price = kit.getInt("price", 0);
        String theme = kit.getString("theme", "balanced");
        boolean ranged = kit.getBoolean("prefer-ranged", false);
        boolean magic = kit.getBoolean("prefer-magic", false);
        List<String> tags = kit.getStringList("tags");
        if (tags.isEmpty()) {
            tags = List.of(category.toLowerCase(Locale.ROOT), rarity.name().toLowerCase(Locale.ROOT));
        }
        SmartGenProfile profile = SmartGenProfile.forRarity(rarity).withTheme(theme, ranged, magic);
        return new KitDefinition(id.toLowerCase(Locale.ROOT), display, description, rarity, icon,
                permission, category, unlocked, price, tags, profile);
    }

    public Collection<KitDefinition> getKits() {
        return Collections.unmodifiableCollection(kits.values());
    }

    public Optional<KitDefinition> getKit(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(kits.get(id.toLowerCase(Locale.ROOT)));
    }

    public List<KitDefinition> getByCategory(String category) {
        return kits.values().stream()
                .filter(k -> k.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return kits.values().stream()
                .map(KitDefinition::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<KitDefinition> search(String query) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
        return kits.values().stream()
                .filter(k -> k.getId().contains(q)
                        || TextUtil.strip(k.getDisplayName()).toLowerCase(Locale.ROOT).contains(q)
                        || k.getDescription().toLowerCase(Locale.ROOT).contains(q)
                        || k.getTags().stream().anyMatch(t -> t.contains(q)))
                .collect(Collectors.toList());
    }

    public boolean canUse(Player player, KitDefinition kit) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (kit.isUnlockedByDefault() || data.hasUnlockedKit(kit.getId())) {
            return true;
        }
        if (player.hasPermission("wildkits.kit.*") || player.hasPermission(kit.getPermission())) {
            return true;
        }
        return plugin.getLuckPermsHook() != null
                && plugin.getLuckPermsHook().hasPermission(player, kit.getPermission());
    }

    public List<KitDefinition> getUsableKits(Player player) {
        List<KitDefinition> usable = new ArrayList<>(Math.min(64, kits.size()));
        for (KitDefinition kit : kits.values()) {
            if (canUse(player, kit)) {
                usable.add(kit);
            }
        }
        return usable;
    }

    public KitDefinition pickRandom(Player player) {
        List<KitDefinition> usable = getUsableKits(player);
        if (usable.isEmpty()) {
            return kits.values().stream().findFirst().orElse(null);
        }
        PlayerData data = plugin.getPlayerDataManager().get(player);
        List<String> recent = data.getRecentKits();
        String last = recent.isEmpty() ? null : recent.get(0);
        var weights = plugin.getConfigManager().getRarityWeights();
        double recentPenalty = plugin.getConfigManager().getConfig().getDouble("generation.recent-kit-penalty", 0.15);
        double lastPenalty = plugin.getConfigManager().getConfig().getDouble("generation.last-kit-penalty", 0.05);

        double total = 0;
        double[] cumulative = new double[usable.size()];
        for (int i = 0; i < usable.size(); i++) {
            KitDefinition kit = usable.get(i);
            double w = weights.getOrDefault(kit.getRarity(), kit.getRarity().getDefaultWeight());
            if (last != null && last.equalsIgnoreCase(kit.getId())) {
                w *= lastPenalty; // almost never identical twice in a row
            } else if (recent.stream().anyMatch(r -> r.equalsIgnoreCase(kit.getId()))) {
                int index = -1;
                for (int r = 0; r < recent.size(); r++) {
                    if (recent.get(r).equalsIgnoreCase(kit.getId())) {
                        index = r;
                        break;
                    }
                }
                // More recent => stronger penalty
                double factor = recentPenalty + (index * 0.05);
                w *= Math.min(0.85, Math.max(0.08, factor));
            }
            total += Math.max(0.001, w);
            cumulative[i] = total;
        }
        double roll = ThreadLocalRandom.current().nextDouble(total);
        for (int i = 0; i < usable.size(); i++) {
            if (roll <= cumulative[i]) {
                return usable.get(i);
            }
        }
        return usable.get(usable.size() - 1);
    }

    public SmartKitGenerator.GeneratedKit giveRandomKit(Player player) {
        return giveKit(player, pickRandom(player));
    }

    public SmartKitGenerator.GeneratedKit giveKit(Player player, KitDefinition kit) {
        if (kit == null) {
            return null;
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        SmartKitGenerator.GeneratedKit generated = generator.generate(kit);
        generator.apply(player.getInventory(), generated);

        PlayerData data = plugin.getPlayerDataManager().get(player);
        data.setCurrentKit(kit.getId());
        data.addRecentKit(kit.getId());
        plugin.getPlayerDataManager().saveAsync(player.getUniqueId());
        plugin.getScoreboardManager().update(player);

        announceService.announce(player, kit, generated.band());
        if (kit.getRarity() == KitRarity.LEGENDARY || kit.getRarity() == KitRarity.MYTHIC
                || kit.getRarity() == KitRarity.ULTIMATE) {
            plugin.getQuestManager().progress(player, "legendary_kit", 1);
        }
        return generated;
    }

    public SmartKitGenerator getGenerator() {
        return generator;
    }

    public SmartKitGenerator.GeneratedKit preview(KitDefinition kit) {
        return generator.generate(kit);
    }
}
