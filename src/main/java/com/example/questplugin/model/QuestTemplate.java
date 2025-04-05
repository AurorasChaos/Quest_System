package com.example.questplugin.model;

import org.bukkit.configuration.ConfigurationSection;

import java.lang.module.Configuration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestTemplate {

    private final String id;
    private final String description;
    private final QuestType type;
    private final String targetKey;
    private final int targetAmount;
    private final double currencyReward;
    private final int skillPointReward;
    private final String skillType;
    private final int skillXp;
    private final QuestTier tier;
    private final QuestRarity rarity;
    private final List<Objective> objectives = new ArrayList<>();

    public static class Objective {
        private final QuestType type;
        private final String targetKey;
        private final int targetAmount;
        private int progress = 0;

        public Objective(QuestType type, String targetKey, int targetAmount){
            this.type = type;
            this.targetKey = targetKey;
            this.targetAmount = targetAmount;
        }

        public QuestType getObjectiveType(){ return type;}
        public String getObjectiveTargetKey(){ return targetKey;}
        public int getObjectiveTargetAmount() {return targetAmount;}
        public int getProgress() {return progress;}
        public void incrementProgress(int amountToProgres) {
            progress += amountToProgres;
        }
        public void setProgress(int progress) {
            this.progress = progress;
        }
    }

    public QuestTemplate(ConfigurationSection section) {
        this.id = section.getString("id");
        this.description = section.getString("description");
        this.type = QuestType.valueOf(section.getString("type"));
        this.targetKey = section.getString("target_key");
        this.targetAmount = section.getInt("target_amount");
        this.currencyReward = section.getInt("currency");
        this.skillPointReward = section.getInt("skill_points");
        this.skillType = section.getString("skill_type");
        this.skillXp = section.getInt("skill_xp");
        this.tier = QuestTier.valueOf(section.getString("tier"));
        this.rarity = QuestRarity.valueOf(section.getString("rarity"));

        for (String objective : section.getConfigurationSection("objectives").getKeys(false)){
            ConfigurationSection objSection = section.getConfigurationSection("objectives." + objective);
            objectives.add(new Objective(
                    QuestType.valueOf(objSection.getString("type")),
                    objSection.getString("target_key"),
                    objSection.getInt("target_amount")
            ));
        }

    }

    public String getId() {
        return id;
    }
    public String getDescription() {return description;}
    public QuestType getType() {return type;}
    public String getTargetKey() {return targetKey;}
    public int getTargetAmount() {return targetAmount;}
    public double getCurrenyReward() {return currencyReward;}
    public int getSkillPointReward() {return skillPointReward;}
    public String getSkillType() {return skillType;}
    public int getSkillXp() {return skillXp;}
    public QuestTier getQuestTier() {return tier;}
    public QuestRarity getQuestRarity() {return rarity;}
    public List<Objective> getObjectives(){
        return objectives;
    }

    public Quest toQuest(){
        return new Quest(this, UUID.randomUUID());
    }

}
