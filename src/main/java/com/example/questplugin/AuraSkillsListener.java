package com.example.questplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AuraSkillsListener implements Listener {

    private final QuestPlugin plugin;

    public AuraSkillsListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSkillUse(SkillUseEvent event) {
        Player player = event.getPlayer();
        String skill = event.getSkillName();

        for (Quest quest : plugin.getQuestManager().getPlayerQuests(player.getUniqueId())) {
            if (quest.getType() == QuestType.USE_SKILL &&
                quest.matchesTarget(skill) &&
                !quest.isCompleted()) {
                quest.incrementProgress(1);
            }
        }
    }

    @EventHandler
    public void onSkillExpGain(SkillExpGainEvent event) {
        Player player = event.getPlayer();
        String skill = event.getSkillName();
        int amount = event.getAmount();

        for (Quest quest : plugin.getQuestManager().getPlayerQuests(player.getUniqueId())) {
            if (quest.getType() == QuestType.GAIN_SKILL_EXP &&
                quest.matchesTarget(skill) &&
                !quest.isCompleted()) {
                quest.incrementProgress(amount);
            }
        }
    }

    @EventHandler
    public void onSkillLevelUp(SkillLevelUpEvent event) {
        Player player = event.getPlayer();
        String skill = event.getSkillName();
        int level = event.getNewLevel();

        for (Quest quest : plugin.getQuestManager().getPlayerQuests(player.getUniqueId())) {
            if (quest.getType() == QuestType.REACH_SKILL_LEVEL &&
                quest.matchesTarget(skill) &&
                !quest.isCompleted() &&
                quest.getCurrentProgress() < level) {
                quest.setCurrentProgress(level);
            }
        }
    }
}
