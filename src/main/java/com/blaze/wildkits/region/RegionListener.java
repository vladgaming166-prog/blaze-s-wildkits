package com.blaze.wildkits.region;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Legacy lobby/drop region listener (kept for backwards compatibility).
 * When a location is covered by the new Arena system, ArenaListener owns protections.
 */
public final class RegionListener implements Listener {

    private static final Set<EntityDamageEvent.DamageCause> LOBBY_BLOCKED = EnumSet.of(
            EntityDamageEvent.DamageCause.FALL,
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.LAVA,
            EntityDamageEvent.DamageCause.HOT_FLOOR,
            EntityDamageEvent.DamageCause.CONTACT,
            EntityDamageEvent.DamageCause.DROWNING,
            EntityDamageEvent.DamageCause.SUFFOCATION,
            EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
            EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
            EntityDamageEvent.DamageCause.MAGIC,
            EntityDamageEvent.DamageCause.POISON,
            EntityDamageEvent.DamageCause.WITHER,
            EntityDamageEvent.DamageCause.VOID,
            EntityDamageEvent.DamageCause.LIGHTNING,
            EntityDamageEvent.DamageCause.FREEZE,
            EntityDamageEvent.DamageCause.CAMPFIRE
    );

    private final BlazesWildKits plugin;
    private final Map<UUID, Boolean> wasInDrop = new ConcurrentHashMap<>();

    public RegionListener(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    private boolean arenaOwns(Location location) {
        if (plugin.getArenaManager() == null || location == null) return false;
        // Prefer arena system whenever any arena is configured
        if (plugin.getArenaManager().getArenas().isEmpty()) return false;
        return plugin.getArenaManager().isOnAnySpawnPlatform(location)
                || plugin.getArenaManager().isInAnyPlayable(location)
                || plugin.getArenaManager().findArenaAt(location) != null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWandInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("wildkits.admin")) return;
        ItemStack item = event.getItem();
        if (!plugin.getRegionManager().isWand(item)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        event.setCancelled(true);
        Location clicked = event.getClickedBlock().getLocation();
        SelectionSession session = plugin.getRegionManager().session(player);

        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK -> {
                session.setPos1(clicked);
                plugin.getMessageService().send(player, "wand-pos1", Map.of(
                        "x", String.valueOf(clicked.getBlockX()),
                        "y", String.valueOf(clicked.getBlockY()),
                        "z", String.valueOf(clicked.getBlockZ())
                ));
            }
            case RIGHT_CLICK_BLOCK -> {
                session.setPos2(clicked);
                plugin.getMessageService().send(player, "wand-pos2", Map.of(
                        "x", String.valueOf(clicked.getBlockX()),
                        "y", String.valueOf(clicked.getBlockY()),
                        "z", String.valueOf(clicked.getBlockZ())
                ));
            }
            default -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("wildkits.admin")) return;
        Location loc = event.getBlock().getLocation();
        if (arenaOwns(loc)) return;

        if (plugin.getRegionManager().isInLobby(loc)) {
            event.setCancelled(true);
            return;
        }

        if (plugin.getRegionManager().isInDrop(loc)) {
            if (!plugin.getRegionManager().isPlayerPlaced(loc)) {
                event.setCancelled(true);
                return;
            }
            plugin.getRegionManager().unmarkPlayerPlaced(loc);
            plugin.getQuestManager().progress(player, "break_placed", 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("wildkits.admin")) return;
        Location loc = event.getBlock().getLocation();
        if (arenaOwns(loc)) return;

        if (plugin.getRegionManager().isInLobby(loc)) {
            event.setCancelled(true);
            return;
        }

        if (plugin.getRegionManager().isInDrop(loc)) {
            plugin.getRegionManager().markPlayerPlaced(loc);
            plugin.getQuestManager().progress(player, "place_block", 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) return;

        if (arenaOwns(victim.getLocation()) || arenaOwns(attacker.getLocation())) {
            // ArenaListener handles platform PvP; still track damage quests in playable
            if (plugin.getArenaManager().isInAnyPlayable(victim.getLocation())
                    && plugin.getArenaManager().isInAnyPlayable(attacker.getLocation())
                    && !plugin.getArenaManager().isOnAnySpawnPlatform(victim.getLocation())
                    && !plugin.getArenaManager().isOnAnySpawnPlatform(attacker.getLocation())) {
                plugin.getQuestManager().progress(attacker, "deal_damage", (int) Math.max(1, event.getFinalDamage()));
                plugin.getQuestManager().progress(victim, "take_damage", (int) Math.max(1, event.getFinalDamage()));
            }
            return;
        }

        boolean victimLobby = plugin.getRegionManager().isInLobby(victim);
        boolean attackerLobby = plugin.getRegionManager().isInLobby(attacker);
        if (victimLobby || attackerLobby) {
            event.setCancelled(true);
            return;
        }

        boolean victimDrop = plugin.getRegionManager().isInDrop(victim);
        boolean attackerDrop = plugin.getRegionManager().isInDrop(attacker);
        if (!victimDrop || !attackerDrop) {
            return;
        }
        plugin.getQuestManager().progress(attacker, "deal_damage", (int) Math.max(1, event.getFinalDamage()));
        plugin.getQuestManager().progress(victim, "take_damage", (int) Math.max(1, event.getFinalDamage()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (arenaOwns(player.getLocation())) return;
        if (!plugin.getRegionManager().isInLobby(player)) return;
        if (LOBBY_BLOCKED.contains(event.getCause()) || event instanceof EntityDamageByEntityEvent) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (arenaOwns(player.getLocation())) return;
        if (plugin.getRegionManager().isInLobby(player)) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (arenaOwns(player.getLocation())) return;
        if (!plugin.getRegionManager().isInLobby(player)) return;
        ItemStack stack = event.getItemDrop().getItemStack();
        Material type = stack.getType();
        if (type.name().contains("SWORD") || type.name().contains("AXE") || type.name().contains("HELMET")
                || type.name().contains("CHESTPLATE") || type.name().contains("LEGGINGS")
                || type.name().contains("BOOTS") || type == Material.SHIELD
                || type == Material.ENDER_PEARL || type == Material.GOLDEN_APPLE
                || type == Material.ENCHANTED_GOLDEN_APPLE || type == Material.TOTEM_OF_UNDYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        // Arena system owns leave-arena teleport when arenas exist
        if (!plugin.getArenaManager().getArenas().isEmpty()) return;

        Player player = event.getPlayer();
        boolean inDrop = plugin.getRegionManager().isInDrop(event.getTo());
        Boolean was = wasInDrop.put(player.getUniqueId(), inDrop);
        if (Boolean.TRUE.equals(was) && !inDrop) {
            if (plugin.getSpawnManager().getSpawn() != null) {
                plugin.getSpawnManager().teleport(player);
                plugin.getMessageService().send(player, "left-drop-arena");
            }
        }
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }
}
