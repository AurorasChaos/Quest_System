package com.example.questplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.example.questplugin.QuestPlugin;
import com.example.questplugin.enums.QuestFilter;
import com.example.questplugin.enums.QuestTier;
import com.example.questplugin.ui.QuestGUI;

public class QuestCommand implements CommandExecutor {

    private final QuestPlugin plugin;

    public QuestCommand(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        player.sendMessage("Opening quests..."); // Debug message
        
        // Open the QuestGUI
        QuestGUI gui = new QuestGUI(plugin);
        gui.open(player, 1, QuestTier.DAILY, QuestFilter.ALL); // or switch tab logic
        return true;
    }
}