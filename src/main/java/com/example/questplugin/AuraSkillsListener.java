package com.example.questplugin;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.event.skill.SkillLevelUpEvent;
import dev.aurelium.auraskills.api.event.skill.XpGainEvent;
import dev.aurelium.auraskills.api.skill.Skill;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AuraSkillsListener implements Listener {

    private final QuestPlugin plugin;

    public AuraSkillsListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSkillExpGain(XpGainEvent  event) {
        Player player = event.getPlayer();
        Skill skill = event.getSkill();
        double amount = event.getAmount();

        for (Quest quest : plugin.getQuestManager().getPlayerQuests(player.getUniqueId())) {
            if (quest.getType() == QuestType.GAIN_SKILL_EXP &&
                quest.matchesTarget(skill.getId().getKey()) &&
                !quest.isCompleted()) {
                quest.incrementProgress((int) amount);
            }
        }
    }

    @EventHandler
    public void onSkillLevelUp(SkillLevelUpEvent event) {
        Player player = event.getPlayer();
        Skill skill = event.getSkill();
        int level = event.getLevel();

        for (Quest quest : plugin.getQuestManager().getPlayerQuests(player.getUniqueId())) {
            if (quest.getType() == QuestType.REACH_SKILL_LEVEL &&
                quest.matchesTarget(skill.getId().getKey()) &&
                !quest.isCompleted() &&
                quest.getCurrentProgress() < level) {
                quest.setCurrentProgress(level);
            }
        }
    }
}
