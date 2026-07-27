package com.blaze.wildkits.particle;

import org.bukkit.Particle;

public final class ParticleTrail {

    private final String id;
    private final String displayName;
    private final Particle particle;
    private final int amount;
    private final double spacing;
    private final double speed;
    private final boolean enabled;

    public ParticleTrail(String id, String displayName, Particle particle, int amount,
                         double spacing, double speed, boolean enabled) {
        this.id = id;
        this.displayName = displayName;
        this.particle = particle;
        this.amount = Math.max(1, amount);
        this.spacing = Math.max(0.05, spacing);
        this.speed = Math.max(0.0, speed);
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Particle getParticle() { return particle; }
    public int getAmount() { return amount; }
    public double getSpacing() { return spacing; }
    public double getSpeed() { return speed; }
    public boolean isEnabled() { return enabled; }
}
