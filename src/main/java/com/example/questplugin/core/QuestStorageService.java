package com.example.questplugin.core;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.enums.QuestTier;
import com.example.questplugin.models.Quest;
import com.example.questplugin.models.QuestTemplate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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

    // ========== Player Data Operations ==========
    public CompletableFuture<Void> savePlayerData(UUID uuid, PlayerQuestData data) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                
                // Clear existing data for this player
                try (PreparedStatement clearStmt = conn.prepareStatement(
                    "DELETE FROM player_quests WHERE uuid = ?")) {
                    clearStmt.setString(1, uuid.toString());
                    clearStmt.executeUpdate();
                }

                // Insert new data
                String insertSql = """
                    INSERT INTO player_quests(uuid, quest_id, progress, claimed, tier) 
                    VALUES(?,?,?,?,?)
                    """;
                
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (Quest quest : data.getDailyQuests()) {
                        saveQuest(insertStmt, uuid, quest, QuestTier.DAILY);
                    }
                    for (Quest quest : data.getWeeklyQuests()) {
                        saveQuest(insertStmt, uuid, quest, QuestTier.WEEKLY);
                    }
                    conn.commit();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + uuid, e);
                throw new RuntimeException(e);
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
                        if (QuestTier.valueOf(rs.getString("tier")) == QuestTier.DAILY) {
                            daily.add(quest);
                        } else {
                            weekly.add(quest);
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + uuid, e);
            }

            return new PlayerQuestData(daily, weekly);
        });
    }

    // ========== Global Quest Operations ==========
    public CompletableFuture<Void> saveGlobalQuests(List<Quest> quests) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                
                // Clear existing global quests
                try (Statement clearStmt = conn.createStatement()) {
                    clearStmt.execute("DELETE FROM global_quest_progress");
                }

                // Insert current progress
                try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO global_quest_progress(quest_id, progress) VALUES(?,?)")) {
                    for (Quest quest : quests) {
                        stmt.setString(1, quest.getId());
                        stmt.setInt(2, quest.getCurrentProgress());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                    conn.commit();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save global quests", e);
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<Quest>> loadGlobalQuests() {
        return CompletableFuture.supplyAsync(() -> {
            List<Quest> globalQuests = new ArrayList<>();
            
            try (Connection conn = getConnection()) {
                String sql = "SELECT * FROM global_quest_progress";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        String questId = rs.getString("quest_id");
                        int progress = rs.getInt("progress");
                        
                        plugin.getQuestLoader().getAllTemplates().stream()
                            .filter(t -> t.getId().equals(questId))
                            .findFirst()
                            .ifPresent(template -> {
                                Quest quest = template.toQuest();
                                quest.setCurrentProgress(progress);
                                globalQuests.add(quest);
                            });
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load global quests", e);
            }

            return globalQuests;
        });
    }

    // ========== Migration ==========
    public CompletableFuture<Void> migrateFromYaml() {
        return CompletableFuture.runAsync(() -> {
            File yamlFile = new File(plugin.getDataFolder(), "player_quests.yml");
            if (!yamlFile.exists()) return;

            try {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(yamlFile);
                try (Connection conn = getConnection()) {
                    conn.setAutoCommit(false);
                    
                    // Process each player in YAML
                    for (String uuidStr : yaml.getKeys(false)) {
                        UUID uuid = UUID.fromString(uuidStr);
                        
                        // Migrate daily quests
                        migratePlayerQuests(conn, uuid, yaml.getConfigurationSection(uuidStr + ".daily"), QuestTier.DAILY);
                        
                        // Migrate weekly quests
                        migratePlayerQuests(conn, uuid, yaml.getConfigurationSection(uuidStr + ".weekly"), QuestTier.WEEKLY);
                    }
                    
                    conn.commit();
                    yamlFile.renameTo(new File(plugin.getDataFolder(), "player_quests.backup.yml"));
                    plugin.getLogger().info("Successfully migrated YAML data to SQLite");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Migration failed", e);
                throw new RuntimeException(e);
            }
        });
    }

    private void migratePlayerQuests(Connection conn, UUID uuid, ConfigurationSection section, QuestTier tier) 
            throws SQLException {
        if (section == null) return;
        
        String sql = "INSERT INTO player_quests(uuid, quest_id, progress, claimed, tier) VALUES(?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String questId : section.getKeys(false)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, questId);
                stmt.setInt(3, section.getInt(questId + ".progress"));
                stmt.setBoolean(4, section.getBoolean(questId + ".claimed"));
                stmt.setString(5, tier.name());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // ========== Utility Methods ==========
    public CompletableFuture<Void> saveWithRetry(UUID uuid, PlayerQuestData data, int maxRetries) {
        return CompletableFuture.runAsync(() -> {
            int attempts = 0;
            while (attempts < maxRetries) {
                try {
                    savePlayerData(uuid, data).join();
                    return;
                } catch (Exception e) {
                    attempts++;
                    try {
                        Thread.sleep(1000L * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                }
            }
            throw new RuntimeException("Failed after " + maxRetries + " attempts");
        });
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "player_quests", null);
            
            if (!tables.next()) {
                conn.createStatement().execute("""
                    CREATE TABLE player_quests (
                        uuid TEXT NOT NULL,
                        quest_id TEXT NOT NULL,
                        progress INTEGER NOT NULL,
                        claimed BOOLEAN NOT NULL,
                        tier TEXT NOT NULL,
                        PRIMARY KEY (uuid, quest_id)
                    )""");
                
                conn.createStatement().execute("""
                    CREATE TABLE global_quest_progress (
                        quest_id TEXT PRIMARY KEY,
                        progress INTEGER NOT NULL)
                    """);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException(e);
        }
    }

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
            .orElseThrow(() -> new SQLException("Unknown quest template: " + rs.getString("quest_id")));
        
        Quest quest = template.toQuest();
        quest.setCurrentProgress(rs.getInt("progress"));
        if (rs.getBoolean("claimed")) quest.claimReward();
        return quest;
    }
}