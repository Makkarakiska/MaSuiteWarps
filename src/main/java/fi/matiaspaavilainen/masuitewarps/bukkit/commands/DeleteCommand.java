package fi.matiaspaavilainen.masuitewarps.bukkit.commands;

import fi.matiaspaavilainen.masuitecore.core.channels.BukkitPluginChannel;
import fi.matiaspaavilainen.masuitewarps.bukkit.MaSuiteWarps;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteCommand implements CommandExecutor {

    private MaSuiteWarps plugin;

    public DeleteCommand(MaSuiteWarps p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if (!(cs instanceof Player)) {
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            if (plugin.in_command.contains(cs)) {
                plugin.formator.sendMessage(cs, plugin.onActiveCommand);
                return;
            }

            plugin.in_command.add(cs);

            Player p = (Player) cs;
            if (args.length == 1) {
                new BukkitPluginChannel(plugin, p, new Object[]{"DelWarp", p.getName(), args[0]}).send();
            } else {
                plugin.formator.sendMessage(cs, plugin.deleteSyntax);
            }
            plugin.in_command.remove(cs);

        });

        return true;
    }
}
