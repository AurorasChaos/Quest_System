package com.example.questplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final QuestPlugin plugin;

    public PlayerJoinListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        boolean needsDaily = plugin.getQuestManager().getPlayerQuests(uuid).isEmpty();
        boolean needsWeekly = plugin.getQuestManager().getPlayerWeeklyQuests(uuid).isEmpty();

        if (needsDaily || needsWeekly) {
            plugin.debug("[Join] Assigning new quests for " + uuid);

            if (needsDaily) {
                List<Quest> daily = plugin.getQuestLoader().getTemplatesByTier(QuestTier.DAILY).stream()
                        .map(QuestTemplate::toQuest).toList();
                plugin.getQuestManager().assignNewDailyQuests(uuid, daily);
            }

            if (needsWeekly) {
                List<Quest> weekly = plugin.getQuestLoader().getTemplatesByTier(QuestTier.WEEKLY).stream()
                        .map(QuestTemplate::toQuest).toList();
                plugin.getQuestManager().assignNewWeeklyQuests(uuid, weekly);
            }
        }
    }
} 
