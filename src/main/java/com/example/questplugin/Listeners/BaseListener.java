package com.example.questplugin.Listeners;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.managers.RewardHandler;
import com.example.questplugin.model.Quest;
import com.example.questplugin.model.QuestType;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class BaseListener {

    protected final QuestPlugin plugin;
    protected final RewardHandler rewardHandler;

    public BaseListener(QuestPlugin plugin) {
        this.plugin = plugin;
        this.rewardHandler = plugin.getRewardHandler();
    }

    /**
     * Increment progress and notify the player of quest completion.
     *
     * @param player The player who completed the task.
     * @param quest  The quest to update.
     */
    protected void incrementProgressAndNotify(Player player, Quest quest) {
        quest.incrementProgress(1);
        plugin.getQuestNotifier().notifyProgress(player, quest);
        rewardHandler.giveReward(player, quest);
    }

    /**
     * Handle quests of a specific type and target.
     *
     * @param type   The type of quest to handle.
     * @param target The target value for the quest (e.g., mob type, block type).
     * @param player The player who performed the action.
     */
    protected void handleQuestTypeAndTarget(QuestType type, String target, Player player) {
        // Handle tiered quests
        for (com.example.questplugin.model.QuestTier tier : com.example.questplugin.model.QuestTier.values()) {
            List<Quest> quests = plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier);
            for (Quest quest : quests) {
                if (quest.getType() == type && quest.matchesTarget(target) && !quest.isCompleted()) {
                    incrementProgressAndNotify(player, quest);
                    plugin.debug("[" + getEventType() + "] +1 progress on " + quest.getId() + " (" + tier + ")");
                }
            }
        }

        // Handle global quests
        List<Quest> globalQuests = plugin.getQuestManager().getGlobalQuests();
        for (Quest quest : globalQuests) {
            if (quest.getType() == type && quest.matchesTarget(target) && !quest.isCompleted()) {
                incrementProgressAndNotify(player, quest);
                plugin.debug("[" + getEventType() + "] +1 GLOBAL progress on " + quest.getId());
            }
        }
    }

    /**
     * Get the event type for logging and debugging.
     *
     * @return The event type as a string.
     */
    protected abstract String getEventType();
}
