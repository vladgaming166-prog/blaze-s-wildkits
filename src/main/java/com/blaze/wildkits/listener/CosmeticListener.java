package com.blaze.wildkits.listener;

import com.blaze.wildkits.BlazesWildKits;
import com.blaze.wildkits.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Premium cosmetics: chat colors, spawn cages, projectile trails, wing particles.
 */
public final class CosmeticListener implements Listener {

    private final BlazesWildKits plugin;
    private final Map<UUID, List<BlockSnapshot>> activeCages = new ConcurrentHashMap<>();
    private BukkitTask wingTask;
    private BukkitTask projectileTask;
    private final Map<UUID, Projectile> trackedProjectiles = new ConcurrentHashMap<>();

    public CosmeticListener(BlazesWildKits plugin) {
        this.plugin = plugin;
        startTasks();
    }

    private void startTasks() {
        wingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.isOnline() || player.isDead() || player.isInvisible()) continue;
                PlayerData data = plugin.getPlayerDataManager().get(player);
                String wing = data.getActiveWingParticle();
                if (wing == null || wing.isBlank()) continue;
                spawnWings(player, wing);
            }
        }, 4L, 4L);

        projectileTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            trackedProjectiles.entrySet().removeIf(entry -> {
                Projectile projectile = entry.getValue();
                if (projectile == null || !projectile.isValid() || projectile.isDead()) return true;
                PlayerData data = plugin.getPlayerDataManager().get(entry.getKey());
                String trail = data.getActiveProjectileTrail();
                if (trail == null) return true;
                spawnProjectileTrail(projectile.getLocation(), trail);
                return false;
            });
        }, 1L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        PlayerData data = plugin.getPlayerDataManager().get(event.getPlayer());
        String color = data.getActiveChatColor();
        if (color == null || color.isBlank()) return;
        String code = switch (color.toLowerCase(Locale.ROOT)) {
            case "red" -> "§c";
            case "gold" -> "§6";
            case "aqua" -> "§b";
            case "green" -> "§a";
            case "purple" -> "§d";
            case "white" -> "§f";
            case "gray" -> "§7";
            default -> "§e";
        };
        event.setMessage(code + event.getMessage());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> spawnCage(player), 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectile(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data.getActiveProjectileTrail() == null) return;
        trackedProjectiles.put(player.getUniqueId(), event.getEntity());
    }

    private void spawnCage(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        String cage = data.getActiveSpawnCage();
        if (cage == null || cage.isBlank()) return;
        clearCage(player.getUniqueId());

        Material mat = switch (cage.toLowerCase(Locale.ROOT)) {
            case "glass" -> Material.GLASS;
            case "iron" -> Material.IRON_BARS;
            case "nether" -> Material.NETHER_BRICK_FENCE;
            case "prismarine" -> Material.PRISMARINE;
            default -> Material.GLASS;
        };

        Location base = player.getLocation().getBlock().getLocation();
        List<BlockSnapshot> snapshots = new ArrayList<>();
        int[][] offsets = {
                {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
                {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1},
                {1, 2, 0}, {-1, 2, 0}, {0, 2, 1}, {0, 2, -1},
                {0, 3, 0}
        };
        for (int[] o : offsets) {
            Block block = base.clone().add(o[0], o[1], o[2]).getBlock();
            if (!block.getType().isAir()) continue;
            snapshots.add(new BlockSnapshot(block.getLocation().clone(), block.getBlockData().clone()));
            block.setType(mat, false);
        }
        activeCages.put(player.getUniqueId(), snapshots);

        Bukkit.getScheduler().runTaskLater(plugin, () -> clearCage(player.getUniqueId()), 40L);
        plugin.getMessageService().send(player, "spawn-cage-active", Map.of("cage", cage));
    }

    private void clearCage(UUID uuid) {
        List<BlockSnapshot> snapshots = activeCages.remove(uuid);
        if (snapshots == null) return;
        for (BlockSnapshot snap : snapshots) {
            Block block = snap.location().getBlock();
            block.setBlockData(snap.data(), false);
        }
    }

    private void spawnWings(Player player, String wing) {
        Location loc = player.getLocation().add(0, 1.2, 0);
        double yaw = Math.toRadians(player.getLocation().getYaw());
        double leftX = -Math.cos(yaw) * 0.55;
        double leftZ = -Math.sin(yaw) * 0.55;
        double rightX = Math.cos(yaw) * 0.55;
        double rightZ = Math.sin(yaw) * 0.55;
        Particle particle = switch (wing.toLowerCase(Locale.ROOT)) {
            case "angel" -> Particle.END_ROD;
            case "demon" -> Particle.FLAME;
            case "dragon" -> Particle.DRAGON_BREATH;
            case "frost" -> Particle.SNOWFLAKE;
            default -> Particle.CLOUD;
        };
        player.getWorld().spawnParticle(particle, loc.clone().add(leftX, 0, leftZ), 3, 0.08, 0.2, 0.08, 0.01);
        player.getWorld().spawnParticle(particle, loc.clone().add(rightX, 0, rightZ), 3, 0.08, 0.2, 0.08, 0.01);
    }

    private void spawnProjectileTrail(Location loc, String trail) {
        Particle particle = switch (trail.toLowerCase(Locale.ROOT)) {
            case "flame" -> Particle.FLAME;
            case "soul" -> Particle.SOUL;
            case "crit" -> Particle.CRIT;
            case "heart" -> Particle.HEART;
            case "rainbow" -> Particle.DUST;
            default -> Particle.CRIT;
        };
        if (particle == Particle.DUST) {
            loc.getWorld().spawnParticle(Particle.DUST, loc, 2, 0.05, 0.05, 0.05, 0,
                    new Particle.DustOptions(Color.fromRGB(255, 100, 200), 1.0f));
        } else {
            loc.getWorld().spawnParticle(particle, loc, 2, 0.05, 0.05, 0.05, 0.01);
        }
    }

    public void shutdown() {
        if (wingTask != null) wingTask.cancel();
        if (projectileTask != null) projectileTask.cancel();
        for (UUID uuid : new ArrayList<>(activeCages.keySet())) {
            clearCage(uuid);
        }
        trackedProjectiles.clear();
    }

    private record BlockSnapshot(Location location, org.bukkit.block.data.BlockData data) {}
}
