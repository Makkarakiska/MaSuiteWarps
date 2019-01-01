package fi.matiaspaavilainen.masuitewarps.bukkit.commands;

import fi.matiaspaavilainen.masuitecore.bukkit.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.configuration.BukkitConfiguration;
import fi.matiaspaavilainen.masuitewarps.bukkit.MaSuiteWarps;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

        BukkitConfiguration config = new BukkitConfiguration();
        Formator formator = new Formator();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            if (plugin.in_command.contains(cs)) {
                formator.sendMessage((Player) cs, config.load(null, "messages.yml").getString("on-active-command"));
                return;
            }

            plugin.in_command.add(cs);

            Player p = (Player) cs;
            if (args.length == 1) {
                try (ByteArrayOutputStream b = new ByteArrayOutputStream();
                     DataOutputStream out = new DataOutputStream(b)) {
                    out.writeUTF("DelWarp");
                    out.writeUTF(p.getName());
                    out.writeUTF(args[0]);
                    p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                formator.sendMessage((Player) cs, config.load("warps", "messages.yml").getString("warp.delete"));
            }
            plugin.in_command.remove(cs);

        });

        return true;
    }
}
