# 🎯 QuestPlugin

A feature-rich and extensible quest system for Minecraft servers running **Spigot 1.21.4**, with full support for:

- 🧭 Daily, weekly & global quests
- 💰 Vault currency & 🧠 AuraSkills integration
- 🎨 Beautiful GUI with pagination & filters
- 📅 Streak bonuses & monthly leaderboards
- 🛠️ Bedrock Forms UI for Geyser/Floodgate players
- 💾 Persistent YAML & SQLite-based storage
- 🔧 Fully configurable via `quests.yml` and `config.yml`

---

## 📦 Features

- 🔁 **Repeatable & unique quests**
- 🎲 **Randomized quests daily** for each player
- 🧑‍🤝‍🧑 **Global server objectives**
- 🏆 **Rarity tiers** with bonus rewards and announcements
- 📈 **Tracking for block break/place, mobs, fishing, skills, biomes & more**
- 🪧 **Leaderboard holograms** with DecentHolograms
- 🌐 **Bedrock UI support** using Floodgate Forms
- 🧩 PlaceholderAPI support for scoreboard, NPCs, etc.

---

## 🧰 Plugin Dependencies

| Plugin          | Purpose                           |
|------------------|-----------------------------------|
| [Vault](https://www.spigotmc.org/resources/vault.34315/)              | Economy support |
| [AuraSkills](https://wiki.aurelium.dev/)            | Skill points, XP, and skill tracking |
| [Citizens](https://www.spigotmc.org/resources/citizens.13811/)        | NPC quest claiming |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Placeholder expansion |
| [Floodgate](https://github.com/GeyserMC/Floodgate)  | Bedrock Forms & player detection |
| [DecentHolograms](https://www.spigotmc.org/resources/decent-holograms.96927/) | Holograms for top players |
| [ItemNBTAPI](https://github.com/tr7zw/Item-NBT-API) | Custom seasonal items |

---

## 🚀 Getting Started

1. 📥 Drop `QuestPlugin.jar` into your `plugins/` folder.
2. 🛠️ Configure quests in `quests.yml`.
3. ⚙️ Adjust global values in `config.yml`.
4. 🔄 Restart or use `/quest reload` to apply changes.
5. ✅ Players can use `/quest` to open their quest panel.

---

## 💻 Commands

| Command        | Description                      | Permission            |
|----------------|----------------------------------|------------------------|
| `/quest`       | Opens the quest GUI              | `questplugin.use`     |
| `/quest reload`| Reloads all configs              | `questplugin.admin`   |
| `/questdebug`  | View debug info (optional)       | `questplugin.debug`   |

---

## 📁 File Structure

```plaintext
plugins/
└── QuestPlugin/
    ├── quests.yml           # List of quest templates
    ├── config.yml           # Settings, rewards, filters, GUI
    └── player_quests.yml    # Persistent player progress
```

---

## 🌟 Quest Types Supported

- `BREAK_BLOCK`, `PLACE_BLOCK`
- `KILL_MOB`, `DAMAGE_BOSS`
- `VISIT_BIOME`, `ENTER_DIMENSION`
- `FISH`, `TRADE`, `BREED`
- `GAIN_SKILL_EXP`, `REACH_SKILL_LEVEL`
- `USE_SKILL` (AuraSkills abilities)

---

## 🧠 Advanced Features

- 🔥 **Legendary quest drops** broadcast server-wide
- 🧵 **Animated GUI reveal** on daily login
- 💫 **Seasonal items** for top players
- 🧩 **Hooks for monthly rewards and expansions**

---

## 🛠️ Developer Tools

- ✅ Maven support (with example `pom.xml`)
- 🧪 `QuestManager`, `QuestLoader`, `QuestGUI`, `QuestStorageManager` classes
- ♻️ Auto-reset on server startup & every midnight
- 🔐 Bedrock-aware UI via Floodgate Forms
- ☑️ Configurable quest filters, tiers, rarity weights

---

## 🧩 Plugin API (Coming Soon)

- `QuestPluginAPI#getPlayerQuests(UUID)`
- `QuestPluginAPI#assignQuest(UUID, Quest)`
- `QuestPluginAPI#getLeaderboard(type)`

---

## ❤️ Contributing

Pull requests, ideas, and testing feedback welcome!  
Want to add your own quest types or hook into the system? Open an issue or PR!

---

## 📄 License

MIT – free to use, fork, and customize for your server.

---
