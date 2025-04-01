package com.example.questplugin.models;

import com.example.questplugin.enums.QuestRarity;
import com.example.questplugin.enums.QuestTier;
import com.example.questplugin.enums.QuestType;

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

    public QuestTemplate(String id, String description, QuestType type, String targetKey,
                         int targetAmount, double currencyReward, int skillPointReward,
                         String skillType, int skillXp,
                         QuestTier tier, QuestRarity rarity) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.targetKey = targetKey;
        this.targetAmount = targetAmount;
        this.currencyReward = currencyReward;
        this.skillPointReward = skillPointReward;
        this.skillType = skillType;
        this.skillXp = skillXp;
        this.tier = tier;
        this.rarity = rarity;
    }

    public Quest toQuest() {
        return new Quest(id, description, type, targetKey, targetAmount,
                         currencyReward, skillPointReward, skillType, skillXp, tier, rarity);
    }

    public QuestTier getTier() {
        return tier;
    }

    public QuestRarity getRarity() {
        return rarity;
    }

    public String getId() {
        return id;
    }
}
