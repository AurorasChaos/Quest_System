package com.example.questplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@SuppressWarnings("unused")
public class QuestGUI {

    private final QuestPlugin plugin;
    private final RewardHandler rewardHandler;
    private final QuestNotifier notifier;

    private final Map<UUID, Integer> pageMap = new HashMap<>();
    private final Map<UUID, QuestFilter> filterMap = new HashMap<>();
    private final Map<UUID, QuestTier> tierMap = new HashMap<>();
    private static final int QUESTS_PER_PAGE = 21;

    public QuestGUI(QuestPlugin plugin) {
        this.plugin = plugin;
        this.rewardHandler = new RewardHandler(plugin);
        this.notifier = new QuestNotifier(plugin);
    }

    public void open(Player player, int page, QuestTier tier, QuestFilter filter) {
        UUID id = player.getUniqueId();
        List<Quest> allQuests = plugin.getQuestManager().getQuestsForTier(id, tier);
        List<Quest> filtered = filter.apply(allQuests);

        int totalPages = (int) Math.ceil((double) filtered.size() / QUESTS_PER_PAGE);
        page = Math.max(0, Math.min(page, totalPages - 1));

        pageMap.put(id, page);
        filterMap.put(id, filter);
        tierMap.put(id, tier);

        Inventory gui = Bukkit.createInventory(null, 36, getGuiTitle(tier, page));
        int start = page * QUESTS_PER_PAGE;
        int end = Math.min(start + QUESTS_PER_PAGE, filtered.size());

        for (int i = start; i < end; i++) {
            gui.setItem(i - start, createQuestItem(filtered.get(i)));
        }

        gui.setItem(27, navItem(Material.ARROW, "Previous", ChatColor.YELLOW));
        gui.setItem(31, navItem(Material.HOPPER, "Filter: " + filter.label(), ChatColor.AQUA));
        gui.setItem(35, navItem(Material.ARROW, "Next", ChatColor.YELLOW));
        gui.setItem(29, navItem(Material.PAPER, "§a📅 Daily", ChatColor.GREEN));
        gui.setItem(30, navItem(Material.BOOK, "§9📆 Weekly", ChatColor.BLUE));
        gui.setItem(32, navItem(Material.ENDER_EYE, "§d🌍 Global", ChatColor.DARK_PURPLE));

        player.openInventory(gui);
    }

    private String getGuiTitle(QuestTier tier, int page) {
        return switch (tier) {
            case DAILY -> "§a§lDaily Quests §7(Page " + (page + 1) + ")";
            case WEEKLY -> "§9§lWeekly Quests §7(Page " + (page + 1) + ")";
            case GLOBAL -> "§d§lGlobal Quests §7(Page " + (page + 1) + ")";
        };
    }

    private ItemStack createQuestItem(Quest quest) {
        Material mat = switch (quest.getRarity()) {
            case COMMON -> Material.PAPER;
            case RARE -> Material.MAP;
            case EPIC -> Material.ENCHANTED_BOOK;
            case LEGENDARY -> Material.NETHER_STAR;
        };

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(quest.getRarity().getColor() + quest.getDescription());

        List<String> lore = new ArrayList<>();
        lore.add("§8Rarity: " + quest.getRarity().getDisplayName());
        lore.add("§7Progress: §f" + quest.getCurrentProgress() + " / " + quest.getTargetAmount());
        if (quest.isRewardClaimed()) {
            lore.add("§a✔ Reward claimed!");
        } else if (quest.isCompleted()) {
            lore.add("§eClick to claim reward!");
            meta.addEnchant(org.bukkit.enchantments.Enchantment.FORTUNE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            lore.add("§cNot completed yet.");
        }
        lore.add("§b+ " + quest.getCurrencyReward() + " coins");
        lore.add("§d+ " + quest.getSkillPointReward() + " skill points");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack navItem(Material mat, String name, ChatColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color + name);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID id = player.getUniqueId();
        event.setCancelled(true);

        if (!(event.getView().getTitle().contains("Quests"))) return;

        int slot = event.getRawSlot();
        int page = pageMap.getOrDefault(id, 0);
        QuestFilter filter = filterMap.getOrDefault(id, QuestFilter.ALL);
        QuestTier tier = tierMap.getOrDefault(id, QuestTier.DAILY);

        List<Quest> quests = plugin.getQuestManager().getQuestsForTier(id, tier);
        List<Quest> filtered = filter.apply(quests);
        int start = page * QUESTS_PER_PAGE;
        int index = slot + start;

        if (slot >= 0 && slot < QUESTS_PER_PAGE && index < filtered.size()) {
            Quest quest = filtered.get(index);
            if (quest.canClaim()) {
                rewardHandler.giveReward(player, quest);
                notifier.notifyCompletion(player, quest);
                open(player, page, tier, filter);
            } else {
                player.sendMessage(ChatColor.RED + "❌ You can't claim this yet.");
            }
            return;
        }

        switch (slot) {
            case 27 -> open(player, page - 1, tier, filter);
            case 35 -> open(player, page + 1, tier, filter);
            case 31 -> open(player, 0, tier, filter.next());
            case 29 -> open(player, 0, QuestTier.DAILY, QuestFilter.ALL);
            case 30 -> open(player, 0, QuestTier.WEEKLY, QuestFilter.ALL);
            case 32 -> open(player, 0, QuestTier.GLOBAL, QuestFilter.ALL);
        }
    }
}