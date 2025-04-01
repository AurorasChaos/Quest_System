// BaseListener.java
package com.example.questplugin.listeners;

import java.util.function.Supplier;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.enums.QuestType;

public abstract class BaseListener implements Listener {
    protected final QuestPlugin plugin = QuestPlugin.getInstance();
    
    protected void processEvent(Event event, QuestType type, Supplier<String> targetSupplier) {
        Player player = extractPlayer(event);
        plugin.getQuestHandler().checkAndProgressQuest(
            player, type, targetSupplier.get(), 1
        );
    }

    // Add this to your BaseListener.java
    protected record QuestEventData(QuestType type, String targetKey, int amount) {}

    protected void processEvent(Event event, Supplier<QuestEventData> dataSupplier) {
        if (!(event instanceof Cancellable cancellable) || !cancellable.isCancelled()) {
            Player player = extractPlayer(event); // Implement player extraction per event type
            QuestEventData data = dataSupplier.get();
            plugin.getQuestHandler().checkAndProgressQuest(
                player, 
                data.type(), 
                data.targetKey(), 
                data.amount()
            );
        }
    }

    private Player extractPlayer(Event event) {
        if (event instanceof BlockBreakEvent e) return e.getPlayer();
        if (event instanceof BlockPlaceEvent e) return e.getPlayer();
        // Add other event types as needed
        throw new IllegalArgumentException("Unsupported event type: " + event.getClass());
    }
}