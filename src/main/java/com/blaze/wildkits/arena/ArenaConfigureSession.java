package com.blaze.wildkits.arena;

import org.bukkit.Location;

import java.util.UUID;

/**
 * In-progress arena editor session (no GUI — tool based).
 */
public final class ArenaConfigureSession {

    private final UUID playerId;
    private final String arenaId;
    private final boolean creating;
    private Location spawnPlatform;
    private Location pos1;
    private Location pos2;

    public ArenaConfigureSession(UUID playerId, String arenaId, boolean creating) {
        this.playerId = playerId;
        this.arenaId = arenaId == null ? "default" : arenaId.toLowerCase();
        this.creating = creating;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getArenaId() {
        return arenaId;
    }

    public boolean isCreating() {
        return creating;
    }

    public Location getSpawnPlatform() {
        return spawnPlatform == null ? null : spawnPlatform.clone();
    }

    public void setSpawnPlatform(Location spawnPlatform) {
        this.spawnPlatform = spawnPlatform == null ? null : spawnPlatform.clone();
    }

    public Location getPos1() {
        return pos1 == null ? null : pos1.clone();
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1 == null ? null : pos1.clone();
    }

    public Location getPos2() {
        return pos2 == null ? null : pos2.clone();
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2 == null ? null : pos2.clone();
    }

    public boolean hasArenaSelection() {
        return pos1 != null && pos2 != null
                && pos1.getWorld() != null && pos2.getWorld() != null
                && pos1.getWorld().equals(pos2.getWorld());
    }

    public boolean canSave() {
        return spawnPlatform != null && hasArenaSelection();
    }
}
