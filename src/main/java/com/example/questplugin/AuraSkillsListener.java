package com.example.questplugin;

import com.google.common.eventbus.Subscribe;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.event.skill.SkillLevelUpEvent;
import dev.aurelium.auraskills.api.event.skill.XpGainEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class AuraSkillsListener implements Listener {

    private final QuestPlugin plugin;

    public AuraSkillsListener(QuestPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Subscribe
    public void onAuraSkillLevelUp(SkillLevelUpEvent event) {
        Player player = event.getPlayer();
        String skillName = event.getSkill().name().toUpperCase();
        plugin.debug("[AuraSkills] " + player.getName() + " leveled up " + skillName);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.GAIN_SKILL_LEVEL && skillName.equalsIgnoreCase(quest.getTargetKey()) && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                    plugin.debug("[AuraSkills] +1 progress on quest " + quest.getId() + " (" + tier + ")");
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.GAIN_SKILL_LEVEL && skillName.equalsIgnoreCase(quest.getTargetKey()) && !quest.isCompleted()) {
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
                plugin.debug("[AuraSkills] +1 GLOBAL progress on quest " + quest.getId());
            }
        }
    }

    @Subscribe
    public void onAuraXpGain(XpGainEvent event) {
        Player player = event.getPlayer();
        String skillName = event.getSkill().name();
        plugin.debug("[AuraSkills] " + player.getName() + " gained xp in " + skillName);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.GAIN_SKILL_EXP && skillName.equalsIgnoreCase(quest.getTargetKey()) && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                    plugin.debug("[AuraSkills] +1 progress on quest " + quest.getId() + " (" + tier + ")");
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.GAIN_SKILL_EXP && skillName.equalsIgnoreCase(quest.getTargetKey()) && !quest.isCompleted()) {
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
                plugin.debug("[AuraSkills] +1 GLOBAL progress on quest " + quest.getId());
            }
        }
    }
}