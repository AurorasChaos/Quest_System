package com.example.questplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class QuestLoader {

    private final QuestPlugin plugin;
    private final Map<String, QuestTemplate> questTemplates = new HashMap<>();

    public QuestLoader(QuestPlugin plugin) {
        this.plugin = plugin;
        loadTemplates();
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

    public List<QuestTemplate> getAllTemplates() {
        return new ArrayList<>(questTemplates.values());
    }

    public List<QuestTemplate> getTemplatesByTier(QuestTier tier) {
        return questTemplates.values().stream()
                .filter(q -> q.getTier() == tier)
                .toList();
    }
}
