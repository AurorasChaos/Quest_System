// QuestManager with global quest persistence and saving support
package com.example.questplugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class QuestManager {

    private final QuestPlugin plugin;
    private final Map<UUID, List<Quest>> dailyQuests = new ConcurrentHashMap<>();
    private final Map<UUID, List<Quest>> weeklyQuests = new ConcurrentHashMap<>();
    private final List<Quest> globalQuests = new ArrayList<>();
    private final File globalFile;
    private final FileConfiguration globalConfig;

    private final Map<UUID, List<Quest>> playerDailyQuests = new HashMap<>();
    private final Map<UUID, List<Quest>> playerWeeklyQuests = new HashMap<>();
    private final Map<UUID, List<Quest>> playerGlobalQuests = new HashMap<>();

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
            case DAILY -> playerDailyQuests.getOrDefault(uuid, new ArrayList<>());
            case WEEKLY -> playerWeeklyQuests.getOrDefault(uuid, new ArrayList<>());
            case GLOBAL -> playerGlobalQuests.getOrDefault(uuid, new ArrayList<>());
            case ALL -> {
                List<Quest> all = new ArrayList<>();
                all.addAll(playerDailyQuests.getOrDefault(uuid, Collections.emptyList()));
                all.addAll(playerWeeklyQuests.getOrDefault(uuid, Collections.emptyList()));
                all.addAll(playerGlobalQuests.getOrDefault(uuid, Collections.emptyList()));
                yield all;
            }
        };
    }

    public List<Quest> getAllPlayerQuests(UUID uuid) {
        List<Quest> all = new ArrayList<>();
        all.addAll(getPlayerQuests(uuid));
        all.addAll(getPlayerWeeklyQuests(uuid));
        all.addAll(getGlobalQuests()); // include global quests too
        return all;
    }

    public void ensureInitialAssignments() {
        int dailyQuestCount = plugin.getConfig().getInt("QuestLimits.DAILY", 5);
        int weeklyQuestCount = plugin.getConfig().getInt("QuestLimits.WEEKLY", 7);
    
        for (UUID uuid : getAllPlayers()) {
            if (getPlayerQuests(uuid).isEmpty()) {
                List<Quest> daily = plugin.getQuestAssigner().getRandomQuestsWeighted(uuid, QuestTier.DAILY, dailyQuestCount);
                assignNewDailyQuests(uuid, daily);
                plugin.debug("[Assign] Assigned new DAILY quests to " + uuid + ". Total = " + dailyQuestCount);
            }
            if (getPlayerWeeklyQuests(uuid).isEmpty()) {
                List<Quest> weekly = plugin.getQuestAssigner().getRandomQuestsWeighted(uuid, QuestTier.WEEKLY, weeklyQuestCount);
                assignNewWeeklyQuests(uuid, weekly);
                plugin.debug("[Assign] Assigned new WEEKLY quests to " + uuid + ". Total = " + weeklyQuestCount);
            }
        }
    }

    public void assignNewDailyQuests(UUID uuid, List<Quest> quests) {
        dailyQuests.put(uuid, quests); // existing
        playerDailyQuests.put(uuid, quests); // required for GUI
    }

    public void assignNewWeeklyQuests(UUID uuid, List<Quest> quests) {
        weeklyQuests.put(uuid, quests); // existing
        playerWeeklyQuests.put(uuid, quests); // required for GUI
    }

    public void assignGlobalQuests(UUID uuid, List<Quest> quests) {
        playerGlobalQuests.put(uuid, quests); // needed for GUI
    }

    public Set<UUID> getAllPlayers() {
        Set<UUID> set = new HashSet<>();
        set.addAll(dailyQuests.keySet());
        set.addAll(weeklyQuests.keySet());
        return set;
    }

    public void setGlobalQuests(List<Quest> quests) {
        this.globalQuests.clear();
        this.globalQuests.addAll(quests);
    }

    public List<Quest> getGlobalQuests() {
        return globalQuests;
    }
} 
