# Blaze's WildKits

Premium KitPvP plugin for **Paper 1.21+** (Java 21).

Players do **not** type `/kit`. They automatically receive a smart-generated random kit on join and every respawn.

## Features

- Auto random kits every life (`auto-random-kits`)
- **160** premade professionally themed kits
- Smart balanced equipment generation (armor/weapon tiers stay coherent)
- Rarities: Common → Ultimate with configurable weights
- Cosmetics shop (trails, death/victory effects, tags, titles, kits)
- 15 particle trails with amount/spacing/speed/toggle/preview
- Modern animated GUI (categories, search, favorites, recent, preview, shop)
- Configurable respawn + spawn system (`/wk setspawn`, `/wk spawn`)
- Citizens NPC support (right-click opens GUI)
- Animated scoreboard (no content dependency on vanilla score numbers)
- PlaceholderAPI, Vault, LuckPerms, MiniMessage
- SQLite (default) + optional MySQL (HikariCP)
- Fully reloadable configs

## Commands

| Command | Description |
|---|---|
| `/wk` `/wildkits` `/blazewildkits` | Open main GUI |
| `/wk kits` | Kits browser |
| `/wk shop` | Shop |
| `/wk particles` | Trails |
| `/wk random` | Force random kit |
| `/wk preview <kit>` | Preview kit |
| `/wk search <query>` | Search kits |
| `/wk spawn` | Teleport to spawn |
| `/wk setspawn` | Set spawn (admin) |
| `/wk daily` | Daily reward |
| `/wk stats` | Player stats |
| `/wk reload` | Reload configs |
| `/wk npc [name]` | Create Citizens NPC |

## Placeholders

- `%wildkits_kills%`
- `%wildkits_deaths%`
- `%wildkits_kd%`
- `%wildkits_coins%`
- `%wildkits_level%`
- `%wildkits_currentkit%`
- `%wildkits_killstreak%`

## Build

```bash
mvn -B clean package
```

JAR output: `target/BlazesWildKits-1.0.0.jar`

## Config files

- `config.yml`
- `kits.yml`
- `shop.yml`
- `scoreboard.yml`
- `animations.yml`
- `messages_en.yml`
- `messages_ro.yml`
- `database.yml`
- `particles.yml`
