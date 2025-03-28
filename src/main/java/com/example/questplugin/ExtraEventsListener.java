package com.example.questplugin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;

public class ExtraEventsListener implements Listener {

    private final QuestPlugin plugin;
    private final QuestNotifier notifier;

    public ExtraEventsListener(QuestPlugin plugin) {
        this.plugin = plugin;
        this.notifier = new QuestNotifier(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();

        for (Quest quest : plugin.getQuestManager().getPlayerQuests(player.getUniqueId())) {
            if (quest.getType() == QuestType.GATHER_ITEM &&
                quest.matchesTarget(mat.name()) &&
                !quest.isCompleted()) {
                quest.incrementProgress(1);
                notifier.notifyProgress(player, quest);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();

        for (Quest quest : plugin.getQuestManager().getPlayerQuests(player.getUniqueId())) {
            if (quest.getType() == QuestType.PLACE_BLOCK &&
                quest.matchesTarget(mat.name()) &&
                !quest.isCompleted()) {
                quest.incrementProgress(1);
                notifier.notifyProgress(player, quest);
            }
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        for (Quest quest : plugin.getQuestManager().getPlayerQuests(player.getUniqueId())) {
            if (quest.getType() == QuestType.FISH &&
                !quest.isCompleted()) {
                quest.incrementProgress(1);
                notifier.notifyProgress(player, quest);
            }
        }
    }

    @EventHandler
    public void onVillagerTrade(VillagerReplenishTradeEvent event) {
        // You can track trade events via InventoryClickEvent or other custom triggers
    }

    public void onBossDamage(Player player, int damageAmount) {
        for (Quest quest : plugin.getQuestManager().getPlayerQuests(player.getUniqueId())) {
            if (quest.getType() == QuestType.CUSTOM &&
                quest.matchesTarget("BOSS_DAMAGE") &&
                !quest.isCompleted()) {
                quest.incrementProgress(damageAmount);
                notifier.notifyProgress(player, quest);
            }
        }
    }
}
