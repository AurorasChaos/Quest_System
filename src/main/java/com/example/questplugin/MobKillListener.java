package com.example.questplugin;

import org.bukkit.entity.Monster;
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
    public void onMobKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;
        if (!(event.getEntity() instanceof Monster)) return;

        String mobType = event.getEntityType().name();
        var uuid = killer.getUniqueId();

        // Process all tiers (daily, weekly)
        for (QuestTier tier : QuestTier.values()) {
            for (Quest quest : plugin.getQuestManager().getQuestsForTier(uuid, tier)) {
                if (quest.getType() == QuestType.KILL_MOB
                        && quest.matchesTarget(mobType)
                        && !quest.isCompleted()) {
                    quest.incrementProgress(1);
                    new QuestNotifier(plugin).notifyProgress(killer, quest);
                    plugin.debug("[Progress] +" + 1 + " for " + tier + " quest " + quest.getId() + " (" + mobType + ")");
                }
            }
        }

        // Also update global quests (shared progress)
        for (Quest quest : plugin.getQuestManager().getGlobalQuests()) {
            if (quest.getType() == QuestType.KILL_MOB
                    && quest.matchesTarget(mobType)
                    && !quest.isCompleted()) {
                quest.incrementProgress(1);
                plugin.debug("[Progress] +1 global progress on quest " + quest.getId() + " (" + mobType + ")");
            }
        }
    }
}
