package com.blaze.wildkits;

import com.blaze.wildkits.command.WildKitsCommand;
import com.blaze.wildkits.config.ConfigManager;
import com.blaze.wildkits.database.DatabaseManager;
import com.blaze.wildkits.economy.CoinManager;
import com.blaze.wildkits.gui.GuiListener;
import com.blaze.wildkits.gui.MenuService;
import com.blaze.wildkits.integration.CitizensHook;
import com.blaze.wildkits.integration.LuckPermsHook;
import com.blaze.wildkits.integration.PlaceholderHook;
import com.blaze.wildkits.integration.VaultHook;
import com.blaze.wildkits.kit.KitManager;
import com.blaze.wildkits.listener.CombatListener;
import com.blaze.wildkits.listener.PlayerListener;
import com.blaze.wildkits.message.MessageService;
import com.blaze.wildkits.particle.ParticleManager;
import com.blaze.wildkits.player.PlayerDataManager;
import com.blaze.wildkits.scoreboard.ScoreboardManager;
import com.blaze.wildkits.shop.ShopManager;
import com.blaze.wildkits.spawn.SpawnManager;
import com.blaze.wildkits.util.AnimationService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlazesWildKits extends JavaPlugin {

    private static BlazesWildKits instance;

    private ConfigManager configManager;
    private MessageService messageService;
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private KitManager kitManager;
    private ShopManager shopManager;
    private ParticleManager particleManager;
    private CoinManager coinManager;
    private SpawnManager spawnManager;
    private ScoreboardManager scoreboardManager;
    private MenuService menuService;
    private AnimationService animationService;
    private VaultHook vaultHook;
    private LuckPermsHook luckPermsHook;
    private CitizensHook citizensHook;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfigs();

        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        this.messageService = new MessageService(this);
        this.messageService.load();

        this.animationService = new AnimationService(this);
        this.animationService.load();

        this.databaseManager = new DatabaseManager(this);
        boolean databaseOk = false;
        try {
            databaseOk = this.databaseManager.connect();
        } catch (Throwable t) {
            getLogger().severe("Database bootstrap crashed unexpectedly: " + t.getMessage());
            t.printStackTrace();
        }
        if (!databaseOk) {
            getLogger().warning("WildKits is running without a persistent database.");
            getLogger().warning("Stats/coins may reset on restart until SQLite/MySQL is fixed.");
        } else if (this.databaseManager.isMemoryFallback()) {
            getLogger().warning("WildKits database is in memory-fallback mode (non-persistent).");
        }

        this.playerDataManager = new PlayerDataManager(this);
        this.kitManager = new KitManager(this);
        this.kitManager.load();

        this.shopManager = new ShopManager(this);
        this.shopManager.load();

        this.particleManager = new ParticleManager(this);
        this.particleManager.load();
        this.particleManager.startTask();

        this.coinManager = new CoinManager(this);
        this.coinManager.startPlaytimeTask();

        this.spawnManager = new SpawnManager(this);
        this.spawnManager.load();

        this.scoreboardManager = new ScoreboardManager(this);
        this.scoreboardManager.start();

        this.menuService = new MenuService(this);

        registerIntegrations();
        registerCommands();
        registerListeners();

        getLogger().info("Blaze's WildKits v" + getPluginMeta().getVersion() + " enabled ("
                + kitManager.getKits().size() + " kits loaded).");
    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) {
            scoreboardManager.shutdown();
        }
        if (particleManager != null) {
            particleManager.shutdown();
        }
        if (coinManager != null) {
            coinManager.shutdown();
        }
        if (playerDataManager != null) {
            playerDataManager.saveAllSync();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("Blaze's WildKits disabled.");
    }

    public boolean reloadPlugin() {
        try {
            configManager.loadAll();
            messageService.load();
            animationService.load();
            kitManager.load();
            shopManager.load();
            particleManager.load();
            spawnManager.load();
            scoreboardManager.reload();
            return true;
        } catch (Exception e) {
            getLogger().severe("Reload failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void saveDefaultConfigs() {
        String[] files = {
                "config.yml", "kits.yml", "shop.yml", "scoreboard.yml",
                "animations.yml", "messages_en.yml", "messages_ro.yml",
                "database.yml", "particles.yml"
        };
        for (String file : files) {
            saveResource(file, false);
        }
    }

    private void registerIntegrations() {
        this.vaultHook = new VaultHook(this);
        this.vaultHook.setup();

        this.luckPermsHook = new LuckPermsHook(this);
        this.luckPermsHook.setup();

        this.citizensHook = new CitizensHook(this);
        this.citizensHook.setup();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
            getLogger().info("PlaceholderAPI hooked.");
        }
    }

    private void registerCommands() {
        WildKitsCommand command = new WildKitsCommand(this);
        var pluginCommand = getCommand("wk");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
    }

    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new GuiListener(this), this);
        if (citizensHook != null && citizensHook.isEnabled()) {
            pm.registerEvents(citizensHook, this);
        }
    }

    public static BlazesWildKits getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    public CoinManager getCoinManager() {
        return coinManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public MenuService getMenuService() {
        return menuService;
    }

    public AnimationService getAnimationService() {
        return animationService;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public CitizensHook getCitizensHook() {
        return citizensHook;
    }
}
