package com.example.questplugin;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEventsListener implements Listener {
    private final QuestPlugin plugin;

    public BlockEventsListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String type = event.getBlock().getType().name();

        plugin.debug("[BlockBreak] " + player.getName() + " broke " + type);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if ((quest.getType() == QuestType.GATHER_ITEM || quest.getType() == QuestType.MINE_BLOCK)
                        && quest.matchesTarget(type) && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                    plugin.debug("[BlockBreak] +1 progress on " + quest.getId() + " (" + tier + ")");
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if ((quest.getType() == QuestType.GATHER_ITEM || quest.getType() == QuestType.MINE_BLOCK)
                    && quest.matchesTarget(type) && !quest.isCompleted()) {
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
                plugin.debug("[BlockBreak] +1 GLOBAL progress on " + quest.getId());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String type = event.getBlock().getType().name();

        plugin.debug("[BlockPlace] " + player.getName() + " placed " + type);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.PLACE_BLOCK && quest.matchesTarget(type) && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                    plugin.debug("[BlockPlace] +1 progress on " + quest.getId() + " (" + tier + ")");
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.PLACE_BLOCK && quest.matchesTarget(type) && !quest.isCompleted()) {
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
                plugin.debug("[BlockPlace] +1 GLOBAL progress on " + quest.getId());
            }
        }
    }
}