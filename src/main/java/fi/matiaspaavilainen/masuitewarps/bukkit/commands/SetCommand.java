package fi.matiaspaavilainen.masuitewarps.bukkit.commands;

import fi.matiaspaavilainen.masuitecore.core.channels.BukkitPluginChannel;
import fi.matiaspaavilainen.masuitewarps.bukkit.MaSuiteWarps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCommand implements CommandExecutor {

    private MaSuiteWarps plugin;

    public SetCommand(MaSuiteWarps p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if (!(cs instanceof Player)) {
            return false;
        }
        if (plugin.checkCooldown(cs, plugin)) return false;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            Player p = (Player) cs;
            Location loc = p.getLocation();
            String l = loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
            if (args.length == 1) {
                new BukkitPluginChannel(plugin, p, new Object[]{"SetWarp", 2, p.getName(), args[0], l}).send();
            } else if (args.length == 2) {
                if (args[1].equalsIgnoreCase("hidden") || args[1].equalsIgnoreCase("global")) {
                    if (args[1].equalsIgnoreCase("hidden") && !p.hasPermission("masuitewarp.setwarp.hidden")) {
                        plugin.formator.sendMessage(cs, plugin.noPermission);
                        return;
                    }
                    if (args[1].equalsIgnoreCase("global") && !p.hasPermission("masuitewarp.setwarp.global")) {
                        plugin.formator.sendMessage(cs, plugin.noPermission);
                        return;
                    }
                    if (!args[1].equalsIgnoreCase("global") && !args[1].equalsIgnoreCase("hidden") && !p.hasPermission("masuitewarp.setwarp.server")) {
                        plugin.formator.sendMessage(cs, plugin.noPermission);
                        return;
                    }
                    new BukkitPluginChannel(plugin, p, new Object[]{"SetWarp", 3, p.getName(), args[0], l, args[1]}).send();
                }
            } else {
                plugin.formator.sendMessage(cs, plugin.setSyntax);
            }
            plugin.in_command.remove(cs);
        });
        return true;
    }

}
