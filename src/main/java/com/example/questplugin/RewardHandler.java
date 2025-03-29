package com.example.questplugin;

import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import dev.aurelium.auraskills.api.user.SkillsUser;

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

        SkillsUser user = plugin.getAuraSkillsApi().getUser(player.getUniqueId());
        if (plugin.getAuraSkillsApi() != null) {
            try {
                Method getPoints = user.getClass().getMethod("getUnallocatedSkillPoints");
                int points = (int) getPoints.invoke(user);

                Method setPoints = user.getClass().getMethod("setUnallocatedSkillPoints", int.class);
                setPoints.invoke(user, points + quest.getSkillPointReward());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        quest.claimReward();
        plugin.getLeaderboardManager().recordCompletion(player.getUniqueId(), quest);
        return true;
    }
}
