package com.example.questplugin.model;

import com.example.questplugin.util.EntityCategoryMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Quest {

    private final String id;
    private final String description;
    private final QuestType type;
    private final String targetKey;
    private final int targetAmount;
    private final double currencyReward;
    private final QuestTier tier;
    private final QuestRarity rarity;

    private final UUID playerUUID;
    private final List<QuestTemplate.Objective> objectives = new ArrayList<>();

    private int progress = 0;
    private boolean rewardClaimed = false;

    private final String skillType;
    private final int skillXp;

    public Quest(QuestTemplate template, UUID playerUUID) {
        this.playerUUID = playerUUID;
        for (QuestTemplate.Objective obj : template.getObjectives()){
            objectives.add(new QuestTemplate.Objective(obj.getObjectiveType(), obj.getObjectiveTargetKey(), obj.getObjectiveTargetAmount()));
        }
        this.id = template.getId();
        this.description = template.getDescription();
        this.type = template.getType();
        this.targetKey = template.getTargetKey();
        this.targetAmount = template.getTargetAmount();
        this.currencyReward = template.getCurrenyReward();
        this.skillType = template.getSkillType();
        this.skillXp = template.getSkillXp();
        this.tier = template.getQuestTier();
        this.rarity = template.getQuestRarity();
    }
    
    public String getId() { return id; }
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public String getTargetKey() { return targetKey; }
    public int getTargetAmount() { return targetAmount; }
    public int getCurrentProgress() { return progress; }
    public boolean isCompleted() { return progress >= targetAmount; }
    public boolean isRewardClaimed() { return rewardClaimed; }
    public double getCurrencyReward() { return currencyReward; }
    public QuestTier getTier() { return tier; }
    public QuestRarity getRarity() { return rarity; }
    public String getSkillType() { return skillType; }
    public List<QuestTemplate.Objective> getQuestObjectives() {return objectives;}
    public int getSkillXp() { return skillXp; }

    public void incrementProgress(int amount) {
        if (!isCompleted()) {
            this.progress += amount;
        }
    }

    public void setCurrentProgress(int amount) {
        this.progress = amount;
    }

    public void claimReward() {
        this.rewardClaimed = true;
    }

    public boolean canClaim() {
        return isCompleted() && !isRewardClaimed();
    }

    public boolean matchesTarget(String inputKey) {
        return switch (this.getType()) {
            case KILL_MOB -> EntityCategoryMatcher.matches(this.getTargetKey(), inputKey);
            case GATHER_ITEM, MINE_BLOCK, PLACE_BLOCK -> this.getTargetKey().equalsIgnoreCase(inputKey);
            default -> this.getTargetKey().equalsIgnoreCase(inputKey);
        };
    }
}
