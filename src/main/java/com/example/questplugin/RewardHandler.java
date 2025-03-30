package com.example.questplugin;

import java.lang.reflect.Method;
import java.util.Locale;

import org.bukkit.entity.Player;

import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;

public class RewardHandler {
    private final QuestPlugin plugin;

    public RewardHandler(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean giveReward(Player player, Quest quest) {
        if (!quest.canClaim()) return false;

        double multiplier = quest.getRarity().getMultiplier();

        plugin.debug("[Reward] Claiming reward for quest: " + quest.getId());

        if (plugin.getEconomy() != null) {
            plugin.debug("[AuraSkills] Gave " +quest.getCurrencyReward() + " to " + player.getName());
            plugin.getEconomy().depositPlayer(player, quest.getCurrencyReward() * multiplier);
        }

        if (quest.getSkillType() != null && quest.getSkillXp() > 0) {
            AuraSkillsApi api = plugin.getAuraSkillsApi();
            SkillsUser user = api.getUser(player.getUniqueId());

            if (user != null) {
                Skill skill = api.getGlobalRegistry().getSkill(NamespacedId.fromString(quest.getSkillType().toLowerCase()));
                if (skill != null) {
                    user.addSkillXp(skill, quest.getSkillXp());
                    Locale locale = Locale.forLanguageTag(player.getLocale());
                    plugin.debug("[AuraSkills] Gave " + quest.getSkillXp() + " XP to " + skill.getDisplayName(locale) + " for " + player.getName());
                } else {
                    plugin.getLogger().warning("[AuraSkills] Unknown skill type: " + quest.getSkillType());
                }
            }
        }
        quest.claimReward();
        plugin.getLeaderboardManager().recordCompletion(player.getUniqueId(), quest);
        return true;
    }
}
