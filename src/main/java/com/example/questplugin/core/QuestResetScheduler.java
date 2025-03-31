package com.example.questplugin.core;

import org.bukkit.scheduler.BukkitScheduler;
import com.example.questplugin.QuestPlugin;

public class QuestResetScheduler {
    private final BukkitScheduler scheduler = null;
    
    public void startDailyReset() {
        scheduler.runTaskTimer(plugin, () -> {
            plugin.getQuestManager().resetDailyQuests();
        }, 0L, 20L * 60 * 60 * 24); // 24h
    }
}