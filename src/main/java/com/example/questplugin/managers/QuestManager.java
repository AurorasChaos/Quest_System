// QuestManager with global quest persistence and saving support
package com.example.questplugin.managers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.core.PlayerQuestData;
import com.example.questplugin.enums.QuestRarity;
import com.example.questplugin.enums.QuestTier;
import com.example.questplugin.enums.QuestType;
import com.example.questplugin.models.Quest;
import com.example.questplugin.models.QuestTemplate;
import com.google.common.cache.Cache;

public class QuestManager {

    private final QuestPlugin plugin;
    private final Map<UUID, List<Quest>> dailyQuests = new ConcurrentHashMap<>();
    private final Map<UUID, List<Quest>> weeklyQuests = new ConcurrentHashMap<>();
    private final List<Quest> globalQuests = new ArrayList<>();

    private final File globalFile;
    private final FileConfiguration globalConfig;

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

    public List<Quest> getPlayerWeeklyQuests(UUID uuid) {
        return weeklyQuests.getOrDefault(uuid, new ArrayList<>());
    }

    public List<Quest> getQuestsForTier(UUID uuid, QuestTier tier) {
        return switch (tier) {
            case DAILY -> dailyQuests.getOrDefault(uuid, new ArrayList<>());
            case WEEKLY -> weeklyQuests.getOrDefault(uuid, new ArrayList<>());
            case GLOBAL -> playerGlobalQuests.getOrDefault(uuid, new ArrayList<>());
            case ALL -> {
                List<Quest> all = new ArrayList<>();
                all.addAll(dailyQuests.getOrDefault(uuid, Collections.emptyList()));
                all.addAll(weeklyQuests.getOrDefault(uuid, Collections.emptyList()));
                all.addAll(playerGlobalQuests.getOrDefault(uuid, Collections.emptyList()));
                yield all;
            }
        };
    }

    public List<Quest> getAllPlayerQuests(UUID uuid) {
        List<Quest> all = new ArrayList<>();
        all.addAll(getPlayerDailyQuests(uuid));
        all.addAll(getPlayerWeeklyQuests(uuid));
        all.addAll(getGlobalQuests()); // include global quests too
        return all;
    }

    public List<Quest> getPlayerDailyQuests(UUID uuid) {
        return dailyQuests.getOrDefault(uuid, new ArrayList<>());
    }

    public void ensureInitialAssignments() {
        int dailyQuestCount = plugin.getConfig().getInt("QuestLimits.DAILY", 7);
        int weeklyQuestCount = plugin.getConfig().getInt("QuestLimits.WEEKLY", 7);
    
        for (UUID uuid : getAllPlayers()) {
            if (getPlayerDailyQuests(uuid).isEmpty()) {
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

        assignInitialGlobalQuests();
    }


    public void assignNewDailyQuests(UUID uuid, List<Quest> quests) {
        dailyQuests.put(uuid, quests); // existing
    }

    public void assignNewWeeklyQuests(UUID uuid, List<Quest> quests) {
        weeklyQuests.put(uuid, quests); // existing
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

    public void loadTemplates() {
        File file = new File(plugin.getDataFolder(), "quests.yml");
        if (!file.exists()) plugin.saveResource("quests.yml", false);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String id : config.getConfigurationSection("quests").getKeys(false)) {
            String path = "quests." + id;
            String description = config.getString(path + ".description");
            QuestType type = QuestType.valueOf(config.getString(path + ".type", "CUSTOM"));
            String targetKey = config.getString(path + ".target_key", null);
            int target_amount = config.getInt(path + ".target_amount", 1);
            double currency = config.getDouble(path + ".currency", 0);
            int skill = config.getInt(path + ".skill_points", 0);
            String skillType = config.getString(path + ".skill_type", null);
            int skillXp = config.getInt(path + ".skill_xp", 0);
            QuestTier tier = QuestTier.valueOf(config.getString(path + ".tier", "DAILY"));
            QuestRarity rarity = QuestRarity.fromString(config.getString(path + ".rarity", "COMMON"));

            QuestTemplate template = new QuestTemplate(
                id, description, type, targetKey, target_amount,
                currency, skill, skillType, skillXp, tier, rarity
            );
            questTemplates.put(id, template);
        }
    }

    public List<QuestTemplate> getTemplatesByTier(QuestTier tier) {
        return questTemplates.values().stream()
                .filter(q -> q.getTier() == tier)
                .toList();
    }

    

    private LocalDate lastResetDate;

    private File resetFile;

    private YamlConfiguration config;

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
            plugin.getQuestStorageService().savePlayerQuests(uuid, daily, weekly);
        }
    
        plugin.getQuestStorageService().save();
    }

