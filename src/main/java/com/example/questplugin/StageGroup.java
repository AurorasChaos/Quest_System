package com.questsystem.model;

import java.util.ArrayList;
import java.util.List;

public class StageGroup {
    public enum LogicType {
        AND, OR
    }

    private LogicType logic;
    private List<Object> entries; // Can be QuestStage or nested StageGroup

    public StageGroup(LogicType logic) {
        this.logic = logic;
        this.entries = new ArrayList<>();
    }

    public void addEntry(Object entry) {
        entries.add(entry);
    }

    public List<Object> getEntries() {
        return entries;
    }

    public LogicType getLogic() {
        return logic;
    }

    public boolean isComplete(PlayerQuestProgress progress, int baseIndex) {
        int index = baseIndex * 100;
        int completed = 0;

        for (Object entry : entries) {
            if (entry instanceof QuestStage) {
                if (progress.isStageComplete(index)) {
                    completed++;
                }
                index++;
            } else if (entry instanceof StageGroup nested) {
                if (nested.isComplete(progress, index)) {
                    completed++;
                }
            }
        }

        return switch (logic) {
            case AND -> completed == entries.size();
            case OR -> completed > 0;
        };
    }
}
