// ConfigManager.java
package com.example.questplugin.core;

import com.example.questplugin.QuestPlugin;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final FileConfiguration config;

    public ConfigManager(QuestPlugin plugin) {
        this.config = plugin.getConfig();
    }

    // Quest Settings
    public int getDailyQuestLimit() {
        return config.getInt("QuestLimits.DAILY", 5);
    }

    public int getWeeklyQuestLimit(){
        return config.getInt("QuestLimits.WEEKLY");
    }

    public int getGlobalQuestLimit(){
        return config.getInt("QuestLimits.GLOBAL");
    }

    // Leaderboard Settings
    public boolean isHologramEnabled() {
        return config.getBoolean("Leaderboard.EnableHologram", false);
    }

    public String getHologramWorld() {
        return config.getString("Leaderboard.Location.World", "world");
    }

    // Add more getters as needed...
}