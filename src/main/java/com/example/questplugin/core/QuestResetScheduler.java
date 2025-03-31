public class QuestResetScheduler {
    private final BukkitScheduler scheduler;
    
    public void startDailyReset() {
        scheduler.runTaskTimer(plugin, () -> {
            plugin.getQuestManager().resetDailyQuests();
        }, 0L, 20L * 60 * 60 * 24); // 24h
    }
}