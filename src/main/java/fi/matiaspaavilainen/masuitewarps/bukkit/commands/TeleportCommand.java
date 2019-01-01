package fi.matiaspaavilainen.masuitewarps.bukkit.commands;

import fi.matiaspaavilainen.masuitecore.bukkit.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.configuration.BukkitConfiguration;
import fi.matiaspaavilainen.masuitewarps.bukkit.Countdown;
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

public class TeleportCommand implements CommandExecutor {

    private MaSuiteWarps plugin;

    private BukkitConfiguration config = new BukkitConfiguration();
    private Formator formator = new Formator();

    public TeleportCommand(MaSuiteWarps p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if (!(cs instanceof Player)) {
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            if (plugin.checkCooldown(cs, plugin)) return;

            if (args.length == 1) {

                Player p = (Player) cs;

                if (checkWarp(cs, args[0])) {
                    if (checkCooldown(p)) {
                        if (plugin.getConfig().getInt("warmup") > 0) {
                            MaSuiteWarps.warmups.add(p.getUniqueId());
                            formator.sendMessage((Player) cs, config.load("warps", "messages.yml").getString("teleportation-started").replace("%time%", String.valueOf(config.load("warps", "config.yml").getInt("warmup"))));
                            new Countdown(config.load("warps", "config.yml").getInt("warmup"), plugin) {
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
                        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
                             DataOutputStream out = new DataOutputStream(b)) {
                            out.writeUTF("WarpPlayerCommand");
                            out.writeUTF(args[1]);
                            out.writeUTF("console");
                            out.writeUTF(args[0]);
                            Player p = Bukkit.getPlayer(args[1]);
                            if (p != null) {
                                sendLastLoc(p);
                                p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    formator.sendMessage((Player) cs, config.load(null, "messages.yml").getString("no-permission"));
                    plugin.in_command.remove(cs);
                    return;
                }
            } else {
                formator.sendMessage((Player) cs, config.load("warps", "syntax.yml").getString("warp.teleport"));
                plugin.in_command.remove(cs);
                return;
            }

            plugin.in_command.remove(cs);

        });
        return true;
    }

    private void send(String[] args, Player p) {
        sendLastLoc(p);
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("WarpCommand");
            if (p.hasPermission("masuitewarps.warp.hidden")) {
                out.writeUTF("HIDDEN");
            } else {
                out.writeUTF("-------");
            }
            out.writeUTF(p.getName());
            out.writeUTF(args[0]);
            p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Boolean checkWarp(CommandSender cs, String name) {
        if (MaSuiteWarps.warpNames.contains(name.toLowerCase())) {
            return true;
        } else {
            formator.sendMessage((Player) cs, config.load("warps", "messages.yml").getString("warp-not-found"));
            return false;
        }
    }

    private Boolean checkCooldown(Player p) {
        if (plugin.getConfig().getInt("cooldown") > 0) {
            if (MaSuiteWarps.cooldowns.containsKey(p.getUniqueId())) {
                if (System.currentTimeMillis() - MaSuiteWarps.cooldowns.get(p.getUniqueId()) > plugin.getConfig().getInt("cooldown") * 1000) {
                    MaSuiteWarps.cooldowns.remove(p.getUniqueId());
                    return true;
                } else {
                    formator.sendMessage(p, config.load("warps", "messages.yml").getString("in-cooldown").replace("%time%", plugin.getConfig().getString("cooldown")));
                    return false;
                }
            } else {
                return true;
            }
        }
        return true;
    }

    private void sendLastLoc(Player p) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("MaSuiteTeleports");
            out.writeUTF("GetLocation");
            out.writeUTF(p.getName());
            Location loc = p.getLocation();
            out.writeUTF(loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":"
                    + loc.getYaw() + ":" + loc.getPitch());
            out.writeUTF("DETECTSERVER");
            p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
