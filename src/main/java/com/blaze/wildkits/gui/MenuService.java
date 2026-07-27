package com.blaze.wildkits.gui;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.kit.KitDefinition;
import com.blaze.wildkits.kit.SmartKitGenerator;
import com.blaze.wildkits.particle.ParticleTrail;
import com.blaze.wildkits.player.PlayerData;
import com.blaze.wildkits.shop.ShopItem;
import com.blaze.wildkits.util.ItemBuilder;
import com.blaze.wildkits.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MenuService {

    private static final int SIZE = 54;
    private final BlazesWildKits plugin;
    private final Map<UUID, GuiSession> sessions = new HashMap<>();

    public MenuService(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public GuiSession session(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), id -> new GuiSession());
    }

    public void clear(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public void openMain(Player player) {
        GuiSession session = session(player);
        session.setType(GuiType.MAIN);
        session.clearActions();
        Inventory inv = Bukkit.createInventory(player, SIZE, TextUtil.parse("<gold><bold>Blaze's WildKits</bold></gold>"));
        fillBorder(inv, animatedBorder());

        set(inv, session, 20, new ItemBuilder(Material.CHEST).name("<aqua>Kits").lore("<gray>Browse all kits").glow().build(), "open:kits");
        set(inv, session, 21, new ItemBuilder(Material.NETHER_STAR).name("<light_purple>Random Kit").lore("<gray>Receive a random kit now").build(), "action:random");
        set(inv, session, 22, new ItemBuilder(Material.ENDER_EYE).name("<green>Daily Reward").lore("<gray>Claim daily coins + kit").build(), "action:daily");
        set(inv, session, 23, new ItemBuilder(Material.EMERALD).name("<gold>Shop").lore("<gray>Premium cosmetics").build(), "open:shop");
        set(inv, session, 24, new ItemBuilder(Material.BLAZE_POWDER).name("<red>Cosmetics").lore("<gray>Trails & effects").build(), "open:particles");
        set(inv, session, 29, new ItemBuilder(Material.GOLDEN_APPLE).name("<yellow>Favorites").lore("<gray>Your favorite kits").build(), "open:favorites");
        set(inv, session, 30, new ItemBuilder(Material.CLOCK).name("<white>Recent").lore("<gray>Recently used kits").build(), "open:recent");
        set(inv, session, 31, new ItemBuilder(Material.BOOK).name("<gold>Quests").lore("<gray>Daily / Weekly / Monthly / Lifetime").build(), "action:quests");
        set(inv, session, 32, new ItemBuilder(Material.COMPASS).name("<aqua>Categories").lore("<gray>Browse by category").build(), "open:categories");
        set(inv, session, 33, new ItemBuilder(Material.ENDER_PEARL).name("<green>Spawn").lore("<gray>Teleport to global spawn").build(), "action:spawn");
        set(inv, session, 40, new ItemBuilder(Material.AMETHYST_SHARD).name("<light_purple>Reroll Kit")
                .lore("<gray>Uses 1 kit reroll", "<aqua>Owned: " + plugin.getPlayerDataManager().get(player).getKitRerolls())
                .build(), "action:reroll");

        playOpen(player);
        player.openInventory(inv);
        animateTitle(player);
    }

    public void openKits(Player player, List<KitDefinition> kits, String title, GuiType type) {
        GuiSession session = session(player);
        session.setType(type);
        session.clearActions();

        int page = session.getPage();
        int perPage = 28;
        int maxPage = Math.max(0, (kits.size() - 1) / perPage);
        if (page > maxPage) {
            page = maxPage;
            session.setPage(page);
        }

        Inventory inv = Bukkit.createInventory(player, SIZE, TextUtil.parse(title + " <dark_gray>(" + (page + 1) + "/" + (maxPage + 1) + ")"));
        fillBorder(inv, Material.GRAY_STAINED_GLASS_PANE);

        int[] slots = contentSlots();
        int start = page * perPage;
        for (int i = 0; i < slots.length; i++) {
            int index = start + i;
            if (index >= kits.size()) break;
            KitDefinition kit = kits.get(index);
            PlayerData data = plugin.getPlayerDataManager().get(player);
            List<String> lore = new ArrayList<>(kit.loreLines());
            lore.add("");
            lore.add(plugin.getKitManager().canUse(player, kit) ? "<green>Click to select" : "<red>Locked");
            lore.add(data.isFavorite(kit.getId()) ? "<yellow>Shift-click to unfavorite" : "<gray>Shift-click to favorite");
            lore.add("<aqua>Right-click to preview");
            ItemBuilder builder = new ItemBuilder(kit.getIcon()).name(kit.getDisplayName()).lore(lore);
            if (data.getCurrentKit() != null && data.getCurrentKit().equalsIgnoreCase(kit.getId())) {
                builder.glow();
            }
            set(inv, session, slots[i], builder.build(), "kit:" + kit.getId());
        }

        set(inv, session, 45, new ItemBuilder(Material.ARROW).name("<yellow>Previous").build(), "page:prev");
        set(inv, session, 49, new ItemBuilder(Material.BARRIER).name("<red>Back").build(), "open:main");
        set(inv, session, 53, new ItemBuilder(Material.ARROW).name("<yellow>Next").build(), "page:next");
        playOpen(player);
        player.openInventory(inv);
    }

    public void openCategories(Player player) {
        GuiSession session = session(player);
        session.setType(GuiType.CATEGORIES);
        session.clearActions();
        Inventory inv = Bukkit.createInventory(player, SIZE, TextUtil.parse("<aqua>Kit Categories"));
        fillBorder(inv, Material.CYAN_STAINED_GLASS_PANE);
        int[] slots = contentSlots();
        List<String> categories = plugin.getKitManager().getCategories();
        for (int i = 0; i < categories.size() && i < slots.length; i++) {
            String category = categories.get(i);
            set(inv, session, slots[i], new ItemBuilder(Material.BOOK)
                    .name("<yellow>" + category)
                    .lore("<gray>Browse " + category + " kits")
                    .build(), "category:" + category);
        }
        set(inv, session, 49, new ItemBuilder(Material.BARRIER).name("<red>Back").build(), "open:main");
        playOpen(player);
        player.openInventory(inv);
    }

    public void openPreview(Player player, KitDefinition kit) {
        GuiSession session = session(player);
        session.setType(GuiType.PREVIEW);
        session.setPreviewKit(kit.getId());
        session.clearActions();
        Inventory inv = Bukkit.createInventory(player, SIZE, TextUtil.parse("<white>Preview: " + kit.getDisplayName()));
        fillBorder(inv, Material.PURPLE_STAINED_GLASS_PANE);

        SmartKitGenerator.GeneratedKit generated = plugin.getKitManager().preview(kit);
        List<ItemStack> items = generated.allPreviewItems();
        int[] slots = contentSlots();
        for (int i = 0; i < items.size() && i < slots.length; i++) {
            inv.setItem(slots[i], items.get(i));
        }
        set(inv, session, 48, new ItemBuilder(Material.LIME_CONCRETE).name("<green>Select Kit").build(), "kit:" + kit.getId());
        set(inv, session, 49, new ItemBuilder(Material.BARRIER).name("<red>Back").build(), "open:kits");
        set(inv, session, 50, new ItemBuilder(Material.ENDER_EYE).name("<aqua>Reroll Preview").build(), "preview:" + kit.getId());
        playOpen(player);
        player.openInventory(inv);
    }

    public void openShop(Player player) {
        GuiSession session = session(player);
        session.setType(GuiType.SHOP);
        session.clearActions();
        Inventory inv = Bukkit.createInventory(player, SIZE, TextUtil.parse("<gradient:#FF4500:#FFD700><bold>WildKits Shop</bold></gradient>"));
        fillBorder(inv, animatedBorder());

        PlayerData data = plugin.getPlayerDataManager().get(player);
        set(inv, session, 4, new ItemBuilder(Material.GOLD_INGOT)
                .name("<gold>Balance: <white>" + data.getCoins() + " coins")
                .lore(
                        "<gray>Rerolls: <aqua>" + data.getKitRerolls(),
                        "<gray>Multiplier: <yellow>x" + String.format("%.1f", data.getCoinMultiplier()),
                        "<gray>Wins: <aqua>" + data.getWins()
                ).glow().build(), "none");

        set(inv, session, 19, new ItemBuilder(Material.BLAZE_POWDER).name("<red>Trails").lore("<gray>Particle trails").build(), "shopcat:Trails");
        set(inv, session, 20, new ItemBuilder(Material.IRON_SWORD).name("<red>Kill Effects").lore("<gray>On-kill effects").build(), "shopcat:Effects");
        set(inv, session, 21, new ItemBuilder(Material.SKELETON_SKULL).name("<dark_gray>Death Effects").lore("<gray>Death animations").build(), "shopcat:Effects");
        set(inv, session, 22, new ItemBuilder(Material.FIREWORK_ROCKET).name("<light_purple>Victory Effects").lore("<gray>Win celebrations").build(), "shopcat:Effects");
        set(inv, session, 23, new ItemBuilder(Material.PAPER).name("<white>Titles").lore("<gray>Display titles").build(), "shopcat:Titles");
        set(inv, session, 24, new ItemBuilder(Material.NAME_TAG).name("<yellow>Prefixes & Tags").lore("<gray>Chat identity").build(), "shopcat:Tags");
        set(inv, session, 25, new ItemBuilder(Material.RED_DYE).name("<gold>Chat Colors").lore("<gray>Message colors").build(), "shopcat:Chat");

        set(inv, session, 28, new ItemBuilder(Material.NETHER_STAR).name("<aqua>Particles").lore("<gray>Extra particle cosmetics").build(), "shopcat:Trails");
        set(inv, session, 29, new ItemBuilder(Material.GLASS).name("<white>Spawn Cages").lore("<gray>Respawn cages").build(), "shopcat:Cages");
        set(inv, session, 30, new ItemBuilder(Material.ARROW).name("<yellow>Projectile Trails").lore("<gray>Arrow trails").build(), "shopcat:Projectiles");
        set(inv, session, 31, new ItemBuilder(Material.FEATHER).name("<white>Wing Particles").lore("<gray>Wing cosmetics").build(), "shopcat:Wings");
        set(inv, session, 32, new ItemBuilder(Material.CHEST).name("<gold>Crates & Keys").lore("<gray>Buy crate keys").build(), "shopcat:Crates");
        set(inv, session, 33, new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("<green>Boosters").lore("<gray>Coin multipliers").build(), "shopcat:Boosters");
        set(inv, session, 34, new ItemBuilder(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE).name("<light_purple>Kit Rerolls").lore("<gray>Extra random kits").build(), "shopcat:Rerolls");

        set(inv, session, 40, new ItemBuilder(Material.DIAMOND_SWORD).name("<aqua>Premium Kits").lore("<gray>Unlock kits").build(), "shopcat:Kits");
        set(inv, session, 49, new ItemBuilder(Material.BARRIER).name("<red>Back").build(), "open:main");
        playOpen(player);
        player.openInventory(inv);
    }

    public void openShopCategory(Player player, String category) {
        GuiSession session = session(player);
        session.setType(GuiType.SHOP_CATEGORY);
        session.setCategory(category);
        session.clearActions();
        List<ShopItem> items = plugin.getShopManager().getByCategory(category);
        Inventory inv = Bukkit.createInventory(player, SIZE, TextUtil.parse("<gold>Shop: <white>" + category));
        fillBorder(inv, Material.GREEN_STAINED_GLASS_PANE);
        int[] slots = contentSlots();
        PlayerData data = plugin.getPlayerDataManager().get(player);
        for (int i = 0; i < items.size() && i < slots.length; i++) {
            ShopItem item = items.get(i);
            boolean owned = plugin.getShopManager().owns(data, item);
            set(inv, session, slots[i], new ItemBuilder(item.getIcon())
                    .name(item.getDisplayName())
                    .lore(
                            "<gray>" + item.getDescription(),
                            "",
                            "<white>Price: <gold>" + item.getPrice(),
                            owned ? "<green>Owned" : "<yellow>Click to buy"
                    ).build(), "buy:" + item.getId());
        }
        set(inv, session, 49, new ItemBuilder(Material.BARRIER).name("<red>Back").build(), "open:shop");
        playOpen(player);
        player.openInventory(inv);
    }

    public void openParticles(Player player) {
        GuiSession session = session(player);
        session.setType(GuiType.PARTICLES);
        session.clearActions();
        Inventory inv = Bukkit.createInventory(player, SIZE, TextUtil.parse("<red>Particle Trails"));
        fillBorder(inv, Material.ORANGE_STAINED_GLASS_PANE);
        int[] slots = contentSlots();
        List<ParticleTrail> trails = new ArrayList<>(plugin.getParticleManager().getTrails());
        PlayerData data = plugin.getPlayerDataManager().get(player);
        for (int i = 0; i < trails.size() && i < slots.length; i++) {
            ParticleTrail trail = trails.get(i);
            boolean owned = data.hasCosmetic(trail.getId()) || player.hasPermission("wildkits.admin");
            boolean active = trail.getId().equalsIgnoreCase(data.getActiveTrail());
            ItemBuilder builder = new ItemBuilder(Material.BLAZE_POWDER)
                    .name(trail.getDisplayName())
                    .lore(
                            owned ? "<green>Unlocked" : "<red>Locked (buy in shop)",
                            active ? "<aqua>Active" : "<gray>Click to equip",
                            "<yellow>Right-click to preview"
                    );
            if (active) builder.glow();
            set(inv, session, slots[i], builder.build(), "trail:" + trail.getId());
        }
        set(inv, session, 45, new ItemBuilder(Material.GRAY_DYE).name("<gray>Clear Trail").build(), "trail:none");
        set(inv, session, 47, new ItemBuilder(Material.LEVER).name("<yellow>Toggle Trails").build(), "action:toggle_trail");
        set(inv, session, 49, new ItemBuilder(Material.BARRIER).name("<red>Back").build(), "open:main");
        playOpen(player);
        player.openInventory(inv);
    }

    public void handleAction(Player player, String action, boolean rightClick, boolean shift) {
        if (action == null || action.equals("none")) return;
        GuiSession session = session(player);

        if (action.startsWith("open:")) {
            switch (action.substring(5)) {
                case "main" -> openMain(player);
                case "kits" -> {
                    session.setPage(0);
                    openKits(player, new ArrayList<>(plugin.getKitManager().getKits()), "<aqua>All Kits", GuiType.KITS);
                }
                case "shop" -> openShop(player);
                case "particles" -> openParticles(player);
                case "favorites" -> {
                    PlayerData data = plugin.getPlayerDataManager().get(player);
                    List<KitDefinition> fav = data.getFavorites().stream()
                            .map(id -> plugin.getKitManager().getKit(id).orElse(null))
                            .filter(k -> k != null)
                            .toList();
                    openKits(player, fav, "<yellow>Favorites", GuiType.FAVORITES);
                }
                case "recent" -> {
                    PlayerData data = plugin.getPlayerDataManager().get(player);
                    List<KitDefinition> recent = data.getRecentKits().stream()
                            .map(id -> plugin.getKitManager().getKit(id).orElse(null))
                            .filter(k -> k != null)
                            .toList();
                    openKits(player, recent, "<white>Recent Kits", GuiType.RECENT);
                }
                case "categories" -> openCategories(player);
            }
            return;
        }

        if (action.equals("page:prev")) {
            session.setPage(session.getPage() - 1);
            reopenList(player, session);
            return;
        }
        if (action.equals("page:next")) {
            session.setPage(session.getPage() + 1);
            reopenList(player, session);
            return;
        }

        if (action.equals("action:random")) {
            player.closeInventory();
            plugin.getKitManager().giveRandomKit(player);
            return;
        }
        if (action.equals("action:reroll")) {
            if (!plugin.getPlayerDataManager().get(player).consumeKitReroll()) {
                plugin.getMessageService().send(player, "no-rerolls");
                return;
            }
            player.closeInventory();
            plugin.getKitManager().giveRandomKit(player);
            plugin.getPlayerDataManager().saveAsync(player.getUniqueId());
            return;
        }
        if (action.equals("action:daily")) {
            player.closeInventory();
            plugin.getCoinManager().claimDaily(player);
            plugin.getKitManager().giveRandomKit(player);
            return;
        }
        if (action.equals("action:spawn")) {
            player.closeInventory();
            if (!plugin.getSpawnManager().teleport(player)) {
                plugin.getMessageService().send(player, "spawn-not-set");
            } else {
                plugin.getMessageService().send(player, "spawn-teleport");
            }
            return;
        }
        if (action.equals("action:search_help")) {
            plugin.getMessageService().send(player, "search-help");
            return;
        }
        if (action.equals("action:toggle_trail")) {
            plugin.getParticleManager().toggle(player);
            return;
        }
        if (action.equals("action:quests")) {
            player.closeInventory();
            plugin.getQuestManager().openGui(player);
            return;
        }

        if (action.startsWith("category:")) {
            String category = action.substring(9);
            session.setPage(0);
            openKits(player, plugin.getKitManager().getByCategory(category), "<aqua>" + category, GuiType.KITS);
            return;
        }

        if (action.startsWith("kit:")) {
            String id = action.substring(4);
            plugin.getKitManager().getKit(id).ifPresent(kit -> {
                if (rightClick) {
                    openPreview(player, kit);
                    return;
                }
                if (shift) {
                    plugin.getPlayerDataManager().get(player).toggleFavorite(kit.getId());
                    plugin.getPlayerDataManager().saveAsync(player.getUniqueId());
                    plugin.getMessageService().send(player, "favorite-toggled", Map.of("kit", kit.getDisplayName()));
                    reopenList(player, session);
                    return;
                }
                if (!plugin.getConfigManager().isAutoRandomKits() || player.hasPermission("wildkits.admin")) {
                    if (!plugin.getKitManager().canUse(player, kit)) {
                        plugin.getMessageService().send(player, "kit-locked");
                        return;
                    }
                    player.closeInventory();
                    plugin.getKitManager().giveKit(player, kit);
                } else {
                    plugin.getMessageService().send(player, "kit-auto-mode");
                }
            });
            return;
        }

        if (action.startsWith("preview:")) {
            plugin.getKitManager().getKit(action.substring(8)).ifPresent(kit -> openPreview(player, kit));
            return;
        }

        if (action.startsWith("shopcat:")) {
            openShopCategory(player, action.substring(8));
            return;
        }

        if (action.startsWith("buy:")) {
            plugin.getShopManager().get(action.substring(4)).ifPresent(item -> {
                if (plugin.getShopManager().purchase(player, item)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f);
                    openShopCategory(player, item.getCategory());
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
                }
            });
            return;
        }

        if (action.startsWith("trail:")) {
            String id = action.substring(6);
            if (rightClick && !id.equals("none")) {
                plugin.getParticleManager().preview(player, id);
                return;
            }
            plugin.getParticleManager().setActive(player, id);
            openParticles(player);
        }
    }

    private void reopenList(Player player, GuiSession session) {
        switch (session.getType()) {
            case FAVORITES -> handleAction(player, "open:favorites", false, false);
            case RECENT -> handleAction(player, "open:recent", false, false);
            case SEARCH -> openKits(player, plugin.getKitManager().search(session.getSearch()),
                    "<white>Search", GuiType.SEARCH);
            default -> {
                if (session.getCategory() != null && session.getType() == GuiType.KITS) {
                    openKits(player, plugin.getKitManager().getByCategory(session.getCategory()),
                            "<aqua>" + session.getCategory(), GuiType.KITS);
                } else {
                    openKits(player, new ArrayList<>(plugin.getKitManager().getKits()), "<aqua>All Kits", GuiType.KITS);
                }
            }
        }
    }

    public void openSearch(Player player, String query) {
        GuiSession session = session(player);
        session.setSearch(query);
        session.setPage(0);
        openKits(player, plugin.getKitManager().search(query), "<white>Search: " + query, GuiType.SEARCH);
    }

    private void set(Inventory inv, GuiSession session, int slot, ItemStack item, String action) {
        inv.setItem(slot, item);
        session.setAction(slot, action);
    }

    private void fillBorder(Inventory inv, Material material) {
        ItemStack pane = new ItemBuilder(material).name(" ").build();
        for (int i = 0; i < SIZE; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, pane);
            }
        }
    }

    private Material animatedBorder() {
        Material[] materials = {
                Material.ORANGE_STAINED_GLASS_PANE,
                Material.YELLOW_STAINED_GLASS_PANE,
                Material.RED_STAINED_GLASS_PANE
        };
        return materials[(int) (System.currentTimeMillis() / 400 % materials.length)];
    }

    private int[] contentSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    private void playOpen(Player player) {
        String soundName = plugin.getConfigManager().getAnimations().getString("gui-open-sound", "UI_BUTTON_CLICK");
        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName), 0.6f, 1.2f);
        } catch (Exception e) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
        }
    }

    private void animateTitle(Player player) {
        List<String> frames = plugin.getConfigManager().getAnimations().getStringList("gui-title-frames");
        if (frames.isEmpty()) return;
        for (int i = 0; i < frames.size(); i++) {
            final int frame = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                if (player.getOpenInventory().getTopInventory().getSize() != SIZE) return;
                // Soft action-bar animation instead of inventory rename spam
                player.sendActionBar(TextUtil.parse(frames.get(frame)));
            }, i * 4L);
        }
    }
}
