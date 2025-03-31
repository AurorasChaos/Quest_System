package com.example.questplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEventsListener extends BaseListener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        processEvent(event, 
            () -> {
                // Determine quest type based on context (could be MINE_BLOCK or GATHER_ITEM)
                QuestType type = shouldCountAsGather(event.getBlock().getType()) 
                    ? QuestType.GATHER_ITEM 
                    : QuestType.MINE_BLOCK;
                
                return new QuestEventData(
                    type,
                    event.getBlock().getType().name(),
                    1
                );
            }
        );
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        processEvent(event, 
            () -> new QuestEventData(
                QuestType.PLACE_BLOCK,
                event.getBlock().getType().name(),
                1
            )
        );
    }

    private boolean shouldCountAsGather(Material material) {
        // Example logic - expand with your game-specific rules
        return material == Material.STONE || 
               material == Material.DEEPSLATE ||
               material.toString().endsWith("_ORE");
    }
}