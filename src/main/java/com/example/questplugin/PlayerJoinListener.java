package com.example.questplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerJoinListener implements Listener {

    private final QuestPlugin plugin;

    public PlayerJoinListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
    
        if (plugin.getQuestManager().getPlayerQuests(uuid).isEmpty()) {
            List<Quest> daily = plugin.getQuestAssigner().assignDailyQuests(uuid);
            plugin.getQuestManager().assignNewDailyQuests(uuid, daily);
            plugin.debug("[Join] Assigned daily quests to " + uuid);
        }
    
        if (plugin.getQuestManager().getPlayerWeeklyQuests(uuid).isEmpty()) {
            List<Quest> weekly = plugin.getQuestAssigner().assignWeeklyQuests(uuid);
            plugin.getQuestManager().assignNewWeeklyQuests(uuid, weekly);
            plugin.debug("[Join] Assigned weekly quests to " + uuid);
        }

        if (plugin.getQuestManager().getGlobalQuests().isEmpty()){
            plugin.debug("Setting global quests");
                    // Reset global quests that are completed
            List<Quest> currentGlobal = plugin.getQuestManager().getGlobalQuests();
            int globalLimit = plugin.getConfig().getInt("QuestLimits.GLOBAL", 10);
            List<Quest> newGlobals = new ArrayList<>(currentGlobal);
    
            List<QuestTemplate> allGlobalTemplates = new ArrayList<>(plugin.getQuestLoader().getTemplatesByTier(QuestTier.GLOBAL));
            Set<String> usedIds = currentGlobal.stream()
                .filter(q -> !q.isCompleted()) // preserve incomplete ones
                .map(Quest::getId)
                .collect(Collectors.toSet());
    
            // Remove completed ones
            newGlobals.removeIf(Quest::isCompleted);
    
            // Fill back up to the limit
            List<QuestTemplate> available = new ArrayList<>(allGlobalTemplates.stream()
                .filter(template -> !usedIds.contains(template.getId()))
                .toList());
    
            Collections.shuffle(available);
            for (QuestTemplate template : available) {
                if (newGlobals.size() >= globalLimit) break;
                newGlobals.add(template.toQuest());
            }
    
            plugin.getQuestManager().setGlobalQuests(newGlobals);
        }
    }
} 
