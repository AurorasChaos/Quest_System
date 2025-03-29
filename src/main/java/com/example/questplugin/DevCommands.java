package com.example.questplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class DevCommands implements CommandExecutor {

    private final QuestPlugin plugin;

    public DevCommands(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!player.hasPermission("questplugin.dev")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "/questdev reload - Reload quests");
            player.sendMessage(ChatColor.YELLOW + "/questdev give <quest_id> - Give quest");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.getQuestLoader().loadTemplates();
                player.sendMessage(ChatColor.GREEN + "Reloaded quest templates.");
            }
            case "give" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /questdev give <quest_id>");
                    return true;
                }
                String id = args[1];
                QuestTemplate template = plugin.getQuestLoader().getAllTemplates().stream()
                    .filter(q -> q.toQuest().getId().equalsIgnoreCase(id))
                    .findFirst().orElse(null);
                if (template == null) {
                    player.sendMessage(ChatColor.RED + "Quest not found.");
                    return true;
                }
                Quest quest = template.toQuest();
                plugin.getQuestManager().getPlayerQuests(player.getUniqueId()).add(quest);
                player.sendMessage(ChatColor.GREEN + "Given quest: " + quest.getDescription());
            }
        }

        return true;
    }
}
