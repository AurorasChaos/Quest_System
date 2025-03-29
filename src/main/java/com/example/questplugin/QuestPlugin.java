// This file will serve as the main patch hub for the fixed QuestPlugin system.
// We'll start with logging and config enhancements, then correct data syncing, resets, and missing event handling.

package com.example.questplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import dev.aurelium.auraskills.api.AuraSkillsApi;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class QuestPlugin extends JavaPlugin {

    private QuestManager questManager;
    private QuestLoader questLoader;
    private QuestStorageManager questStorage;
    private LeaderboardManager leaderboardManager;
    private RarityRoller rarityRoller;
    private Economy economy;
    private ResetTaskManager resetTaskManager;
    private boolean debugMode;

    @Override
    public void onEnable() {
        log("[Init] Loading configuration...");
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        this.debugMode = config.getBoolean("Debug", false);

        if (!setupEconomy()) {
            log("[Vault] Economy provider not found. Coin rewards will be disabled.");
        }

        log("[Init] Loading managers...");
        this.questLoader = new QuestLoader(this);
        this.questStorage = new QuestStorageManager(this);
        this.questManager = new QuestManager(this);
        this.leaderboardManager = new LeaderboardManager(this);
        this.rarityRoller = new RarityRoller(this);
        this.resetTaskManager = new ResetTaskManager(this);

        log("[Init] Registering event listeners...");
        getServer().getPluginManager().registerEvents(new QuestGUI(this), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockEventsListener(this), this);
        getServer().getPluginManager().registerEvents(new LifeEventsListener(this), this);
        getServer().getPluginManager().registerEvents(new AuraSkillsListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        

        log("[Init] Registering commands...");
        getCommand("questdev").setExecutor(new DevCommands(this));
        getCommand("quest").setExecutor(new QuestCommand(this));

        log("[Init] Loading saved quest data...");
        questStorage.loadIntoManager(questManager);

        for (UUID uuid : questManager.getAllPlayers()) {
            if (questManager.getPlayerQuests(uuid).isEmpty()) {
                List<Quest> daily = questLoader.getTemplatesByTier(QuestTier.DAILY).stream().map(QuestTemplate::toQuest).toList();
                questManager.assignNewDailyQuests(uuid, daily);
                debug("[Assign] Assigned new DAILY quests to " + uuid);
            }
            if (questManager.getPlayerWeeklyQuests(uuid).isEmpty()) {
                List<Quest> weekly = questLoader.getTemplatesByTier(QuestTier.WEEKLY).stream().map(QuestTemplate::toQuest).toList();
                questManager.assignNewWeeklyQuests(uuid, weekly);
                debug("[Assign] Assigned new WEEKLY quests to " + uuid);
            }
        }

        log("QuestPlugin enabled.");
    }

    @Override
    public void onDisable() {
        log("[Shutdown] Saving player and global quest data...");
        if (questManager != null) {
            questStorage.saveFromManager(questManager);
            questManager.saveGlobalQuests();
        }
        log("QuestPlugin disabled.");
    }

    public void log(String message) {
        getLogger().info(message);
    }

    public void debug(String message) {
        if (debugMode) log("[DEBUG] " + message);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public QuestManager getQuestManager() { return questManager; }
    public QuestLoader getQuestLoader() { return questLoader; }
    public QuestStorageManager getQuestStorage() { return questStorage; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public RarityRoller getRarityRoller() { return rarityRoller; }
    public Economy getEconomy() { return economy; }
    public AuraSkillsApi getAuraSkillsApi() { return AuraSkillsApi.get(); }
    public boolean isDebugMode() { return debugMode; }
}