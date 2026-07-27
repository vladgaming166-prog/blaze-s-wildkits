package com.blaze.wildkits.crate;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CrateDefinition {

    public record Reward(String id, String displayName, Material icon, double weight,
                         int coins, int xp, String cosmeticId, String keyRarity, int keys) {}

    private final String id;
    private final String displayName;
    private final CrateRarity rarity;
    private final Material blockType;
    private Location location;
    private final List<Reward> rewards = new ArrayList<>();

    public CrateDefinition(String id, String displayName, CrateRarity rarity, Material blockType) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.blockType = blockType == null ? Material.CHEST : blockType;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public CrateRarity getRarity() { return rarity; }
    public Material getBlockType() { return blockType; }
    public Location getLocation() { return location == null ? null : location.clone(); }
    public void setLocation(Location location) { this.location = location == null ? null : location.clone(); }
    public List<Reward> getRewards() { return Collections.unmodifiableList(rewards); }
    public void clearRewards() { rewards.clear(); }
    public void addReward(Reward reward) { if (reward != null) rewards.add(reward); }
}