    public void performWeeklyReset() {
        Bukkit.getLogger().info("[QuestPlugin] Performing weekly quest reset.");
    
        // Now reset weekly quests for players
        for (UUID uuid : plugin.getQuestManager().getAllPlayers()) {
            List<Quest> weekly = plugin.getQuestAssigner().assignWeeklyQuests(uuid);
            List<Quest> daily = plugin.getQuestManager().getPlayerDailyQuests(uuid);
            plugin.getQuestManager().assignNewWeeklyQuests(uuid, weekly);
            plugin.getQuestStorageService().savePlayerQuests(uuid, daily, weekly);
        }
    
        plugin.getQuestStorageService().save();
        Bukkit.getLogger().info("[QuestPlugin] Weekly reset complete.");
    }

    public void refreshGlobalQuests() {
        // Reset global quests that are completed
        List<Quest> currentGlobal = plugin.getQuestManager().getGlobalQuests();
        int globalLimit = plugin.getConfig().getInt("QuestLimits.GLOBAL", 10);
        List<Quest> newGlobals = new ArrayList<>(currentGlobal);
    
        List<QuestTemplate> allGlobalTemplates = plugin.getQuestManager().getTemplatesByTier(QuestTier.GLOBAL);
        Set<String> usedIds = currentGlobal.stream()
            .filter(q -> !q.isCompleted()) // preserve incomplete ones
            .map(Quest::getId)
            .collect(Collectors.toSet());
    
        // Remove completed ones
        newGlobals.removeIf(Quest::isCompleted);
    
        // Fill back up to the limit

        List<QuestTemplate> available = new ArrayList<>(
            allGlobalTemplates.stream()
                .filter(template -> !usedIds.contains(template.getId()))
                .toList()
        );
        Collections.shuffle(available);
        for (QuestTemplate template : available) {
            if (newGlobals.size() >= globalLimit) break;
            newGlobals.add(template.toQuest());
        }
    
        plugin.getQuestManager().setGlobalQuests(newGlobals);
    }

    public void assignInitialGlobalQuests() {
        if (!globalQuests.isEmpty()) return;
        refreshGlobalQuests();
    }

    public void giveDevQuest(UUID uuid, Quest quest) {
        List<Quest> existing = plugin.getQuestManager().getPlayerDailyQuests(uuid);
        boolean alreadyHas = existing.stream().anyMatch(q -> q.getId().equalsIgnoreCase(quest.getId()));
        if (!alreadyHas) {
            existing.add(quest);
            plugin.getQuestStorageService().savePlayerQuests(uuid, existing, plugin.getQuestManager().getPlayerWeeklyQuests(uuid));
            plugin.debug("[Dev] Added quest '" + quest.getId() + "' to player " + uuid);
        } else {
            plugin.debug("[Dev] Player already has quest '" + quest.getId() + "'");
        }
    }

    private final Cache<UUID, PlayerQuestData> questCache = Caffeine.newBuilder()
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build();

    public PlayerQuestData getPlayerData(UUID uuid) {
        return questCache.get(uuid, 
            k -> storageService.loadPlayerData(uuid).join());
    }


    //DEV-ADMIN TOOLS BELOW

    public void simulateDailyReset() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            assignNewDailyQuests(p.getUniqueId(), 
                plugin.getQuestAssigner().getRandomQuestsWeighted(
                    p.getUniqueId(), 
                    QuestTier.DAILY, 
                    plugin.getConfigManager().getDailyQuestLimit()
                )
            );
        });
    }

    public void dumpCacheToConsole() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dumpCacheToConsole'");
    }
}