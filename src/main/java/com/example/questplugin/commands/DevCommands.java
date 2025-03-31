package com.example.questplugin.commands.dev;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.example.questplugin.QuestPlugin;

public class DevCommands implements CommandExecutor {

    private final QuestPlugin plugin;

    public DevCommands(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPlayer-only command.");
            return true;
        }

        if (!player.hasPermission("questplugin.dev")) {
            player.sendMessage("§cYou need §6questplugin.dev §cpermission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reloadtemplates":
                plugin.getQuestLoader().loadTemplates();
                player.sendMessage("§aReloaded all quest templates.");
                break;

            case "forcecomplete":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /questdev forcecomplete <questId>");
                    return true;
                }
                plugin.getQuestHandler().forceComplete(player, args[1]);
                break;

            case "simreset":
                plugin.getQuestManager().simulateDailyReset();
                player.sendMessage("§aSimulated daily reset.");
                break;

            case "dumpcache":
                plugin.getQuestManager().dumpCacheToConsole();
                player.sendMessage("§aDumped cache to console.");
                break;

            default:
                sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§lQuestDev Commands:");
        player.sendMessage("§e/questdev reloadtemplates §7- Hot-reload quests.yml");
        player.sendMessage("§e/questdev forcecomplete <id> §7- Instantly complete a quest");
        player.sendMessage("§e/questdev simreset §7- Test daily reset logic");
        player.sendMessage("§e/questdev dumpcache §7- Debug quest cache");
    }
}