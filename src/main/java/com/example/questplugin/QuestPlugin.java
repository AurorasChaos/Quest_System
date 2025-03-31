package com.example.questplugin;

import com.example.questplugin.core.*;
import com.example.questplugin.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import dev.aurelium.auraskills.api.AuraSkillsApi;

public final class QuestPlugin extends JavaPlugin {

    // Core Services
    private QuestHandler questHandler;
    private ConfigManager configManager;
    private GlobalQuestService globalQuestService;
    private QuestStorageService storageService;
    private QuestManager questManager;
    private LeaderboardManager leaderboardManager;

    // Integrations
    private Economy economy;
    private AuraSkillsApi auraSkillsApi;
    
    private static QuestPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        
        // 1. Initialize configuration
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);

        // 2. Setup integrations
        setupEconomy();
        this.auraSkillsApi = AuraSkillsApi.get();

        // 3. Initialize core systems
        this.storageService = new QuestStorageService(this);
        this.globalQuestService = new GlobalQuestService(this);
        this.questHandler = new QuestHandler(this);
        this.leaderboardManager = new LeaderboardManager(this);
        this.questManager = new QuestManager(this);

        // 4. Register listeners
        registerListeners();

        // 5. Load data
        loadInitialData();

        // 6. Register commands
        getCommand("questdev").setExecutor(new DevCommands(this));
        getCommand("questadmin").setExecutor(new AdminCommands(this));

        getLogger().info("QuestPlugin v" + getDescription().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        // Save all pending data
        questManager.saveAllPlayerData();
        globalQuestService.saveGlobalQuests();
        
        getLogger().info("QuestPlugin disabled gracefully");
    }

    // === Initialization Methods ===
    private void registerListeners() {
        new AuraSkillsListener(this);
        new BlockEventsListener(this);
        new LifeEventsListener(this);
        new MobKillListener(this);
        new PlayerJoinListener(this);
        
        getServer().getPluginManager().registerEvents(new QuestGUI(this), this);
    }

    private void loadInitialData() {
        // Load global quests first
        globalQuestService.initializeQuests();

        // Then player data
        Bukkit.getOnlinePlayers().forEach(player -> {
            storageService.loadPlayerData(player.getUniqueId())
                .thenAccept(data -> questManager.initializePlayer(
                    player.getUniqueId(), 
                    data
                ));
        });
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        var rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    // === Accessors ===
    public static QuestPlugin getInstance() {
        return instance;
    }

    public QuestHandler getQuestHandler() {
        return questHandler;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GlobalQuestService getGlobalQuestService() {
        return globalQuestService;
    }

    public QuestStorageService getQuestStorageService() {
        return storageService;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public AuraSkillsApi getAuraSkillsApi() {
        return auraSkillsApi;
    }
}