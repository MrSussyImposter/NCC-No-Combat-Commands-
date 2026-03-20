package dev.ncc.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class CombatListener implements Listener {

    private final NoCombatCommands plugin;
    private final CombatManager combatManager;
    private final CombatLogManager combatLogManager;
    private final Map<UUID, BukkitTask> warnTasks = new HashMap<>();

    public CombatListener(NoCombatCommands plugin, CombatManager combatManager, CombatLogManager combatLogManager) {
        this.plugin = plugin;
        this.combatManager = combatManager;
        this.combatLogManager = combatLogManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        if (event instanceof EntityDamageByEntityEvent pvp && pvp.getDamager() instanceof Player attacker) {
            combatManager.tagPlayer(attacker);
            combatManager.tagPlayer(victim);
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM) {
            combatManager.tagPlayer(victim);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!combatManager.isInCombat(player)) return;

        String rawMessage = event.getMessage().toLowerCase();
        String usedCommand = rawMessage.contains(" ")
                ? rawMessage.substring(0, rawMessage.indexOf(' '))
                : rawMessage;

        if (!usedCommand.startsWith("/")) return;

        List<String> blockedCommands = plugin.getConfig().getStringList("blocked-commands");

        for (String entry : blockedCommands) {
            String normalized = entry.trim().toLowerCase();
            if (!normalized.startsWith("/")) normalized = "/" + normalized;
            if (usedCommand.equals(normalized)) {
                event.setCancelled(true);
                int remaining = combatManager.getRemainingSeconds(player);
                player.sendMessage("§cYou are in combat, please wait for §e" + remaining + "s §cto use that command.");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!combatManager.isInCombat(player)) return;

        boolean aclEnabled = plugin.getConfig().getBoolean("acl", true);

        if (!aclEnabled) {
            combatManager.cleanupPlayer(uuid);
            return;
        }

        String method = plugin.getConfig().getString("acl-method", "KILL").toUpperCase();

        switch (method) {
            case "KILL" -> {
                combatLogManager.logCombatLog(player, "KILL");
                combatManager.cleanupPlayer(uuid);
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.playSound(online.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
                }
                player.setHealth(0);
            }
            case "WARN" -> {
                int warnTimer = plugin.getConfig().getInt("acl-warn-timer", 20);
                combatLogManager.logCombatLog(player, "WARN");
                combatManager.cleanupPlayer(uuid);

                Bukkit.broadcastMessage("§e PLAYER " + player.getName()
                        + " HAS COMBAT-LOGGED! THEY HAVE " + warnTimer
                        + "S TO RE-JOIN BEFORE THEIR DEATH FINALIZES. ");

                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    warnTasks.remove(uuid);
                    Player rejoined = Bukkit.getPlayer(uuid);
                    if (rejoined != null && rejoined.isOnline()) return;
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.playSound(online.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
                    }
                    Bukkit.broadcastMessage("§4 PLAYER " + player.getName() + " HAS DIED FROM NOT REJOINING. ");
                }, warnTimer * 20L);

                warnTasks.put(uuid, task);
            }
            default -> {
                combatLogManager.logCombatLog(player, "*");
                combatManager.cleanupPlayer(uuid);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        BukkitTask task = warnTasks.remove(uuid);
        if (task != null) task.cancel();
    }
}