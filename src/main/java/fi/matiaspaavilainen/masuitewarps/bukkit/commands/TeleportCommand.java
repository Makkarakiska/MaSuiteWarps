package fi.matiaspaavilainen.masuitewarps.bukkit.commands;

import fi.matiaspaavilainen.masuitecore.core.channels.BukkitPluginChannel;
import fi.matiaspaavilainen.masuitecore.core.utils.BukkitWarmup;
import fi.matiaspaavilainen.masuitewarps.bukkit.MaSuiteWarps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand implements CommandExecutor {

    private MaSuiteWarps plugin;

    public TeleportCommand(MaSuiteWarps p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if (!(cs instanceof Player)) {
            if (checkWarp(cs, args[0])) {
                if (plugin.getServer().getOnlinePlayers().stream().findFirst().isPresent()) {
                    new BukkitPluginChannel(plugin, plugin.getServer().getOnlinePlayers().stream().findFirst().get(), new Object[]{"WarpPlayerCommand", args[1], "console", args[0]}).send();
                }
            }
            return true;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            if (plugin.checkCooldown(cs, plugin)) return;

            if (args.length == 1) {

                Player p = (Player) cs;

                if (checkWarp(cs, args[0])) {
                    if (checkCooldown(p)) {
                        if (plugin.warmupTime > 0) {
                            if (cs.hasPermission("masuitewarps.warmup.override")) {
                                if (checkWarp(cs, args[0])) {
                                    send(args, p);
                                    plugin.in_command.remove(cs);
                                    return;
                                }
                            }
                            MaSuiteWarps.warmups.add(p.getUniqueId());
                            plugin.formator.sendMessage(cs, plugin.teleportationStarted.replace("%time%", String.valueOf(plugin.warmupTime)));
                            new BukkitWarmup(plugin.warmupTime, plugin) {
                                @Override
                                public void count(int current) {
                                    if (current == 0) {
                                        if (MaSuiteWarps.warmups.contains(p.getUniqueId())) {
                                            send(args, p);
                                            MaSuiteWarps.warmups.remove(p.getUniqueId());
                                        }
                                    }
                                }
                            }.start();
                            plugin.in_command.remove(cs);
                        } else {
                            if (checkWarp(cs, args[0])) {
                                send(args, p);
                                plugin.in_command.remove(cs);
                            }
                        }
                    }
                }

            } else if (args.length == 2) {
                if (cs.hasPermission("masuitewarps.warp.others")) {
                    if (checkWarp(cs, args[0])) {
                        if (plugin.getServer().getOnlinePlayers().stream().findFirst().isPresent()) {
                            new BukkitPluginChannel(plugin, plugin.getServer().getOnlinePlayers().stream().findFirst().get(), new Object[]{"WarpPlayerCommand", args[1], "console", args[0]}).send();
                        }
                    }
                } else {
                    plugin.formator.sendMessage(cs, plugin.noPermission);
                    plugin.in_command.remove(cs);
                    return;
                }
            } else {
                plugin.formator.sendMessage(cs, plugin.teleportSyntax);
                plugin.in_command.remove(cs);
                return;
            }

            plugin.in_command.remove(cs);

        });
        return true;
    }

    private void send(String[] args, Player p) {
        sendLastLoc(p);
        String hidden;
        if (p.hasPermission("masuitewarps.warp.hidden")) {
            hidden = "HIDDEN";
        } else {
            hidden = "-------";
        }
        boolean hasPerm = false;
        if (p.hasPermission("masuitewarps.warp.to." + args[0]) || p.hasPermission("masuitewarps.warp.to.*")) {
            hasPerm = true;
        }
        new BukkitPluginChannel(plugin, p, new Object[]{"WarpCommand", hidden, p.getName(), args[0].toLowerCase(), hasPerm}).send();
    }

    private Boolean checkWarp(CommandSender cs, String name) {
        if (MaSuiteWarps.warps.containsKey(name.toLowerCase())) {
            return true;
        } else {
            plugin.formator.sendMessage(cs, plugin.warpNotFound);
            return false;
        }
    }

    private Boolean checkCooldown(Player p) {
        if (plugin.getConfig().getInt("cooldown") > 0) {
            if (p.hasPermission("masuitewarps.cooldown.override")) return true;
            if (MaSuiteWarps.cooldowns.containsKey(p.getUniqueId())) {
                if (System.currentTimeMillis() - MaSuiteWarps.cooldowns.get(p.getUniqueId()) > plugin.cooldownTime * 1000) {
                    MaSuiteWarps.cooldowns.remove(p.getUniqueId());
                    return true;
                } else {
                    plugin.formator.sendMessage(p, plugin.inCooldown.replace("%time%", String.valueOf(plugin.cooldownTime)));
                    return false;
                }
            } else {
                return true;
            }
        }
        return true;
    }

    private void sendLastLoc(Player p) {
        Location loc = p.getLocation();
        new BukkitPluginChannel(plugin, p, new Object[]{"MaSuiteTeleports", "GetLocation", p.getName(), loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch()}).send();
    }
}
