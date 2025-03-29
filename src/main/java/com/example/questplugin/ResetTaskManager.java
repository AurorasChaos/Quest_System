package com.example.questplugin;

import org.bukkit.Bukkit;

import java.time.LocalDate;
import java.util.UUID;

public class ResetTaskManager {

    private final QuestPlugin plugin;
    private LocalDate lastResetDate;

    public ResetTaskManager(QuestPlugin plugin) {
        this.plugin = plugin;
        this.lastResetDate = LocalDate.now().minusDays(1);
    }

    public void checkResetOnStartup() {
        LocalDate today = LocalDate.now();
        performDailyReset();
        performWeeklyReset();
        if (!lastResetDate.equals(today)) {
            performDailyReset();
            if (today.getDayOfWeek().getValue() == 1) {
                performWeeklyReset();
            }
            lastResetDate = today;
        }
        
    }

    public void performDailyReset() {
        Bukkit.getLogger().info("[QuestPlugin] Performing daily quest reset.");
        for (UUID uuid : plugin.getQuestManager().getAllPlayers()) {
            var templates = plugin.getQuestLoader().getTemplatesByTier(QuestTier.DAILY);
            var assigned = templates.stream().map(QuestTemplate::toQuest).toList();
            plugin.getQuestManager().assignNewDailyQuests(uuid, assigned);
        }
        plugin.getQuestStorage().save();
    }
    
    public void performWeeklyReset() {
        Bukkit.getLogger().info("[QuestPlugin] Performing weekly quest reset.");
        for (UUID uuid : plugin.getQuestManager().getAllPlayers()) {
            var templates = plugin.getQuestLoader().getTemplatesByTier(QuestTier.WEEKLY);
            var assigned = templates.stream().map(QuestTemplate::toQuest).toList();
            plugin.getQuestManager().assignNewWeeklyQuests(uuid, assigned);
        }
        plugin.getQuestStorage().save();
    }

    public void scheduleMidnightResetCheck() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            LocalDate today = LocalDate.now();
            if (!lastResetDate.equals(today)) {
                performDailyReset();
                if (today.getDayOfWeek().getValue() == 1) {
                    performWeeklyReset();
                }
                lastResetDate = today;
            }
        }, 0L, 20L * 60 * 60); // Every hour
    }
}
