package com.blaze.wildkits.command;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.kit.KitDefinition;
import com.blaze.wildkits.player.PlayerData;
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
                if (!player.hasPermission("wildkits.spawn")) {
                    plugin.getMessageService().send(player, "no-permission");
                    return true;
                }
                if (!plugin.getSpawnManager().teleport(player)) {
                    plugin.getMessageService().send(player, "spawn-not-set");
                } else {
                    plugin.getMessageService().send(player, "spawn-teleport");
                }
            }
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

    private void sendHelp(CommandSender sender) {
        List<String> lines = List.of(
                "<gold><bold>Blaze's WildKits</bold></gold>",
                "<yellow>/wk</yellow> <gray>- Open main GUI",
                "<yellow>/wk kits</yellow> <gray>- Open kits",
                "<yellow>/wk shop</yellow> <gray>- Open shop",
                "<yellow>/wk particles</yellow> <gray>- Trails",
                "<yellow>/wk random</yellow> <gray>- Random kit",
                "<yellow>/wk preview <kit></yellow>",
                "<yellow>/wk search <query></yellow>",
                "<yellow>/wk spawn</yellow> <gray>- Teleport spawn",
                "<yellow>/wk setspawn</yellow> <gray>- Admin set spawn",
                "<yellow>/wk daily</yellow> <gray>- Daily reward",
                "<yellow>/wk stats</yellow>",
                "<yellow>/wk reload</yellow> <gray>- Admin reload"
        );
        for (String line : lines) {
            com.blaze.wildkits.util.TextUtil.send(sender, line);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("help", "kits", "gui", "shop", "particles", "trails", "random", "kit",
                    "preview", "search", "daily", "coins", "stats", "spawn", "setspawn", "reload",
                    "givecoins", "eventreward", "db", "dbstatus", "npc"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("kit") || args[0].equalsIgnoreCase("preview"))) {
            return filter(plugin.getKitManager().getKits().stream().map(KitDefinition::getId).collect(Collectors.toList()), args[1]);
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
