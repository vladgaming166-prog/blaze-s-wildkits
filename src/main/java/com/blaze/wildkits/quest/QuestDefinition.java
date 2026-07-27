package com.blaze.wildkits.quest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class QuestDefinition {

    private final String id;
    private final String displayName;
    private final String description;
    private final QuestType type;
    private final String objective; // kill, crate_open, playtime_minutes, etc.
    private final int target;
    private final Map<String, Integer> rewards;

    public QuestDefinition(String id, String displayName, String description, QuestType type,
                           String objective, int target, Map<String, Integer> rewards) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.objective = objective;
        this.target = Math.max(1, target);
        this.rewards = rewards == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(rewards));
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public String getObjective() { return objective; }
    public int getTarget() { return target; }
    public Map<String, Integer> getRewards() { return rewards; }
}
