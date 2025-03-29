package com.example.questplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("unused")
public class QuestGUI implements Listener{
    private static final int QUESTS_PER_PAGE = 21;
    private final QuestPlugin plugin;
    private final RewardHandler rewardHandler;
    private final QuestNotifier notifier;
    private final Map<UUID, Integer> pageMap = new HashMap<>();
    private final Map<UUID, QuestFilter> filterMap = new HashMap<>();
    private final Map<UUID, QuestTier> tierMap = new HashMap<>();
    private final Map<UUID, Set<String>> claimedGlobalQuestMap = new HashMap<>();
    private final File claimFile;
    private final FileConfiguration claimConfig;

    public QuestGUI(QuestPlugin plugin) {
        this.plugin = plugin;
        this.rewardHandler = new RewardHandler(plugin);
        this.notifier = new QuestNotifier(plugin);

        this.claimFile = new File(plugin.getDataFolder(), "global_claims.yml");
        if (!claimFile.exists()) {
            try {
                claimFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.claimConfig = YamlConfiguration.loadConfiguration(claimFile);
        loadClaims();
    }

    public void open(Player player, int page, QuestTier tier, QuestFilter filter) {
        UUID uuid = player.getUniqueId();
        List<Quest> quests = (tier == QuestTier.ALL)
            ? plugin.getQuestManager().getAllPlayerQuests(uuid)
            : plugin.getQuestManager().getQuestsForTier(uuid, tier);
        List<Quest> filtered = filter.apply(quests);

        pageMap.put(uuid, page);
        filterMap.put(uuid, filter);
        tierMap.put(uuid, tier);

        int maxPages = (int) Math.ceil(filtered.size() / 21.0);
        if (page >= maxPages && maxPages > 0) page = maxPages - 1;

        Inventory gui = Bukkit.createInventory(null, 36, getTierDisplayName(tier) + " Quests (Page " + (page + 1) + "/" + (maxPages == 0 ? 1 : maxPages) + ")");

        int start = page * 21;
        int end = Math.min(start + 21, filtered.size());
        List<Quest> pageQuests = filtered.subList(start, end);

        if (pageQuests.isEmpty()) {
            ItemStack noQuests = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = noQuests.getItemMeta();
            meta.setDisplayName("§7No quests to display.");
            noQuests.setItemMeta(meta);
            gui.setItem(10, noQuests);
        } else {
            for (int i = 0; i < pageQuests.size(); i++) {
                Quest quest = pageQuests.get(i);
                gui.setItem(i, createQuestItem(quest));
            }
        }

        gui.setItem(27, page > 0 ? createNavItem(Material.ARROW, "Previous Page") : createNavItem(Material.BARRIER, "No Previous Page"));
        gui.setItem(35, page < maxPages - 1 ? createNavItem(Material.ARROW, "Next Page") : createNavItem(Material.BARRIER, "No Next Page"));
        gui.setItem(31, createNavItem(Material.HOPPER, "Filter: " + filter.name()));

        gui.setItem(29, tier == QuestTier.DAILY ? glowing(createNavItem(Material.EMERALD, "Daily Quests")) : createNavItem(Material.EMERALD, "Daily Quests"));
        gui.setItem(30, tier == QuestTier.WEEKLY ? glowing(createNavItem(Material.DIAMOND, "Weekly Quests")) : createNavItem(Material.DIAMOND, "Weekly Quests"));
        gui.setItem(32, tier == QuestTier.GLOBAL ? glowing(createNavItem(Material.NETHER_STAR, "Global Quests")) : createNavItem(Material.NETHER_STAR, "Global Quests"));
        gui.setItem(33, tier == QuestTier.ALL ? glowing(createNavItem(Material.BOOK, "All Quests")) : createNavItem(Material.BOOK, "All Quests"));

        player.openInventory(gui);
    }

    private ItemStack glowing(ItemStack item) {
        item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.LURE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createNavItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + name);
        item.setItemMeta(meta);
        return item;
    }

    private String getTierDisplayName(QuestTier tier) {
        return switch (tier) {
            case DAILY -> "Daily";
            case WEEKLY -> "Weekly";
            case GLOBAL -> "Global";
            case ALL -> "ALL";
        };
    }

    private String getGuiTitle(QuestTier tier, int page) {
        return switch (tier) {
            case DAILY -> "§a§lDaily Quests §7(Page " + (page + 1) + ")";
            case WEEKLY -> "§9§lWeekly Quests §7(Page " + (page + 1) + ")";
            case GLOBAL -> "§d§lGlobal Quests §7(Page " + (page + 1) + ")";
            case ALL -> "§d§lAll Quests §7(Page " + (page + 1) + ")";
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

    private void loadClaims() {
        for (String key : claimConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            List<String> ids = claimConfig.getStringList(key);
            claimedGlobalQuestMap.put(uuid, new HashSet<>(ids));
        }
    }

    private void saveClaims() {
        for (Map.Entry<UUID, Set<String>> entry : claimedGlobalQuestMap.entrySet()) {
            claimConfig.set(entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }
        try {
            claimConfig.save(claimFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().contains("Quests")) return;

        event.setCancelled(true);
        handleClick(event);
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
            case 33 -> open(player, 0, QuestTier.ALL, QuestFilter.ALL);
        }
    }
}