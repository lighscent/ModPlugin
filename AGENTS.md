# ModPlugin — Spigot 1.8 Staff Plugin

## Build & Run

```bash
mvn clean package -U   # builds shaded fat-jar at target/ModPlugin-1.0.0.jar
```

- Java 8 target, Java 25 JDK accepted (warnings about obsolete source/target are harmless)
- No tests exist (`src/test/` absent, no surefire)
- `plugin.yml` has resource filtering — `${project.version}` resolves at build

## Architecture

```
com.modplugin
├── ModPlugin.java             ← entry point (extends JavaPlugin)
├── commands/
│   ├── GamemodeCommand.java   ← /gmc /gms /gmsp /gma (shared executor)
│   └── StaffCommand.java      ← /staff toggles staff mode
├── database/
│   └── DatabaseManager.java   ← H2 + HikariCP pool (file at plugins/ModPlugin/database)
├── listeners/
│   └── StaffModeListener.java ← cancels EntityDamageEvent for staff-mode players
└── managers/
    └── StaffModeManager.java  ← staff mode state + save/restore to H2
```

## Key Facts

- **Fat-jar shading**: H2, HikariCP, SLF4J are relocated to `com.modplugin.libs.*` to avoid classloader conflicts. Do not add them as provided dependencies.
- **Database**: Embedded H2 file via HikariCP pool (max 10, min idle 2). DB file is `<plugin-data-folder>/database`. Table `staff_data` stores inventory/armor/effects/XP/location as Base64 CLOBs.
- **Staff mode**: Saves player state on enable, restores + deletes row on disable. In-memory `Set<UUID>` tracks active staff. Players in staff mode are invulnerable (all damage cancelled).
- **Permission pattern**: `modplugin.{cmd}.self` and `modplugin.{cmd}.other` per gamemode command. `modplugin.all.self` / `modplugin.all.other` are parent nodes. `modplugin.staff.use` is checked by `/staff`; `modplugin.staff` is declared but never read in code.

## Gotchas

- **ResultSet leak**: `DatabaseManager.executeQuery()` returns a raw `ResultSet` without closing the connection. Callers **must** close the ResultSet manually. Only caller is `StaffModeManager.restorePlayerData()` which does `rs.close()`.
- **plugin.yml stale description**: `/staff` command says "List online staff members" but actually toggles staff mode. Update if needed.
- **Staff tool items are hardcoded**: slot 0 = COMPASS, slot 1 = BOOK, slot 2 = STICK. Not configurable.
- **No cooldown on `/staff`**: Toggle is unlimited.
- **Gamemode commands use `getPlayerExact`**: Suppressed via `@SuppressWarnings("deprecation")` — both `getPlayer` and `getPlayerExact` are deprecated in Bukkit 1.8 API.
