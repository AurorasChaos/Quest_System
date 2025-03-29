package com.example.questplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;

public class QuestNotifier {

    @SuppressWarnings("unused")
    private final QuestPlugin plugin;

    public QuestNotifier(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Notifies the player about quest progress using the action bar.
     * This triggers every 10% milestone (excluding 0% and 100%).
     */
    public void notifyProgress(Player player, Quest quest) {
        double percent = (double) quest.getCurrentProgress() / quest.getTargetAmount();
        int percentage = (int) (percent * 100);

        if (percentage % 10 == 0 && percentage != 0 && !quest.isCompleted()) {
            Component actionBar = Component.text()
                    .append(Component.text("Quest: ", NamedTextColor.YELLOW))
                    .append(Component.text(quest.getDescription(), NamedTextColor.GOLD))
                    .append(Component.text(" [" + percentage + "%]", NamedTextColor.YELLOW))
                    .build();

            plugin.adventure().player(player).sendActionBar(actionBar);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        }
    }

    /**
     * Shows a title to the player when a quest is completed.
     */
    public void notifyCompletion(Player player, Quest quest) {
        player.sendTitle("✔ Quest Complete!", null, 500, 3000, 500);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }
}