package com.example.questplugin;

import org.bukkit.entity.Player;

public class RewardHandler {
    private final QuestPlugin plugin;

    public RewardHandler(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean giveReward(Player player, Quest quest) {
        if (!quest.canClaim()) return false;

        double multiplier = quest.getRarity().getMultiplier();

        if (plugin.getEconomy() != null) {
            plugin.getEconomy().depositPlayer(player, quest.getCurrencyReward() * multiplier);
        }

        if (plugin.getAuraSkillsAPI() != null) {
            plugin.getAuraSkillsAPI().addSkillPoints(player, (int)(quest.getSkillPointReward() * multiplier));
        }

        quest.claimReward();
        plugin.getLeaderboardManager().recordCompletion(player.getUniqueId(), quest);
        return true;
    }
}
