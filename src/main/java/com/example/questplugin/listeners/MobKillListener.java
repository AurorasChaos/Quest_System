package com.example.questplugin;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillListener implements BaseListener {
    private final QuestPlugin plugin;

    public MobKillListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instance of Player player)) return;
        plugin.getQuestHandler().checkAndProgressQuest(
            player,
            QuestType.KILL_MOB,
            event,getEntityType().name(),
            1
        )
    }
}