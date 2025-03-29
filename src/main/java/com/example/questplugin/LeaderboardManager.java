package com.example.questplugin;

import java.util.*;

public class LeaderboardManager {

    private final Map<UUID, Integer> totalCompleted = new HashMap<>();
    private final Map<UUID, Map<QuestType, Integer>> typeCompleted = new HashMap<>();
    private final Map<UUID, Integer> monthlyTotal = new HashMap<>();

    public void recordCompletion(UUID uuid, Quest quest) {
        totalCompleted.put(uuid, totalCompleted.getOrDefault(uuid, 0) + 1);
        monthlyTotal.put(uuid, monthlyTotal.getOrDefault(uuid, 0) + 1);

        typeCompleted.putIfAbsent(uuid, new EnumMap<>(QuestType.class));
        Map<QuestType, Integer> typeMap = typeCompleted.get(uuid);
        typeMap.put(quest.getType(), typeMap.getOrDefault(quest.getType(), 0) + 1);
    }

    public int getTotal(UUID uuid) {
        return totalCompleted.getOrDefault(uuid, 0);
    }

    public int getTypeCount(UUID uuid, QuestType type) {
        return typeCompleted.getOrDefault(uuid, Map.of()).getOrDefault(type, 0);
    }

    public List<Map.Entry<UUID, Integer>> getTop(int limit) {
        return totalCompleted.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(limit)
            .toList();
    }

    public List<Map.Entry<UUID, Integer>> getMonthlyTop(int limit) {
        return monthlyTotal.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(limit)
            .toList();
    }

    public void resetMonthlyTopAndReward() {
        monthlyTotal.clear();
    }
}
