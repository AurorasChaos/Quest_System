// QuestHandler.java
package com.example.questplugin;

import org.bukkit.entity.Player;
import java.util.List;

public class QuestHandler {
    private final QuestPlugin plugin;
    
    public QuestHandler(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkAndProgressQuest(Player player, QuestType type, String targetKey, int amount) {
        QuestManager manager = plugin.getQuestManager();
        
        // Process tiered quests (daily/weekly)
        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : manager.getQuestsForTier(player.getUniqueId(), tier)) {
                if (shouldProgress(quest, type, targetKey)) {
                    progressQuest(player, quest, amount);
                }
            }
        }

        // Process global quests
        for (Quest quest : manager.getGlobalQuests()) {
            if (shouldProgress(quest, type, targetKey)) {
                progressQuest(player, quest, amount);
            }
        }
    }

    private boolean shouldProgress(Quest quest, QuestType type, String targetKey) {
        return quest.getType() == type && 
               !quest.isCompleted() && 
               quest.matchesTarget(targetKey);
    }

    private void progressQuest(Player player, Quest quest, int amount) {
        quest.incrementProgress(amount);
        new QuestNotifier(plugin).notifyProgress(player, quest);
        plugin.debug("[Quest] Progress +" + amount + " on " + quest.getId());
        
        if (quest.isCompleted()) {
            plugin.getLeaderboardManager().recordCompletion(player.getUniqueId(), quest);
        }
    }

    public void handleSkillEvent(Player player, QuestType type, Skill skill) {
        checkAndProgressQuest(player, type, skill.name().toUpperCase(), 1);
    }
}