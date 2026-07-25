package com.blaze.wildkits.integration;

import com.blaze.wildkits.BlazesWildKits;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public final class CitizensHook implements Listener {

    public static final String METADATA_KEY = "wildkits-npc";

    private final BlazesWildKits plugin;
    private boolean enabled;

    public CitizensHook(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (Bukkit.getPluginManager().getPlugin("Citizens") == null) {
            plugin.getLogger().info("Citizens not found. NPC features disabled.");
            return;
        }
        this.enabled = true;
        plugin.getLogger().info("Citizens hooked.");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void createNpc(Player player, String name) {
        if (!enabled) return;
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.spawn(player.getLocation());
        npc.data().setPersistent(METADATA_KEY, true);
        if (npc.hasTrait(Owner.class)) {
            npc.getOrAddTrait(Owner.class).setOwner(player);
        }
        plugin.getMessageService().send(player, "npc-created", Map.of("name", name));
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (!enabled) return;
        NPC npc = event.getNPC();
        if (npc == null) return;
        Boolean flag = npc.data().get(METADATA_KEY);
        if (flag == null || !flag) {
            // Also allow by name containing WildKits
            if (npc.getName() == null || !npc.getName().toLowerCase().contains("wildkits")) {
                return;
            }
        }
        Player player = event.getClicker();
        String name = npc.getName() == null ? "" : npc.getName().toLowerCase();
        if (name.contains("quest")) {
            plugin.getQuestManager().openGui(player);
        } else if (name.contains("shop")) {
            plugin.getMenuService().openShop(player);
        } else {
            plugin.getMenuService().openMain(player);
        }
    }
}
