# NoCombatCommands (NCC)

A lightweight Minecraft plugin for Paper, Spigot, Purpur, and Bukkit that prevents players from using configured commands while in combat, with a built-in anti combat-log system.

---

## Features

- **Combat Tagging** — players are tagged when they take damage from another player or the `/damage` command
- **Boss Bar** — a real-time countdown boss bar shows remaining combat time, changing color as it depletes
- **Command Blocking** — configured commands are blocked while a player is in combat
- **Anti Combat-Log (ACL)** — punishes players who disconnect during combat
- **In-Game Config** — add or remove blocked commands without restarting the server

---

## Compatibility

- Minecraft 1.21.11
- Paper, Spigot, Purpur, Bukkit

---

## Installation

1. Download `NCC-1.0.0_1.21.11.jar`
2. Place it in your server's `plugins/` folder
3. Restart the server
4. Configure `plugins/NoCombatCommands/config.yml`

---

## Commands

| Command | Description | Permission |
|---|---|---|
| `/NCC addcommand <command>` | Adds a command to the blocked list | `ncc.admin` |
| `/NCC removecommand <command>` | Removes a command from the blocked list | `ncc.admin` |

`ncc.admin` is granted to server operators by default.

---

## Permissions

| Permission | Description | Default |
|---|---|---|
| `ncc.admin` | Allows managing the blocked commands list | OP |

---

## Configuration

Located at `plugins/NoCombatCommands/config.yml`:

```yaml
# How long (in seconds) a player stays in combat after being hit
combat-tag-duration: 30

# Anti Combat-Log system
# Set to false to disable entirely
acl: true

# KILL  - kills the player instantly on logout, dropping their items
# WARN  - broadcasts a warning and gives them time to rejoin before dying
# *     - logs the logout silently with no punishment
acl-method: KILL

# Only used when acl-method is WARN
# How many seconds the player has to rejoin before dying
acl-warn-timer: 20

# Commands blocked while a player is in combat
# Add or remove entries using /NCC addcommand and /NCC removecommand
blocked-commands:
  - /tpa
  - /home
  - /spawn
  - /warp
  - /tp
  - /back
  - /rtp
```

---

## Boss Bar

The combat timer boss bar changes color based on remaining time:

- **Red** — 21s to 30s
- **Yellow** — 11s to 20s
- **Green** — 1s to 10s

---

## Anti Combat-Log Behavior

| Method | Behavior |
|---|---|
| `KILL` | Player is killed instantly on logout. Items drop normally. Wither spawn sound plays for all online players. |
| `WARN` | Server broadcasts a warning with a countdown. If the player does not rejoin in time, they die and the wither spawn sound plays. |
| `*` | No punishment. The logout is silently recorded in `combat-log-logs.yml`. |

All combat log events are recorded in `plugins/NoCombatCommands/combat-log-logs.yml` regardless of method, with the player name, coordinates, world, date, time, and method used.

---

## Building from Source

Requires Java 21 and Gradle 9.3.1.

```bash
cd NCC
gradlew jar
```

Output: `build/libs/NCC-1.0.0_1.21.11.jar`

---

## License

This project is open source. You are free to inspect, modify, and redistribute the source code.
