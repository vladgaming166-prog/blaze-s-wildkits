# Blaze's WildKits

Premium KitPvP plugin for **Paper 1.21+** (Java 21).

Players do **not** type `/kit`. They automatically receive a smart-generated random kit on join and every respawn.

## Features

- Instant auto random kits every life (`auto-random-kits`, delay `0`)
- **188** premium kit templates with unique themes
- Gear bands: Iron ~60% / Diamond ~35% / Netherite-partial ~5% (never full netherite)
- Per-life randomized extras (food, gaps, blocks, pearls, buckets, TNT, potions, etc.)
- Light random enchantments (Sharp/Prot/Power I–II)
- `/wk showkit true|false` — reveal or hide kit identity everywhere
- Chaos-style scoreboard: MiniMessage gradients, `shadow:black:1`, `%animation:%`, blank score numbers
- Cosmetics shop (trails, death/victory effects, tags, titles, kits)
- 15 particle trails with amount/spacing/speed/toggle/preview
- Modern animated GUI (categories, search, favorites, recent, preview, shop)
- Configurable respawn + spawn system (`/wk setspawn`, `/wk spawn`)
- Citizens NPC support (right-click opens GUI)
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
mvn -B clean verify
```

JAR output: `target/BlazesWildKits-1.1.0.jar`

`verify` runs a Linux SQLite native smoke test against the shaded JAR (`NativeDB` / `_open_utf8`).

### SQLite shading notes

`org.xerial:sqlite-jdbc` is bundled **without relocation**. Relocating `org.sqlite` breaks JNI native extraction on Linux (`UnsatisfiedLinkError: NativeDB._open_utf8`). Only HikariCP is relocated.

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
