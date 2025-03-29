package com.example.questplugin;

import java.util.*;

public class QuestManager {

    private final Map<UUID, List<Quest>> dailyQuests = new HashMap<>();
    private final Map<UUID, List<Quest>> weeklyQuests = new HashMap<>();
    @SuppressWarnings("unused")
    private final QuestPlugin plugin;

    public QuestManager(QuestPlugin plugin) {
        this.plugin = plugin;
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
            case GLOBAL -> new ArrayList<>(); // Placeholder
        };
    }

    public void assignNewDailyQuests(UUID uuid, List<Quest> quests) {
        dailyQuests.put(uuid, quests);
    }

    public void assignNewWeeklyQuests(UUID uuid, List<Quest> quests) {
        weeklyQuests.put(uuid, quests);
    }

    public Map<UUID, List<Quest>> getAllDaily() {
        return dailyQuests;
    }

    public Map<UUID, List<Quest>> getAllWeekly() {
        return weeklyQuests;
    }
}
