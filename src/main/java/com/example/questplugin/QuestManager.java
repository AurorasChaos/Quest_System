// QuestManager with global quest persistence and saving support
package com.example.questplugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class QuestManager {

    private final QuestPlugin plugin;
    private final Map<UUID, List<Quest>> dailyQuests = new ConcurrentHashMap<>();
    private final Map<UUID, List<Quest>> weeklyQuests = new ConcurrentHashMap<>();
    private final List<Quest> globalQuests = new ArrayList<>();
    private final File globalFile;
    private final FileConfiguration globalConfig;

    public QuestManager(QuestPlugin plugin) {
        this.plugin = plugin;
        this.globalFile = new File(plugin.getDataFolder(), "global_quests.yml");
        if (!globalFile.exists()) {
            try {
                globalFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.globalConfig = YamlConfiguration.loadConfiguration(globalFile);
        initializeGlobalQuests();
    }

    private void initializeGlobalQuests() {
        var templates = plugin.getQuestLoader().getTemplatesByTier(QuestTier.GLOBAL);
        for (QuestTemplate template : templates) {
            Quest quest = template.toQuest();
            String id = quest.getId();
            if (globalConfig.contains(id)) {
                quest.setCurrentProgress(globalConfig.getInt(id + ".progress"));
                if (globalConfig.getBoolean(id + ".claimed")) quest.claimReward();
            }
            globalQuests.add(quest);
        }
        plugin.debug("[Global] Loaded " + globalQuests.size() + " global quests.");
    }

    public void saveGlobalQuests() {
        for (Quest quest : globalQuests) {
            String id = quest.getId();
            globalConfig.set(id + ".progress", quest.getCurrentProgress());
            globalConfig.set(id + ".claimed", quest.isRewardClaimed());
        }
        try {
            globalConfig.save(globalFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.debug("[Global] Saved global quests to file.");
    }

    public List<Quest> getPlayerQuests(UUID uuid) {
        return dailyQuests.getOrDefault(uuid, new ArrayList<>());
    }

    public List<Quest> getPlayerWeeklyQuests(UUID uuid) {
        return weeklyQuests.getOrDefault(uuid, new ArrayList<>());
    }

    public List<Quest> getQuestsForTier(UUID uuid, QuestTier tier) {
        return switch (tier) {
            case DAILY -> getPlayerQuests(uuid);
            case WEEKLY -> getPlayerWeeklyQuests(uuid);
            case GLOBAL -> getGlobalQuests();
            case ALL -> getAllPlayerQuests(uuid);
        };
    }

    public List<Quest> getAllPlayerQuests(UUID uuid) {
        List<Quest> all = new ArrayList<>();
        all.addAll(getPlayerQuests(uuid));
        all.addAll(getPlayerWeeklyQuests(uuid));
        all.addAll(getGlobalQuests()); // include global quests too
        return all;
    }

    public void assignNewDailyQuests(UUID uuid, List<Quest> quests) {
        dailyQuests.put(uuid, quests);
    }

    public void assignNewWeeklyQuests(UUID uuid, List<Quest> quests) {
        weeklyQuests.put(uuid, quests);
    }

    public Set<UUID> getAllPlayers() {
        Set<UUID> set = new HashSet<>();
        set.addAll(dailyQuests.keySet());
        set.addAll(weeklyQuests.keySet());
        return set;
    }

    public List<Quest> getGlobalQuests() {
        return globalQuests;
    }
} 
