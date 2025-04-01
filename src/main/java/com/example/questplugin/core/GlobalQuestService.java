package com.example.questplugin.core;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.enums.QuestTier;
import com.example.questplugin.models.Quest;
import com.example.questplugin.models.QuestTemplate;

import org.bukkit.Bukkit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalQuestService {
    private final QuestPlugin plugin;
    private final List<Quest> activeQuests = new ArrayList<>();
    private final Map<UUID, Set<String>> playerCompletions = new ConcurrentHashMap<>();
    private final int globalQuestLimit;

    public GlobalQuestService(QuestPlugin plugin) {
        this.plugin = plugin;
        this.globalQuestLimit = plugin.getConfigManager().getGlobalQuestLimit();
        loadGlobalQuests();
    }

    public List<Quest> getActiveQuests() {
        return Collections.unmodifiableList(activeQuests);
    }

    public void refreshGlobalQuests() {
        List<Quest> newQuests = new ArrayList<>();
        List<QuestTemplate> allTemplates = plugin.getQuestManager().getTemplatesByTier(QuestTier.GLOBAL);

        // Keep incomplete quests
        activeQuests.removeIf(Quest::isCompleted);
        
        // Add new quests up to limit
        Collections.shuffle(allTemplates);
        for (QuestTemplate template : allTemplates) {
            if (newQuests.size() >= globalQuestLimit) break;
            if (activeQuests.stream().noneMatch(q -> q.getId().equals(template.getId()))) {
                newQuests.add(template.toQuest());
            }
        }

        activeQuests.addAll(newQuests);
        plugin.debug("[Global] Refreshed quests. New: " + newQuests.size());
    }

    public void recordCompletion(UUID playerId, Quest quest) {
        playerCompletions.computeIfAbsent(playerId, k -> new HashSet<>())
                         .add(quest.getId());
    }

    public boolean hasCompleted(UUID playerId, String questId) {
        return playerCompletions.getOrDefault(playerId, Collections.emptySet())
                               .contains(questId);
    }

    private void loadGlobalQuests() {
        // Initialize with default quests if empty
        if (activeQuests.isEmpty()) {
            refreshGlobalQuests();
        }
    }
}