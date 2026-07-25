package com.blaze.wildkits.region;

import org.bukkit.Location;

import java.util.UUID;

public final class SelectionSession {

    public enum Target {
        LOBBY, DROP, NONE
    }

    private final UUID uuid;
    private Location pos1;
    private Location pos2;
    private Target target = Target.NONE;

    public SelectionSession(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() { return uuid; }
    public Location getPos1() { return pos1; }
    public Location getPos2() { return pos2; }
    public void setPos1(Location pos1) { this.pos1 = pos1 == null ? null : pos1.clone(); }
    public void setPos2(Location pos2) { this.pos2 = pos2 == null ? null : pos2.clone(); }
    public Target getTarget() { return target; }
    public void setTarget(Target target) { this.target = target == null ? Target.NONE : target; }

    public boolean isComplete() {
        return pos1 != null && pos2 != null
                && pos1.getWorld() != null && pos2.getWorld() != null
                && pos1.getWorld().equals(pos2.getWorld());
    }

    public CuboidRegion toRegion() {
        if (!isComplete()) return null;
        return new CuboidRegion(pos1, pos2);
    }
}
