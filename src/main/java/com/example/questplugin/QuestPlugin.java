package com.example.questplugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import dev.aurelium.auraskills.api.AuraSkillsApi;

@SuppressWarnings("unused")
public class QuestPlugin extends JavaPlugin {

    private QuestManager questManager;
    private QuestLoader questLoader;
    private QuestStorageManager questStorage;
    private LeaderboardManager leaderboardManager;
    private RarityRoller rarityRoller;
    private Economy economy;
    private ResetTaskManager resetTaskManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.questLoader = new QuestLoader(this);
        this.questStorage = new QuestStorageManager(this);
        this.questManager = new QuestManager(this);
        this.leaderboardManager = new LeaderboardManager();
        this.rarityRoller = new RarityRoller(this);
        this.resetTaskManager = new ResetTaskManager(this);

        getServer().getPluginManager().registerEvents(new ExtraEventsListener(this), this);
        getServer().getPluginManager().registerEvents(new AuraSkillsListener(this), this);

        getCommand("questdev").setExecutor(new DevCommands(this));
        getCommand("quest").setExecutor(new QuestCommand(this));

        resetTaskManager.checkResetOnStartup();

        getLogger().info("QuestPlugin enabled.");

    }

    @Override
    public void onDisable() {
        if (questStorage != null) {
            questStorage.save();
        }
        getLogger().info("QuestPlugin disabled.");
    }

    public QuestManager getQuestManager() { return questManager; }
    public QuestLoader getQuestLoader() { return questLoader; }
    public QuestStorageManager getQuestStorage() { return questStorage; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public RarityRoller getRarityRoller() { return rarityRoller; }
    public Economy getEconomy() {return economy;}
    public AuraSkillsApi getAuraSkillsApi() {return AuraSkillsApi.get();}
}
