// BaseListener.java
package com.example.questplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class BaseListener implements Listener {
    protected final QuestPlugin plugin = QuestPlugin.getInstance();
    
    protected void processEvent(Event event, QuestType type, Supplier<String> targetSupplier) {
        Player player = // ... extract player from event ...;
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