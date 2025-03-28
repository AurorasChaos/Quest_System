package com.example.questplugin;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class QuestNotifier {
    private final QuestPlugin plugin;

    public QuestNotifier(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    public void notifyProgress(Player player, Quest quest) {
        double percent = (double) quest.getCurrentProgress() / quest.getTargetAmount();
        int percentage = (int) (percent * 100);
        if (percentage % 10 == 0 && percentage != 0 && !quest.isCompleted()) {
            player.sendActionBar(ChatColor.YELLOW + "Quest: " + quest.getDescription() + " [" + percentage + "%]");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        }
    }

    public void notifyCompletion(Player player, Quest quest) {
        player.sendTitle(
            ChatColor.GREEN + "✔ Quest Complete!",
            ChatColor.GOLD + quest.getDescription(),
            10, 60, 10
        );
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }
}
