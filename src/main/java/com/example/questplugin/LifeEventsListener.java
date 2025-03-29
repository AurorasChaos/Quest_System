package com.example.questplugin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.entity.Villager;

public class LifeEventsListener implements Listener {

    private final QuestPlugin plugin;

    public LifeEventsListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVillagerTrade(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager)) return;
        Player player = event.getPlayer();
        String target = "VILLAGER";

        plugin.debug("[Trade] " + player.getName() + " is trading with a villager");

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.TRADE && quest.matchesTarget(target) && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                    plugin.debug("[Trade] +1 progress on " + quest.getId() + " (" + tier + ")");
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.TRADE && quest.matchesTarget(target) && !quest.isCompleted()) {
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
                plugin.debug("[Trade] +1 GLOBAL progress on " + quest.getId());
            }
        }
    }

    @EventHandler
    public void onAnimalBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) return;

        String type = event.getEntityType().name();

        plugin.debug("[Breed] " + player.getName() + " bred a " + type);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.BREED_ANIMAL && quest.matchesTarget(type) && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                    plugin.debug("[Breed] +1 progress on " + quest.getId() + " (" + tier + ")");
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.BREED_ANIMAL && quest.matchesTarget(type) && !quest.isCompleted()) {
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
                plugin.debug("[Breed] +1 GLOBAL progress on " + quest.getId());
            }
        }
    }

        @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) return;

        String type = event.getEntityType().name();
        plugin.debug("[Tame] " + player.getName() + " tamed a " + type);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.TAME_ENTITY && quest.matchesTarget(type) && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                    plugin.debug("[Tame] +1 progress on " + quest.getId() + " (" + tier + ")");
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.TAME_ENTITY && quest.matchesTarget(type) && !quest.isCompleted()) {
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
                plugin.debug("[Tame] +1 GLOBAL progress on " + quest.getId());
            }
        }
    }

    @EventHandler
    public void onCraft(org.bukkit.event.inventory.CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String item = event.getRecipe().getResult().getType().name();
        plugin.debug("[Craft] " + player.getName() + " crafted " + item);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(player.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.CRAFT_ITEM && quest.matchesTarget(item) && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                    plugin.debug("[Craft] +1 progress on " + quest.getId() + " (" + tier + ")");
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.CRAFT_ITEM && quest.matchesTarget(item) && !quest.isCompleted()) {
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
                plugin.debug("[Craft] +1 GLOBAL progress on " + quest.getId());
            }
        }
    }
}