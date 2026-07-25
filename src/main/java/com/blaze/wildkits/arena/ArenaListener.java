package com.blaze.wildkits.arena;

import com.blaze.wildkits.BlazesWildKits;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles arena configure tools, spawn-platform safe zone, playable protection,
 * and the one-shot fall-damage exemption from spawn platform into arena.
 */
public final class ArenaListener implements Listener {

    private static final Set<EntityDamageEvent.DamageCause> PLATFORM_BLOCKED = EnumSet.of(
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
            EntityDamageEvent.DamageCause.LIGHTNING,
            EntityDamageEvent.DamageCause.FREEZE,
            EntityDamageEvent.DamageCause.CAMPFIRE
    );

    private final BlazesWildKits plugin;

    public ArenaListener(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConfigureTool(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("wildkits.admin")) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ArenaManager arenas = plugin.getArenaManager();
        ArenaConfigureSession session = arenas.getSession(player);

        if (arenas.isSpawnTool(item)) {
            event.setCancelled(true);
            if (session == null) {
                plugin.getMessageService().send(player, "configure-no-session");
                return;
            }
            // Prefer exact click location; allow air aim via player eye location
            Location target;
            if (event.getClickedBlock() != null) {
                target = event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
                target.setYaw(player.getLocation().getYaw());
                target.setPitch(player.getLocation().getPitch());
            } else {
                // Air click — use player's exact position (supports mid-air platforms)
                target = player.getLocation().clone();
            }
            session.setSpawnPlatform(target);
            plugin.getMessageService().send(player, "configure-spawn-set", Map.of(
                    "arena", session.getArenaId(),
                    "x", String.valueOf(target.getBlockX()),
                    "y", String.valueOf(target.getBlockY()),
                    "z", String.valueOf(target.getBlockZ())
            ));
            tryAutoSave(player, session);
            return;
        }

        if (arenas.isArenaTool(item)) {
            event.setCancelled(true);
            if (session == null) {
                plugin.getMessageService().send(player, "configure-no-session");
                return;
            }
            Location clicked;
            if (event.getClickedBlock() != null) {
                clicked = event.getClickedBlock().getLocation();
            } else {
                // Allow air selection using player block location
                clicked = player.getLocation().getBlock().getLocation();
            }
            switch (event.getAction()) {
                case LEFT_CLICK_BLOCK, LEFT_CLICK_AIR -> {
                    session.setPos1(clicked);
                    plugin.getMessageService().send(player, "configure-pos1", Map.of(
                            "arena", session.getArenaId(),
                            "x", String.valueOf(clicked.getBlockX()),
                            "y", String.valueOf(clicked.getBlockY()),
                            "z", String.valueOf(clicked.getBlockZ())
                    ));
                }
                case RIGHT_CLICK_BLOCK, RIGHT_CLICK_AIR -> {
                    session.setPos2(clicked);
                    plugin.getMessageService().send(player, "configure-pos2", Map.of(
                            "arena", session.getArenaId(),
                            "x", String.valueOf(clicked.getBlockX()),
                            "y", String.valueOf(clicked.getBlockY()),
                            "z", String.valueOf(clicked.getBlockZ())
                    ));
                }
                default -> {
                }
            }
            tryAutoSave(player, session);
        }
    }

    private void tryAutoSave(Player player, ArenaConfigureSession session) {
        if (!session.canSave()) return;
        if (plugin.getArenaManager().saveSession(player)) {
            plugin.getMessageService().send(player, "configure-saved", Map.of("arena", session.getArenaId()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("wildkits.admin")) return;
        Location loc = event.getBlock().getLocation();
        ArenaManager arenas = plugin.getArenaManager();

        if (arenas.isOnAnySpawnPlatform(loc)) {
            event.setCancelled(true);
            return;
        }

        if (arenas.isInAnyPlayable(loc)) {
            if (!arenas.isPlayerPlaced(loc)) {
                event.setCancelled(true);
                return;
            }
            arenas.unmarkPlayerPlaced(loc);
            plugin.getQuestManager().progress(player, "break_placed", 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("wildkits.admin")) return;
        Location loc = event.getBlock().getLocation();
        ArenaManager arenas = plugin.getArenaManager();

        if (arenas.isOnAnySpawnPlatform(loc)) {
            event.setCancelled(true);
            return;
        }

        if (arenas.isInAnyPlayable(loc)) {
            arenas.markPlayerPlacedReplacing(event.getBlock(), event.getBlockReplacedState().getBlockData());
            plugin.getQuestManager().progress(player, "place_block", 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) return;
        ArenaManager arenas = plugin.getArenaManager();
        if (arenas.isOnAnySpawnPlatform(victim.getLocation()) || arenas.isOnAnySpawnPlatform(attacker.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ArenaManager arenas = plugin.getArenaManager();

        // One-shot protected fall: spawn platform → arena
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && arenas.consumeProtectedFall(player)) {
            event.setCancelled(true);
            return;
        }

        if (arenas.isOnAnySpawnPlatform(player.getLocation())) {
            if (PLATFORM_BLOCKED.contains(event.getCause()) || event instanceof EntityDamageByEntityEvent) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getArenaManager().isOnAnySpawnPlatform(player.getLocation())) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.getArenaManager().isOnAnySpawnPlatform(event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        ArenaManager arenas = plugin.getArenaManager();
        event.blockList().removeIf(block ->
                arenas.isOnAnySpawnPlatform(block.getLocation())
                        || (arenas.isInAnyPlayable(block.getLocation()) && !arenas.isPlayerPlaced(block.getLocation())));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        ArenaManager arenas = plugin.getArenaManager();
        event.blockList().removeIf(block ->
                arenas.isOnAnySpawnPlatform(block.getLocation())
                        || (arenas.isInAnyPlayable(block.getLocation()) && !arenas.isPlayerPlaced(block.getLocation())));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        Player player = event.getPlayer();
        ArenaManager arenas = plugin.getArenaManager();
        UUID id = player.getUniqueId();
        boolean onPlatform = arenas.isOnAnySpawnPlatform(event.getTo());
        Boolean was = arenas.getWasOnPlatform().put(id, onPlatform);

        // Leaving spawn platform downward into playable arena → arm one protected fall
        if (Boolean.TRUE.equals(was) && !onPlatform) {
            if (event.getTo().getY() < event.getFrom().getY()
                    && arenas.isInAnyPlayable(event.getTo())) {
                arenas.armProtectedFall(player);
            }
        }

        // Leaving playable arena entirely → return to global spawn
        if (!arenas.getArenas().isEmpty()) {
            boolean inPlayable = arenas.isInAnyPlayable(event.getTo());
            boolean wasPlayable = arenas.isInAnyPlayable(event.getFrom());
            if (wasPlayable && !inPlayable && !onPlatform && !arenas.isOnAnySpawnPlatform(event.getFrom())) {
                if (plugin.getSpawnManager().getSpawn() != null) {
                    plugin.getSpawnManager().teleport(player);
                    arenas.clearProtectedFall(player);
                    plugin.getMessageService().send(player, "left-drop-arena");
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getArenaManager().clearProtectedFall(event.getPlayer());
        plugin.getArenaManager().clearSession(event.getPlayer());
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }
}
