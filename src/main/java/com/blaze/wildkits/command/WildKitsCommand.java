package com.blaze.wildkits.command;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.crate.CrateRarity;
import com.blaze.wildkits.kit.KitDefinition;
import com.blaze.wildkits.player.PlayerData;
import com.blaze.wildkits.region.SelectionSession;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class WildKitsCommand implements CommandExecutor, TabCompleter {

    private final BlazesWildKits plugin;

    public WildKitsCommand(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Standalone /spawn
        if (command.getName().equalsIgnoreCase("spawn")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            return teleportSpawn(player);
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Players only.");
                return true;
            }
            plugin.getMenuService().openMain(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help" -> sendHelp(sender);
            case "reload" -> {
                if (!sender.hasPermission("wildkits.admin")) {
                    plugin.getMessageService().send(sender, "no-permission");
                    return true;
                }
                boolean ok = plugin.reloadPlugin();
                plugin.getMessageService().send(sender, ok ? "reloaded" : "reload-failed");
            }
            case "setspawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                if (!player.hasPermission("wildkits.admin")) {
                    plugin.getMessageService().send(player, "no-permission");
                    return true;
                }
                plugin.getSpawnManager().setSpawn(player.getLocation());
                plugin.getMessageService().send(player, "spawn-set");
            }
            case "spawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                return teleportSpawn(player);
            }
            case "setup" -> {
                if (!(sender instanceof Player player)) return true;
                if (!player.hasPermission("wildkits.admin")) {
                    plugin.getMessageService().send(player, "no-permission");
                    return true;
                }
                plugin.getSetupGui().open(player);
            }
            case "wand" -> {
                if (!(sender instanceof Player player)) return true;
                if (!player.hasPermission("wildkits.admin")) {
                    plugin.getMessageService().send(player, "no-permission");
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("save")) {
                    plugin.getRegionManager().saveSelection(player);
                    return true;
                }
                SelectionSession.Target target = SelectionSession.Target.NONE;
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("lobby")) target = SelectionSession.Target.LOBBY;
                    else if (args[1].equalsIgnoreCase("drop")) target = SelectionSession.Target.DROP;
                }
                plugin.getRegionManager().giveWand(player, target);
            }
            case "quests", "quest" -> {
                if (!(sender instanceof Player player)) return true;
                plugin.getQuestManager().openGui(player);
            }
            case "crate" -> handleCrate(sender, args);
            case "kits", "gui" -> {
                if (!(sender instanceof Player player)) return true;
                plugin.getMenuService().openMain(player);
            }
            case "shop" -> {
                if (!(sender instanceof Player player)) return true;
                if (!player.hasPermission("wildkits.shop")) {
                    plugin.getMessageService().send(player, "no-permission");
                    return true;
                }
                plugin.getMenuService().openShop(player);
            }
            case "particles", "trails" -> {
                if (!(sender instanceof Player player)) return true;
                if (!player.hasPermission("wildkits.cosmetics")) {
                    plugin.getMessageService().send(player, "no-permission");
                    return true;
                }
                plugin.getMenuService().openParticles(player);
            }
            case "random" -> {
                if (!(sender instanceof Player player)) return true;
                plugin.getKitManager().giveRandomKit(player);
            }
            case "kit" -> {
                if (!(sender instanceof Player player)) return true;
                if (args.length < 2) {
                    plugin.getMessageService().send(player, "usage-kit");
                    return true;
                }
                if (plugin.getConfigManager().isAutoRandomKits() && !player.hasPermission("wildkits.admin")) {
                    plugin.getMessageService().send(player, "kit-auto-mode");
                    return true;
                }
                plugin.getKitManager().getKit(args[1]).ifPresentOrElse(kit -> {
                    if (!plugin.getKitManager().canUse(player, kit)) {
                        plugin.getMessageService().send(player, "kit-locked");
                        return;
                    }
                    plugin.getKitManager().giveKit(player, kit);
                }, () -> plugin.getMessageService().send(player, "kit-not-found"));
            }
            case "preview" -> {
                if (!(sender instanceof Player player)) return true;
                if (args.length < 2) {
                    plugin.getMessageService().send(player, "usage-preview");
                    return true;
                }
                plugin.getKitManager().getKit(args[1]).ifPresentOrElse(
                        kit -> plugin.getMenuService().openPreview(player, kit),
                        () -> plugin.getMessageService().send(player, "kit-not-found")
                );
            }
            case "search" -> {
                if (!(sender instanceof Player player)) return true;
                if (args.length < 2) {
                    plugin.getMessageService().send(player, "search-help");
                    return true;
                }
                String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                plugin.getMenuService().openSearch(player, query);
            }
            case "daily" -> {
                if (!(sender instanceof Player player)) return true;
                plugin.getCoinManager().claimDaily(player);
            }
            case "coins", "balance" -> {
                if (args.length >= 2 && sender.hasPermission("wildkits.admin")) {
                    Player target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) {
                        plugin.getMessageService().send(sender, "player-not-found");
                        return true;
                    }
                    PlayerData data = plugin.getPlayerDataManager().get(target);
                    plugin.getMessageService().send(sender, "coins-other", Map.of(
                            "player", target.getName(),
                            "coins", String.valueOf(data.getCoins())
                    ));
                    return true;
                }
                if (!(sender instanceof Player player)) return true;
                PlayerData data = plugin.getPlayerDataManager().get(player);
                plugin.getMessageService().send(player, "coins-self", Map.of("coins", String.valueOf(data.getCoins())));
            }
            case "givecoins" -> {
                if (!sender.hasPermission("wildkits.admin")) {
                    plugin.getMessageService().send(sender, "no-permission");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("/wk givecoins <player> <amount>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    plugin.getMessageService().send(sender, "player-not-found");
                    return true;
                }
                long amount;
                try {
                    amount = Long.parseLong(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid amount.");
                    return true;
                }
                plugin.getPlayerDataManager().get(target).addCoins(amount);
                plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
                plugin.getMessageService().send(sender, "coins-given", Map.of(
                        "player", target.getName(),
                        "coins", String.valueOf(amount)
                ));
            }
            case "stats" -> {
                if (!(sender instanceof Player player)) return true;
                PlayerData data = plugin.getPlayerDataManager().get(player);
                plugin.getMessageService().send(player, "stats", Map.of(
                        "kills", String.valueOf(data.getKills()),
                        "deaths", String.valueOf(data.getDeaths()),
                        "kd", String.valueOf(data.getKd()),
                        "coins", String.valueOf(data.getCoins()),
                        "level", String.valueOf(data.getLevel()),
                        "streak", String.valueOf(data.getKillstreak()),
                        "kit", data.getCurrentKit() == null ? "None" : data.getCurrentKit()
                ));
            }
            case "npc" -> {
                if (!sender.hasPermission("wildkits.npc")) {
                    plugin.getMessageService().send(sender, "no-permission");
                    return true;
                }
                if (!(sender instanceof Player player)) return true;
                if (plugin.getCitizensHook() == null || !plugin.getCitizensHook().isEnabled()) {
                    plugin.getMessageService().send(player, "citizens-missing");
                    return true;
                }
                String name = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "WildKits";
                plugin.getCitizensHook().createNpc(player, name);
            }
            case "showkit" -> {
                if (!sender.hasPermission("wildkits.admin")) {
                    plugin.getMessageService().send(sender, "no-permission");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("Usage: /wk showkit <true|false>");
                    sender.sendMessage("Current: " + plugin.getConfigManager().isShowKit());
                    return true;
                }
                boolean enabled = Boolean.parseBoolean(args[1]);
                plugin.getConfigManager().setShowKit(enabled);
                plugin.getScoreboardManager().reload();
                plugin.getMessageService().send(sender, enabled ? "showkit-enabled" : "showkit-disabled");
            }
            case "db", "dbstatus" -> {
                if (!sender.hasPermission("wildkits.admin")) {
                    plugin.getMessageService().send(sender, "no-permission");
                    return true;
                }
                var db = plugin.getDatabaseManager();
                sender.sendMessage("Database type: " + db.getType());
                sender.sendMessage("Connected: " + db.isConnected());
                sender.sendMessage("Memory fallback: " + db.isMemoryFallback());
                if (!db.getLastError().isBlank()) {
                    sender.sendMessage("Last error: " + db.getLastError());
                }
            }
            case "eventreward" -> {
                if (!sender.hasPermission("wildkits.admin")) {
                    plugin.getMessageService().send(sender, "no-permission");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("/wk eventreward <player> <amount>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    plugin.getMessageService().send(sender, "player-not-found");
                    return true;
                }
                long amount;
                try {
                    amount = Long.parseLong(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid amount.");
                    return true;
                }
                plugin.getPlayerDataManager().get(target).addCoins(amount);
                plugin.getPlayerDataManager().saveAsync(target.getUniqueId());
                plugin.getMessageService().send(target, "event-reward", Map.of("coins", String.valueOf(amount)));
                plugin.getMessageService().send(sender, "coins-given", Map.of(
                        "player", target.getName(),
                        "coins", String.valueOf(amount)
                ));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private boolean teleportSpawn(Player player) {
        if (!player.hasPermission("wildkits.spawn")) {
            plugin.getMessageService().send(player, "no-permission");
            return true;
        }
        if (!plugin.getSpawnManager().teleport(player)) {
            plugin.getMessageService().send(player, "spawn-not-set");
        } else {
            plugin.getMessageService().send(player, "spawn-teleport");
        }
        return true;
    }

    private void handleCrate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("wildkits.admin")) {
            plugin.getMessageService().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("/wk crate <create|set|key|edit|delete|list> ...");
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "create" -> {
                if (args.length < 4) {
                    sender.sendMessage("/wk crate create <id> <common|rare|epic|legendary|mythic>");
                    return;
                }
                CrateRarity rarity = CrateRarity.from(args[3]);
                plugin.getCrateManager().create(args[2], rarity);
                plugin.getMessageService().send(sender, "crate-created", Map.of("id", args[2], "rarity", rarity.name()));
            }
            case "set" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Players only.");
                    return;
                }
                if (args.length < 3) {
                    sender.sendMessage("/wk crate set <id>");
                    return;
                }
                plugin.getCrateManager().get(args[2]).ifPresentOrElse(crate -> {
                    plugin.getCrateManager().setLocation(args[2], player.getLocation().getBlock().getLocation());
                    plugin.getMessageService().send(player, "crate-set", Map.of("id", args[2]));
                }, () -> plugin.getMessageService().send(player, "crate-not-found"));
            }
            case "key" -> {
                if (args.length < 5) {
                    sender.sendMessage("/wk crate key <player> <rarity> <amount>");
                    return;
                }
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    plugin.getMessageService().send(sender, "player-not-found");
                    return;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid amount.");
                    return;
                }
                plugin.getCrateManager().giveKey(target, args[3], amount);
                plugin.getMessageService().send(sender, "crate-key-given", Map.of(
                        "player", target.getName(),
                        "rarity", args[3],
                        "amount", String.valueOf(amount)
                ));
            }
            case "edit" -> {
                if (!(sender instanceof Player player)) return;
                if (args.length < 3) {
                    sender.sendMessage("/wk crate edit <id>");
                    return;
                }
                plugin.getCrateManager().get(args[2]).ifPresentOrElse(
                        crate -> plugin.getCrateManager().openEditor(player, crate),
                        () -> plugin.getMessageService().send(player, "crate-not-found")
                );
            }
            case "delete" -> {
                if (args.length < 3) {
                    sender.sendMessage("/wk crate delete <id>");
                    return;
                }
                if (plugin.getCrateManager().delete(args[2])) {
                    plugin.getMessageService().send(sender, "crate-deleted", Map.of("id", args[2]));
                } else {
                    plugin.getMessageService().send(sender, "crate-not-found");
                }
            }
            case "list" -> {
                plugin.getCrateManager().getCrates().forEach(c ->
                        sender.sendMessage(c.getId() + " [" + c.getRarity() + "]"));
            }
            default -> sender.sendMessage("/wk crate <create|set|key|edit|delete|list>");
        }
    }

    private void sendHelp(CommandSender sender) {
        List<String> lines = List.of(
                "<gold><bold>Blaze's WildKits</bold></gold>",
                "<yellow>/wk</yellow> <gray>- Open main GUI",
                "<yellow>/wk setup</yellow> <gray>- Admin setup wizard",
                "<yellow>/wk wand [lobby|drop|save]</yellow>",
                "<yellow>/wk quests</yellow> <gray>- Quest menu",
                "<yellow>/wk crate ...</yellow> <gray>- Crate admin",
                "<yellow>/wk shop</yellow> <gray>- Open shop",
                "<yellow>/spawn</yellow> <gray>- Lobby spawn",
                "<yellow>/wk setspawn</yellow> <gray>- Set lobby spawn",
                "<yellow>/wk showkit <true|false></yellow>",
                "<yellow>/wk reload</yellow> <gray>- Admin reload"
        );
        for (String line : lines) {
            com.blaze.wildkits.util.TextUtil.send(sender, line);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("spawn")) return List.of();
        if (args.length == 1) {
            return filter(List.of("help", "kits", "gui", "shop", "particles", "trails", "random", "kit",
                    "preview", "search", "daily", "coins", "stats", "spawn", "setspawn", "reload",
                    "givecoins", "eventreward", "showkit", "db", "dbstatus", "npc",
                    "setup", "wand", "quests", "quest", "crate"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("kit") || args[0].equalsIgnoreCase("preview"))) {
            return filter(plugin.getKitManager().getKits().stream().map(KitDefinition::getId).collect(Collectors.toList()), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("showkit")) {
            return filter(List.of("true", "false"), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("wand")) {
            return filter(List.of("lobby", "drop", "save"), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("crate")) {
            return filter(List.of("create", "set", "key", "edit", "delete", "list"), args[1]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("crate") && args[1].equalsIgnoreCase("create")) {
            return filter(List.of("common", "rare", "epic", "legendary", "mythic"), args[3]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("givecoins") || args[0].equalsIgnoreCase("coins"))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        }
        return new ArrayList<>();
    }

    private List<String> filter(List<String> options, String token) {
        String t = token.toLowerCase(Locale.ROOT);
        return options.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(t)).toList();
    }
}
