package fi.matiaspaavilainen.masuitewarps.bukkit.commands;

import fi.matiaspaavilainen.masuitecore.bukkit.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.configuration.BukkitConfiguration;
import fi.matiaspaavilainen.masuitewarps.bukkit.MaSuiteWarps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
        Formator formator = new Formator();
        BukkitConfiguration config = new BukkitConfiguration();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            if (plugin.checkCooldown(cs, plugin)) return;

            Player p = (Player) cs;
            try (ByteArrayOutputStream b = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream(b)) {
                if (args.length == 1) {
                    out.writeUTF("SetWarp");
                    out.writeInt(2);
                    setWarp(args, p, out);
                    p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                } else if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("hidden") || args[1].equalsIgnoreCase("global")) {
                        out.writeUTF("SetWarp");
                        out.writeInt(3);
                        setWarp(args, p, out);
                        out.writeUTF(args[1]);
                        p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                    }
                } else {
                    formator.sendMessage((Player) cs, config.load("warps", "syntax.yml").getString("warp.set"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            plugin.in_command.remove(cs);

        });

        return true;
    }

    private void setWarp(String[] args, Player p, DataOutputStream out) throws IOException {
        out.writeUTF(p.getName());
        out.writeUTF(args[0]);
        Location loc = p.getLocation();
        out.writeUTF(loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch());
    }
}
