package dev.ncc.plugin;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class CombatLogManager {

    private final NoCombatCommands plugin;
    private File logFile;
    private FileConfiguration logConfig;

    public CombatLogManager(NoCombatCommands plugin) {
        this.plugin = plugin;
        setupLogFile();
    }

    private void setupLogFile() {
        logFile = new File(plugin.getDataFolder(), "combat-log-logs.yml");
        if (!logFile.exists()) {
            plugin.saveResource("combat-log-logs.yml", false);
        }
        logConfig = YamlConfiguration.loadConfiguration(logFile);
    }

    public void logCombatLog(Player player, String method) {
        Location loc = player.getLocation();
        String coordinates = (int) loc.getX() + " " + (int) loc.getY() + " " + (int) loc.getZ();
        String date = new SimpleDateFormat("dd/MM/yy").format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String world = loc.getWorld() != null ? loc.getWorld().getName() : "unknown";

        String entry = player.getName() + " : logged at " + coordinates + " in world " + world
                + " on " + date + " at " + time + " method : " + method;

        List<String> logs = logConfig.getStringList("logs");
        logs.add(entry);
        logConfig.set("logs", logs);

        try {
            logConfig.save(logFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save combat-log-logs.yml: " + e.getMessage());
        }
    }
}