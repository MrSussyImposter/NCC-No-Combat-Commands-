package dev.ncc.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NCCCommand implements CommandExecutor, TabCompleter {

    private final NoCombatCommands plugin;
    private final CombatManager combatManager;

    private static final List<String> SUBCOMMANDS = Arrays.asList("addcommand", "removecommand");

    public NCCCommand(NoCombatCommands plugin, CombatManager combatManager) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ncc.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "addcommand" -> {
                if (args.length < 2) { sendUsage(sender); return true; }
                String target = normalize(args[1]);
                List<String> blocked = plugin.getConfig().getStringList("blocked-commands");
                if (blocked.stream().anyMatch(c -> c.equalsIgnoreCase(target))) {
                    sender.sendMessage("§e" + target + " §cis already blocked.");
                    return true;
                }
                blocked.add(target);
                plugin.getConfig().set("blocked-commands", blocked);
                plugin.saveConfig();
                sender.sendMessage("§aAdded §e" + target + "§a.");
            }
            case "removecommand" -> {
                if (args.length < 2) { sendUsage(sender); return true; }
                String target = normalize(args[1]);
                List<String> blocked = plugin.getConfig().getStringList("blocked-commands");
                if (!blocked.removeIf(c -> c.equalsIgnoreCase(target))) {
                    sender.sendMessage("§e" + target + " §cwas not found.");
                    return true;
                }
                plugin.getConfig().set("blocked-commands", blocked);
                plugin.saveConfig();
                sender.sendMessage("§aRemoved §e" + target + "§a.");
            }
            default -> sendUsage(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ncc.admin")) return new ArrayList<>();
        if (args.length == 1) return filterStarting(SUBCOMMANDS, args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("removecommand")) {
            return filterStarting(plugin.getConfig().getStringList("blocked-commands"), args[1]);
        }
        return new ArrayList<>();
    }

    private List<String> filterStarting(List<String> options, String partial) {
        List<String> result = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase().startsWith(partial.toLowerCase())) result.add(o);
        }
        return result;
    }

    private String normalize(String input) {
        String t = input.toLowerCase().trim();
        return t.startsWith("/") ? t : "/" + t;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§eUsage: §f/NCC addcommand </cmd> §7| §f/NCC removecommand </cmd>");
    }
}