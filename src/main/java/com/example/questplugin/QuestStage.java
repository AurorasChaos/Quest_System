package com.questsystem.model;

import java.util.List;

public class QuestStage {
    private QuestType type;
    private String target;
    private int amount;
    private String description;

    private boolean isReference;
    private String questRefId;

    private List<String> onComplete; // Hooks or commands to execute

    public boolean isReference() {
        return isReference;
    }

    public String getQuestRefId() {
        return questRefId;
    }

    public QuestType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getOnComplete() {
        return onComplete;
    }

    // Setters can be added as needed depending on loader implementation
}
