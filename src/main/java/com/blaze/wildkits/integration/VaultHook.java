package com.blaze.wildkits.integration;

import com.blaze.wildkits.BlazesWildKits;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultHook {

    private final BlazesWildKits plugin;
    private Economy economy;
    private boolean enabled;

    public VaultHook(BlazesWildKits plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found. Using internal coins only.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Vault found but no economy provider.");
            return;
        }
        this.economy = rsp.getProvider();
        this.enabled = economy != null;
        if (enabled) {
            plugin.getLogger().info("Vault economy hooked (" + economy.getName() + ").");
        }
    }

    public boolean isEnabled() {
        return enabled && economy != null;
    }

    public void deposit(Player player, double amount) {
        if (!isEnabled() || amount <= 0) return;
        economy.depositPlayer(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (!isEnabled()) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public double balance(Player player) {
        if (!isEnabled()) return 0;
        return economy.getBalance(player);
    }
}
