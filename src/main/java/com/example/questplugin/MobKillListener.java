package com.example.questplugin;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillListener implements Listener {
    private final QuestPlugin plugin;

    public MobKillListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;

        String type = event.getEntityType().name();
        plugin.debug("[Kill] " + killer.getName() + " killed " + type);

        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(killer.getUniqueId(), tier)) {
                if (quest.getType() == QuestType.KILL_MOB && quest.matchesTarget(type) && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(killer, quest);
                    plugin.debug("[Kill] +1 progress on " + quest.getId() + " (" + tier + ")");
                }
            }
        }

        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.KILL_MOB && quest.matchesTarget(type) && !quest.isCompleted()) {
                quest.incrementProgress(1);
                new QuestNotifier(plugin).notifyProgress(killer, quest);
                plugin.debug("[Kill] +1 GLOBAL progress on " + quest.getId());
            }
        }
    }
}