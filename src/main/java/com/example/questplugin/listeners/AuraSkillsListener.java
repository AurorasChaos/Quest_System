package com.example.questplugin.listeners;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.enums.QuestType;
import com.google.common.eventbus.Subscribe;
import dev.aurelium.auraskills.api.event.skill.SkillLevelUpEvent;
import dev.aurelium.auraskills.api.event.skill.XpGainEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class AuraSkillsListener extends BaseListener { // Extends BaseListener for shared logic

    public AuraSkillsListener(QuestPlugin plugin) {
        super();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Subscribe
    public void onAuraSkillLevelUp(SkillLevelUpEvent event) {
        plugin.getQuestHandler().handleSkillEvent(
            event.getPlayer(), 
            QuestType.GAIN_SKILL_LEVEL, 
            event.getSkill()
        );
    }

    @Subscribe
    public void onAuraXpGain(XpGainEvent event) {
        Player player = event.getPlayer();
        String skillName = event.getSkill().name();
        
        plugin.getQuestHandler().checkAndProgressQuest(
            player,
            QuestType.GAIN_SKILL_EXP,
            skillName,
            1
        );
    }
}