package com.example.questplugin;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.entity.Villager;

public class LifeEventsListener implements Listener {

    private final QuestPlugin plugin;

    public LifeEventsListener(QuestPlugin plugin) {
        this.plugin = plugin;
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

    @EventHandler
    public void onBiomeEnter(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        Biome previousBiome = from.getBlock().getBiome();
        Biome newBiome = to.getBlock().getBiome();

        if (previousBiome == newBiome) return;

        UUID uuid = player.getUniqueId();
        String biomeName = newBiome.name();

        plugin.debug("[BiomeVisit] " + player.getName() + " entered biome: " + biomeName);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(uuid, tier)) {
                if (quest.getType() == QuestType.EXPLORE_BIOME && quest.matchesTarget(biomeName) && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(player, quest);
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.EXPLORE_BIOME && quest.matchesTarget(biomeName) && !quest.isCompleted()) {
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(player, quest);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getType() == InventoryType.MERCHANT) {
            plugin.debug(player.getName() + " interacted with a Villager trade.");

            String target = "VILLAGER";
            // Optional: You can track the trade slot
            int slot = event.getRawSlot();
            if (slot == 2 && event.getCurrentItem() != null) { // Result slot of the trade
                plugin.debug(player.getName() + " completed a trade!");
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
        }
    }
}