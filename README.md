# 🌟 QuestPlugin for Minecraft (1.21.4+)

A fully-featured, highly customizable Spigot plugin for managing **Daily**, **Weekly**, and **Global** quests with a beautiful animated GUI, dynamic rewards, leaderboard support, and cross-plugin integrations.

---

## 🖼️ Quest GUI Preview

![Quest GUI Preview](quest_gui_preview.gif)

---

## 🔧 Features

✅ Daily, Weekly, and Global quest tiers  
✅ Dynamic GUI with animated shimmer borders  
✅ Paginated quest interface with filters and quest rarity icons  
✅ Global quest progress shared server-wide  
✅ Leaderboard with DecentHolograms integration  
✅ Reward system supporting:  
• Vault (economy)  
• AuraSkills XP (per skill)  
• Skill point bonuses  
✅ Reset system with configurable intervals  
✅ YAML-based configuration for quests and rewards  
✅ Debug logging mode (`Debug: true` in config.yml)  
✅ Bedrock-compatible via Geyser (with optional Bedrock UI)

---

## 🧩 Dependencies

- [Vault](https://www.spigotmc.org/resources/vault.34315/) (for economy support)
- [AuraSkills](https://www.spigotmc.org/resources/auraskills.92541/) (for RPG skill rewards)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (optional placeholders)
- [DecentHolograms](https://www.spigotmc.org/resources/decentholograms.96927/) (for leaderboard holograms)
- [Citizens](https://www.spigotmc.org/resources/citizens.13811/) (optional NPC support)

---

## 🎯 Supported Quest Types

| Type            | Description                            |
|-----------------|----------------------------------------|
| `KILL_MOB`      | Kill specific mobs                     |
| `GATHER_ITEM`   | Break or gather specific blocks/items  |
| `CRAFT_ITEM`    | Craft specific items                   |
| `WALK_DISTANCE` | Walk a certain distance                |
| `EXPLORE_BIOME` | Visit specific biomes                  |
| `CONSUME_ITEM`  | Eat or drink specific items            |
| `GAIN_SKILL_EXP`| Gain AuraSkills XP                    |
| `GAIN_SKILL_LEVEL` | Level up a skill in AuraSkills     |
| `FISH`          | Catch fish                             |
| `PLACE_BLOCK`   | Place specific blocks                  |
| `MINE_BLOCK`    | Mine specific blocks                   |
| `LOOT`          | Pickup items                           |
| `TRADE`         | Trade with villagers                   |
| `BREED_ANIMAL`  | Breed animals                          |
| `SMELT_ITEM`    | Smelt items in furnaces                |
| `TAME_ENTITY`   | Tame pets (wolves, cats, etc.)         |
| `CUSTOM`        | Reserved for custom event hooks        |

---

## 📊 Leaderboards

Top 5 players by total claimed quest completions (with support for holograms):  
- Integrated with DecentHolograms  
- Supports refresh/update cycles  
- Future-ready for expansion (per quest, per type, seasonal, etc.)

---

## 📁 Example Configuration

```yaml
Debug: true

ResetTimes:
  Daily: "06:00"
  Weekly: "Monday 06:00"

Currency:
  RewardAmount: 100

Hologram:
  Enabled: true
  Location: world,100.5,65,200.5

Skills:
  Enabled: true
```

---

## 📜 License

MIT License — free to use, modify, and contribute.

---

## 💡 Contribution

Pull requests welcome. Test thoroughly. Suggestions? Create an issue.

---

Crafted with ❤️ for RPG lovers and survival servers.
