package com.example.questplugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class ExtraEventsListener implements Listener {

    private final QuestPlugin plugin;

    public ExtraEventsListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String type = block.getType().name();

        plugin.debug("[BlockBreak] " + player.getName() + " broke " + type);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if ((quest.getType() == QuestType.MINE_BLOCK || quest.getType() == QuestType.GATHER_ITEM)
                        && quest.matchesTarget(type)
                        && !quest.isCompleted()) {
                    plugin.debug("[BlockBreak] Progress +1 for " + player.getName() + " on " + tier + " quest " + quest.getId());
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if ((quest.getType() == QuestType.MINE_BLOCK || quest.getType() == QuestType.GATHER_ITEM)
                    && quest.matchesTarget(type)
                    && !quest.isCompleted()) {
                plugin.debug("[BlockBreak] Progress +1 GLOBAL for " + player.getName() + " on quest " + quest.getId());
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String type = block.getType().name();

        plugin.debug("[BlockPlace] " + player.getName() + " placed " + type);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.PLACE_BLOCK && quest.matchesTarget(type) && !quest.isCompleted()) {
                    plugin.debug("[BlockPlace] Progress +1 for " + player.getName() + " on " + tier + " quest " + quest.getId());
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.PLACE_BLOCK && quest.matchesTarget(type) && !quest.isCompleted()) {
                plugin.debug("[BlockPlace] Progress +1 GLOBAL for " + player.getName() + " on quest " + quest.getId());
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
            }
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        plugin.debug("[Fishing] " + player.getName() + " caught something");

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.FISH && !quest.isCompleted()) {
                    plugin.debug("[Fishing] Progress +1 for " + player.getName() + " on " + tier + " quest " + quest.getId());
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.FISH && !quest.isCompleted()) {
                plugin.debug("[Fishing] Progress +1 GLOBAL for " + player.getName() + " on quest " + quest.getId());
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
            }
        }
    }

    @EventHandler
    public void onLoot(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        plugin.debug("[Loot] " + player.getName() + " looted " + event.getEntityType().name());

        String entityType = event.getEntityType().name();

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.LOOT && quest.matchesTarget(entityType) && !quest.isCompleted()) {
                    plugin.debug("[Loot] Progress +1 for " + player.getName() + " on " + tier + " quest " + quest.getId());
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.LOOT && quest.matchesTarget(entityType) && !quest.isCompleted()) {
                plugin.debug("[Loot] Progress +1 GLOBAL for " + player.getName() + " on quest " + quest.getId());
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
            }
        }
    }
}
