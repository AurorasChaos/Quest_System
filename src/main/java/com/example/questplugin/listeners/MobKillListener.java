package com.example.questplugin.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.enums.QuestType;

public class MobKillListener extends BaseListener {
    private final QuestPlugin plugin;

    public MobKillListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

@EventHandler
public void onEntityDeath(EntityDeathEvent event) {
    if (event.getEntity() instanceof LivingEntity livingEntity && livingEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
        if (damageEvent.getDamager() instanceof Player player) {
            plugin.getQuestHandler().checkAndProgressQuest(
                player,
                QuestType.KILL_MOB,
                event.getEntityType().name(),  // get the type of entity that was killed
                1
            );
        }
    }
}
}