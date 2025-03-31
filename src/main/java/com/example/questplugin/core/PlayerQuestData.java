package com.example.questplugin.core;

import com.example.questplugin.Quest;
import java.util.Collections;
import java.util.List;

/**
 * Immutable container for a player's quest progress data.
 * Used by QuestStorageService for batch save/load operations.
 */
public final class PlayerQuestData {
    private final List<Quest> dailyQuests;
    private final List<Quest> weeklyQuests;
    private final long lastUpdated;

    public PlayerQuestData(List<Quest> dailyQuests, List<Quest> weeklyQuests) {
        this.dailyQuests = Collections.unmodifiableList(dailyQuests);
        this.weeklyQuests = Collections.unmodifiableList(weeklyQuests);
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters
    public List<Quest> getDailyQuests() {
        return dailyQuests;
    }

    public List<Quest> getWeeklyQuests() {
        return weeklyQuests;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // Utility methods
    public boolean isEmpty() {
        return dailyQuests.isEmpty() && weeklyQuests.isEmpty();
    }

    public boolean hasCompletedDaily() {
        return dailyQuests.stream().allMatch(Quest::isCompleted);
    }

    public boolean hasCompletedWeekly() {
        return weeklyQuests.stream().allMatch(Quest::isCompleted);
    }

    @Override
    public String toString() {
        return String.format(
            "PlayerQuestData[daily=%d, weekly=%d, updated=%tc]",
            dailyQuests.size(),
            weeklyQuests.size(),
            lastUpdated
        );
    }
}