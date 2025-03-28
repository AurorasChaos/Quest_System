package com.example.questplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestStorageManager {

    private final QuestPlugin plugin;
    private final Map<UUID, List<Quest>> savedDaily = new HashMap<>();
    private final Map<UUID, List<Quest>> savedWeekly = new HashMap<>();
    private final File file;
    private FileConfiguration config;

    public QuestStorageManager(QuestPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player_quests.yml");
        if (!file.exists()) plugin.saveResource("player_quests.yml", false);
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void load() {
        for (String uuidStr : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            List<Quest> daily = new ArrayList<>();
            List<Quest> weekly = new ArrayList<>();
            for (String key : config.getConfigurationSection(uuidStr + ".daily").getKeys(false)) {
                QuestTemplate template = plugin.getQuestLoader().getAllTemplates().stream()
                    .filter(q -> q.toQuest().getId().equals(key)).findFirst().orElse(null);
                if (template == null) continue;
                Quest quest = template.toQuest();
                quest.setCurrentProgress(config.getInt(uuidStr + ".daily." + key + ".progress"));
                if (config.getBoolean(uuidStr + ".daily." + key + ".claimed")) quest.claimReward();
                daily.add(quest);
            }
            for (String key : config.getConfigurationSection(uuidStr + ".weekly").getKeys(false)) {
                QuestTemplate template = plugin.getQuestLoader().getAllTemplates().stream()
                    .filter(q -> q.toQuest().getId().equals(key)).findFirst().orElse(null);
                if (template == null) continue;
                Quest quest = template.toQuest();
                quest.setCurrentProgress(config.getInt(uuidStr + ".weekly." + key + ".progress"));
                if (config.getBoolean(uuidStr + ".weekly." + key + ".claimed")) quest.claimReward();
                weekly.add(quest);
            }
            savedDaily.put(uuid, daily);
            savedWeekly.put(uuid, weekly);
        }
    }

    public void save() {
        for (UUID uuid : savedDaily.keySet()) {
            for (Quest q : savedDaily.get(uuid)) {
                String path = uuid.toString() + ".daily." + q.getId();
                config.set(path + ".progress", q.getCurrentProgress());
                config.set(path + ".claimed", q.isRewardClaimed());
            }
        }
        for (UUID uuid : savedWeekly.keySet()) {
            for (Quest q : savedWeekly.get(uuid)) {
                String path = uuid.toString() + ".weekly." + q.getId();
                config.set(path + ".progress", q.getCurrentProgress());
                config.set(path + ".claimed", q.isRewardClaimed());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePlayerQuests(UUID uuid, List<Quest> daily, List<Quest> weekly) {
        savedDaily.put(uuid, daily);
        savedWeekly.put(uuid, weekly);
    }

    public List<Quest> getSavedDaily(UUID uuid) {
        return savedDaily.getOrDefault(uuid, new ArrayList<>());
    }

    public List<Quest> getSavedWeekly(UUID uuid) {
        return savedWeekly.getOrDefault(uuid, new ArrayList<>());
    }
}
