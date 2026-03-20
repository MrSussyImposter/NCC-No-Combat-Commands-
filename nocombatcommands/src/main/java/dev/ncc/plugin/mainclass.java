package dev.ncc.plugin;

import org.bukkit.plugin.java.JavaPlugin;

final class NoCombatCommands extends JavaPlugin {

    private static NoCombatCommands instance;
    private CombatManager combatManager;
    private CombatLogManager combatLogManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        combatManager = new CombatManager(this);
        combatLogManager = new CombatLogManager(this);
        getServer().getPluginManager().registerEvents(new CombatListener(this, combatManager, combatLogManager), this);
        NCCCommand executor = new NCCCommand(this, combatManager);
        getCommand("NCC").setExecutor(executor);
        getCommand("NCC").setTabCompleter(executor);
        getLogger().info("No Combat Commands Loaded!");
    }

    @Override
    public void onDisable() {
        if (combatManager != null) {
            combatManager.cleanupAll();
        }
    }

    static NoCombatCommands getInstance() {
        return instance;
    }

    CombatManager getCombatManager() {
        return combatManager;
    }

    CombatLogManager getCombatLogManager() {
        return combatLogManager;
    }
}