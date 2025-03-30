package com.example.questplugin;

public class Quest {

    private final String id;
    private final String description;
    private final QuestType type;
    private final String targetKey;
    private final int targetAmount;
    private final double currencyReward;
    private final int skillPointReward;
    private final QuestTier tier;
    private final QuestRarity rarity;

    private int progress = 0;
    private boolean rewardClaimed = false;

    private String skillType;
    private int skillXp;

    public Quest(String id, String description, QuestType type, String targetKey, int targetAmount,
    double currencyReward, int skillPointReward, String skillType, int skillXp,
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
    
    public String getId() { return id; }
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public String getTargetKey() { return targetKey; }
    public int getTargetAmount() { return targetAmount; }
    public int getCurrentProgress() { return progress; }
    public boolean isCompleted() { return progress >= targetAmount; }
    public boolean isRewardClaimed() { return rewardClaimed; }
    public double getCurrencyReward() { return currencyReward; }
    public int getSkillPointReward() { return skillPointReward; }
    public QuestTier getTier() { return tier; }
    public QuestRarity getRarity() { return rarity; }
    public String getSkillType() { return skillType; }
    public void setSkillType(String skillType) { this.skillType = skillType; }

    public int getSkillXp() { return skillXp; }
    public void setSkillXp(int skillXp) { this.skillXp = skillXp; }

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
