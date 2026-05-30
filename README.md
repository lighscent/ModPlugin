# ModPlugin

**A lightweight staff plugin with H2 persistence — freeze, mute, vanish, inspect, and more.**

---

## Features

- **Staff Mode** — toggle with `/staff`. Saves & restores inventory, armor, effects, XP, and location.
- **Vanish** — Eye of Ender toggle. Uses Paper's `setInvisible` (no particles, no tab name).
- **Freeze** — Blaze Rod or `/freeze`. Sets walk speed to 0, teleports back on move.
- **Mute** — `/mute <player>`. Cancels chat messages.
- **Inventory Inspect** — right-click a player with a Chest, or use `/inventory <player>`. See inventory, armor, off-hand, and XP in a 54-slot GUI.
- **Ender Chest Inspect** — right-click with an Ender Chest, or use `/enderchest <player>`.
- **Offline Data** — player inventories are saved on quit. Modify offline inventories with `modplugin.inventory.modify` — changes apply when they log back in.
- **Gamemode commands** — `/gmc`, `/gms`, `/gmsp`, `/gma` with `.self` / `.other` permissions.
- **Double-click quit** — Barrier item requires a second click to leave staff mode.

## How It Works

The plugin uses an embedded **H2 database** via **HikariCP** to store staff mode state and player inventory snapshots. Data persists across restarts — no MySQL setup needed.

Staff mode serialises the player's full state using Bukkit's serialization API and stores it as Base64 CLOBs. On disable, everything is restored exactly as it was.

## Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/staff` | `modplugin.staff.use` | Toggle staff mode |
| `/vanish` | `modplugin.vanish.use` | Toggle vanish |
| `/freeze <player>` | `modplugin.freeze.use` | Freeze or unfreeze a player |
| `/mute <player>` | `modplugin.mute.use` | Mute or unmute a player |
| `/inventory <player>` | `modplugin.inventory.see` | View a player's inventory |
| `/enderchest <player>` | `modplugin.enderchest.see` | View a player's ender chest |
| `/gmc` `/gms` `/gmsp` `/gma` | `modplugin.{cmd}.self` / `.other` | Switch gamemode |
| `/modplugin reload` | `modplugin.reload` | Reload config.yml |

## Staff Items

Right-click on a player to use each item:

| Slot | Item | Action |
|------|------|--------|
| 0 | Ender Pearl | Teleport to a random player |
| 3 | Chest | Open the player's inventory |
| 4 | Blaze Rod | Freeze / unfreeze |
| 5 | Ender Chest | Open the player's ender chest |
| 7 | Eye of Ender | Toggle vanish |
| 8 | Barrier | Quit staff mode (click twice) |

All item slots are configurable in `config.yml`.

## Permissions

| Permission | Default | Description |
|-----------|---------|-------------|
| `modplugin.inventory.modify` | op | Move items in the inspect GUI |
| `modplugin.reload` | op | Reload the plugin config |

All other permissions default to `op`.

## Configuration

`plugins/ModPlugin/config.yml`:

```yaml
slots:
  freeze: 4
  inventory: 3
  enderchest: 5
  teleport: 0
  vanish: 7
  quit: 8

silent-join:
  enabled: true
  permission: modplugin.silentjoin

auto-vanish:
  enabled: true
  permission: modplugin.autovanish
```

## Installation

1. Download the jar from the releases page.
2. Place it in your `plugins/` folder.
3. Restart your server (or `/reload`).
4. Optionally configure `plugins/ModPlugin/config.yml`.

### Requirements

- **Paper** 1.21 (or a fork)
- Java 8+

The plugin is compiled against the Bukkit 1.8 API for maximum compatibility but uses Paper-specific features (vanish, spectator mode) via reflection.

## Data

| File | Purpose |
|------|---------|
| `plugins/ModPlugin/db/database` | H2 database — stores staff mode state & player snapshots |
| `plugins/ModPlugin/config.yml` | Item slot configuration |

## Building from source

```bash
mvn clean package -U
```

The shaded jar will be at `target/ModPlugin-1.0.0.jar` with H2, HikariCP, and SLF4J relocated to `com.modplugin.libs.*`.

---

*Questions or issues? Open a ticket on the [GitHub repository](https://github.com/anomalyco/opencode/issues).*
