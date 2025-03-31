package com.example.questplugin.core;

import com.example.questplugin.QuestPlugin;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class QuestStorageService {
    private final QuestPlugin plugin;
    private final File playerDataFile;
    private final File globalDataFile;

    public QuestStorageService(QuestPlugin plugin) {
        this.plugin = plugin;
        this.playerDataFile = new File(plugin.getDataFolder(), "player_quests.db");
        this.globalDataFile = new File(plugin.getDataFolder(), "global_quests.db");
        initializeDatabase();
    }

    // Async Operations
    public CompletableFuture<Void> savePlayerData(UUID uuid, PlayerQuestData data) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                // Upsert player progress
                String sql = """
                    INSERT INTO player_quests(uuid, quest_id, progress, claimed, tier) 
                    VALUES(?,?,?,?,?)
                    ON CONFLICT(uuid, quest_id) DO UPDATE SET
                        progress=excluded.progress,
                        claimed=excluded.claimed
                    """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (Quest quest : data.getDailyQuests()) {
                        saveQuest(stmt, uuid, quest, QuestTier.DAILY);
                    }
                    for (Quest quest : data.getWeeklyQuests()) {
                        saveQuest(stmt, uuid, quest, QuestTier.WEEKLY);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<PlayerQuestData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Quest> daily = new ArrayList<>();
            List<Quest> weekly = new ArrayList<>();

            try (Connection conn = getConnection()) {
                String sql = "SELECT * FROM player_quests WHERE uuid = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        Quest quest = loadQuestFromResultSet(rs);
                        if (rs.getString("tier").equals("DAILY")) {
                            daily.add(quest);
                        } else {
                            weekly.add(quest);
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load player data: " + e.getMessage());
            }

            return new PlayerQuestData(daily, weekly);
        });
    }

    // Global Quest Storage
    public void saveGlobalQuests(List<Quest> quests) {
        // Similar async implementation for global quests
    }

    private void initializeDatabase() {
        if (!playerDataFile.exists()) {
            try (Connection conn = getConnection()) {
                conn.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS player_quests (
                        uuid TEXT NOT NULL,
                        quest_id TEXT NOT NULL,
                        progress INTEGER NOT NULL,
                        claimed BOOLEAN NOT NULL,
                        tier TEXT NOT NULL,
                        PRIMARY KEY (uuid, quest_id)
                    )""");
                
                conn.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS global_quest_progress (
                        quest_id TEXT PRIMARY KEY,
                        progress INTEGER NOT NULL
                    )""");
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            }
        }
    }

    // Helper Methods
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + playerDataFile.getPath());
    }

    private void saveQuest(PreparedStatement stmt, UUID uuid, Quest quest, QuestTier tier) 
        throws SQLException {
        stmt.setString(1, uuid.toString());
        stmt.setString(2, quest.getId());
        stmt.setInt(3, quest.getCurrentProgress());
        stmt.setBoolean(4, quest.isRewardClaimed());
        stmt.setString(5, tier.name());
        stmt.executeUpdate();
    }

    private Quest loadQuestFromResultSet(ResultSet rs) throws SQLException {
        QuestTemplate template = plugin.getQuestLoader()
            .getAllTemplates().stream()
            .filter(t -> t.getId().equals(rs.getString("quest_id")))
            .findFirst()
            .orElseThrow();
        
        Quest quest = template.toQuest();
        quest.setCurrentProgress(rs.getInt("progress"));
        if (rs.getBoolean("claimed")) quest.claimReward();
        return quest;
    }

    //tmp for migration
    public CompletableFuture<Void> migrateFromYaml() {
        return CompletableFuture.runAsync(() -> {
            File yamlFile = new File(plugin.getDataFolder(), "player_quests.yml");
            if (yamlFile.exists()) {
                // Convert YAML data to SQLite
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(yamlFile);
                // ... migration logic ...
                yamlFile.renameTo(new File(plugin.getDataFolder(), "player_quests.backup.yml"));
            }
        }
    );

    public CompletableFuture<Void> saveWithRetry(UUID uuid, PlayerQuestData data, int maxRetries) {
        return CompletableFuture.runAsync(() -> {
            int attempts = 0;
            while (attempts < maxRetries) {
                try {
                    savePlayerData(uuid, data).join();
                    break;
                } catch (Exception e) {
                    attempts++;
                    Thread.sleep(1000 * attempts); // Exponential backoff
                }
            }
        });
    }
}