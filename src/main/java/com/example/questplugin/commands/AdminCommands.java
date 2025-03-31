package com.example.questplugin.commands.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.example.questplugin.QuestPlugin;

public class AdminCommands implements CommandExecutor {

    private final QuestPlugin plugin;

    public AdminCommands(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("questplugin.admin")) {
            sender.sendMessage("§cYou need §6questplugin.admin §cpermission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "resetglobal":
                plugin.getGlobalQuestService().refreshGlobalQuests();
                sender.sendMessage("§aReset all global quests.");
                break;

            case "playerstats":
                if (args.length < 2) return false;
                plugin.getLeaderboardManager().displayPlayerStats(sender, args[1]);
                break;

            case "reloadconfig":
                plugin.reloadConfig();
                plugin.getConfigManager().reload();
                sender.sendMessage("§aReloaded config files.");
                break;

            case "migrate":
                plugin.getQuestStorageService().migrateFromYamlAsync()
                    .thenRun(() -> sender.sendMessage("§aMigration completed."));
                break;

            default:
                sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lQuestAdmin Commands:");
        sender.sendMessage("§e/questadmin resetglobal §7- Refresh global quest pool");
        sender.sendMessage("§e/questadmin playerstats <player> §7- View quest progress");
        sender.sendMessage("§e/questadmin reloadconfig §7- Reload config.yml");
        sender.sendMessage("§e/questadmin migrate §7- Convert YAML to database");
    }
}