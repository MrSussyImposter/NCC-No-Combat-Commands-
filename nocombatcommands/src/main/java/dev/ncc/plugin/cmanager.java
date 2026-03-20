package dev.ncc.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class CombatManager {

    private final NoCombatCommands plugin;
    private final Map<UUID, Integer> combatSeconds = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, BukkitTask> countdownTasks = new HashMap<>();

    public CombatManager(NoCombatCommands plugin) {
        this.plugin = plugin;
    }

    private String buildTitle(int seconds) {
        String timeColor;
        if (seconds >= 21) {
            timeColor = "§c";
        } else if (seconds >= 11) {
            timeColor = "§e";
        } else {
            timeColor = "§a";
        }
        return "§c§lCOMBAT TIMER  §0§l[ §r§l" + timeColor + seconds + "S §0§l]";
    }

    public void tagPlayer(Player player) {
        int duration = plugin.getConfig().getInt("combat-tag-duration", 30);
        UUID uuid = player.getUniqueId();

        BukkitTask existing = countdownTasks.remove(uuid);
        if (existing != null) {
            existing.cancel();
        }

        combatSeconds.put(uuid, duration);

        player.sendMessage("§cYou've been combat tagged for §e" + duration + "s§c.");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);

        BossBar bar = bossBars.computeIfAbsent(uuid, id -> {
            BossBar b = Bukkit.createBossBar(buildTitle(duration), BarColor.RED, BarStyle.SOLID);
            b.addPlayer(player);
            return b;
        });

        bar.setTitle(buildTitle(duration));
        bar.setProgress(1.0);
        bar.setVisible(true);

        final int[] secondsLeft = {duration};
        final double totalDuration = duration;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cleanupPlayer(uuid);
                return;
            }

            secondsLeft[0]--;
            combatSeconds.put(uuid, secondsLeft[0]);

            if (secondsLeft[0] <= 0) {
                cleanupPlayer(uuid);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
                player.sendMessage("§aYou are no longer in combat.");
                return;
            }

            double progress = secondsLeft[0] / totalDuration;
            bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
            bar.setTitle(buildTitle(secondsLeft[0]));
        }, 20L, 20L);

        countdownTasks.put(uuid, task);
    }

    public boolean isInCombat(Player player) {
        return combatSeconds.containsKey(player.getUniqueId());
    }

    public int getRemainingSeconds(Player player) {
        return combatSeconds.getOrDefault(player.getUniqueId(), 0);
    }

    public void cleanupPlayer(UUID uuid) {
        combatSeconds.remove(uuid);

        BukkitTask task = countdownTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }

        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.setVisible(false);
            bar.removeAll();
        }
    }

    public void cleanupAll() {
        for (UUID uuid : new HashMap<>(combatSeconds).keySet()) {
            cleanupPlayer(uuid);
        }
    }
}