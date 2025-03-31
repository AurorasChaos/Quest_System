public class QuestValidator {
    public static boolean isQuestValid(Quest quest) {
        return quest != null && 
               quest.getId() != null && 
               quest.getTargetAmount() > 0;
    }
}