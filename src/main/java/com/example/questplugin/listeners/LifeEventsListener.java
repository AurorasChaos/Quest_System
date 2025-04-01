package com.example.questplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.MerchantRecipe;

import com.example.questplugin.enums.QuestType;

import java.util.function.Supplier;

public class LifeEventsListener extends BaseListener {

    @EventHandler
    public void onAnimalBreed(EntityBreedEvent event) {
        processEvent(event, 
            () -> new QuestEventData(
                QuestType.BREED_ANIMAL,
                event.getEntityType().name(),
                1
            )
        );
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        processEvent(event, 
            () -> new QuestEventData(
                QuestType.TAME_ENTITY,
                event.getEntityType().name(),
                1
            )
        );
    }

    @EventHandler
    public void onCraftItem(org.bukkit.event.inventory.CraftItemEvent event) {
        processEvent(event, 
            () -> new QuestEventData(
                QuestType.CRAFT_ITEM,
                event.getRecipe().getResult().getType().name(),
                event.getRecipe().getResult().getAmount()
            )
        );
    }

    @EventHandler
    public void onBiomeEnter(PlayerMoveEvent event) {
        if (!isNewBiome(event)) return;
        
        processEvent(event, 
            () -> new QuestEventData(
                QuestType.EXPLORE_BIOME,
                event.getTo().getBlock().getBiome().name(),
                1
            )
        );
    }

    @EventHandler
    public void onVillagerTrade(InventoryClickEvent event) {
        if (!isTradeEvent(event)) return;
        
        processEvent(event, 
            () -> new QuestEventData(
                QuestType.TRADE,
                "VILLAGER",
                1
            )
        );
    }

    @EventHandler
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        processEvent(event, 
            () -> new QuestEventData(
                QuestType.CONSUME_ITEM,
                event.getItem().getType().name(),
                1
            )
        );
    }

    // Helper Methods
    private boolean isNewBiome(PlayerMoveEvent event) {
        if (event.getTo() == null) return false;
        return !event.getFrom().getBlock().getBiome()
               .equals(event.getTo().getBlock().getBiome());
    }

    private boolean isTradeEvent(InventoryClickEvent event) {
        return event.getInventory().getType() == InventoryType.MERCHANT && 
               event.getRawSlot() == 2; // Result slot
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        processEvent(event, 
            () -> new QuestEventData(
                QuestType.BREW_ITEM,
                "ANY", // Or parse ingredient
                1
            )
        );
    }
}