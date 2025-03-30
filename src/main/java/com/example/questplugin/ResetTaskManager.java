// Updated ResetTaskManager to prevent duplicate resets and persist lastResetDate
package com.example.questplugin;

import org.bukkit.Bukkit;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ResetTaskManager {

    private final QuestPlugin plugin;
    private LocalDate lastResetDate;
    private final File resetFile;
    private final YamlConfiguration config;

    public ResetTaskManager(QuestPlugin plugin) {
        this.plugin = plugin;
        this.resetFile = new File(plugin.getDataFolder(), "reset_data.yml");
        if (!resetFile.exists()) {
            try {
                resetFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(resetFile);
        this.lastResetDate = loadLastResetDate();
    }

    private LocalDate loadLastResetDate() {
        String stored = config.getString("last_reset", null);
        if (stored != null) {
            try {
                return LocalDate.parse(stored);
            } catch (Exception ignored) {}
        }
        return LocalDate.now().minusDays(1);
    }

    private void saveLastResetDate(LocalDate date) {
        config.set("last_reset", date.toString());
        try {
            config.save(resetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkResetOnStartup() {
        LocalDate today = LocalDate.now();
        plugin.debug("[Reset] Last reset: " + lastResetDate + ", Today: " + today);

        if (!lastResetDate.equals(today)) {
            performDailyReset();
            if (today.getDayOfWeek().getValue() == 1) {
                performWeeklyReset();
            }
            lastResetDate = today;
            saveLastResetDate(today);
        } else {
            plugin.debug("[Reset] Skipped reset; already ran today.");
        }
    }

    public void performDailyReset() {
        Bukkit.getLogger().info("[QuestPlugin] Performing daily quest reset.");
    
        for (UUID uuid : plugin.getQuestManager().getAllPlayers()) {
            List<Quest> daily = plugin.getQuestAssigner().assignDailyQuests(uuid);
            List<Quest> weekly = plugin.getQuestManager().getPlayerWeeklyQuests(uuid);
            plugin.getQuestManager().assignNewDailyQuests(uuid, daily);
            plugin.getQuestStorage().savePlayerQuests(uuid, daily, weekly);
        }
    
        plugin.getQuestStorage().save();
    }

    public void performWeeklyReset() {
        Bukkit.getLogger().info("[QuestPlugin] Performing weekly quest reset.");
    
        // Reset global quests that are completed
        List<Quest> currentGlobal = plugin.getQuestManager().getGlobalQuests();
        int globalLimit = plugin.getConfig().getInt("QuestLimits.GLOBAL", 10);
        List<Quest> newGlobals = new ArrayList<>(currentGlobal);
    
        List<QuestTemplate> allGlobalTemplates = plugin.getQuestLoader().getTemplatesByTier(QuestTier.GLOBAL);
        Set<String> usedIds = currentGlobal.stream()
            .filter(q -> !q.isCompleted()) // preserve incomplete ones
            .map(Quest::getId)
            .collect(Collectors.toSet());
    
        // Remove completed ones
        newGlobals.removeIf(Quest::isCompleted);
    
        // Fill back up to the limit
        List<QuestTemplate> available = allGlobalTemplates.stream()
            .filter(template -> !usedIds.contains(template.getId()))
            .toList();
    
        Collections.shuffle(available);
        for (QuestTemplate template : available) {
            if (newGlobals.size() >= globalLimit) break;
            newGlobals.add(template.toQuest());
        }
    
        plugin.getQuestManager().setGlobalQuests(newGlobals);
    
        // Now reset weekly quests for players
        for (UUID uuid : plugin.getQuestManager().getAllPlayers()) {
            List<Quest> weekly = plugin.getQuestAssigner().assignWeeklyQuests(uuid);
            List<Quest> daily = plugin.getQuestManager().getPlayerQuests(uuid);
            plugin.getQuestManager().assignNewWeeklyQuests(uuid, weekly);
            plugin.getQuestStorage().savePlayerQuests(uuid, daily, weekly);
        }
    
        plugin.getQuestStorage().save();
        Bukkit.getLogger().info("[QuestPlugin] Weekly reset complete.");
    }

    public void giveDevQuest(UUID uuid, Quest quest) {
        List<Quest> existing = plugin.getQuestManager().getPlayerQuests(uuid);
        boolean alreadyHas = existing.stream().anyMatch(q -> q.getId().equalsIgnoreCase(quest.getId()));
        if (!alreadyHas) {
            existing.add(quest);
            plugin.getQuestStorage().savePlayerQuests(uuid, existing, plugin.getQuestManager().getPlayerWeeklyQuests(uuid));
            plugin.debug("[Dev] Added quest '" + quest.getId() + "' to player " + uuid);
        } else {
            plugin.debug("[Dev] Player already has quest '" + quest.getId() + "'");
        }
    }
}