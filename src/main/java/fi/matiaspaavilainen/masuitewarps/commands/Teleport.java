package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Teleport extends Command {
    MaSuiteWarps plugin;

    public Teleport(MaSuiteWarps p) {
        super("warp", "masuitewarps.warp", "warpto");
        plugin = p;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!(cs instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) cs;
        Formator formator = new Formator();
        Configuration config = new Configuration();
        if (args.length == 1) {
            Warp warp = new Warp();
            warp = warp.find(args[0]);
            if (warp.getServer() == null) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
                return;
            }
            if (!warp.isGlobal()) {
                if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                    formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-in-other-server"));
                    return;
                }

            }
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            if (warp.isHidden() && !cs.hasPermission("masuitewarps.warp.hidden")) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
                return;
            }
            warp(p, warp, b, out);
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("teleported").replace("%warp%", warp.getName()));

        } else if (args.length == 2) {
            if (p.hasPermission("masuitewarps.warp.others")) {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                if (target == null) {
                    cs.sendMessage(new TextComponent(formator.colorize(config.load(null, "messages.yml").getString("player-not-online"))));
                    return;
                }
                Warp warp = new Warp();
                warp = warp.find(args[0]);
                if (warp.getServer() == null) {
                    formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
                    return;
                }
                if (!warp.isGlobal()) {
                    if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                        formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-in-other-server"));
                        return;
                    }

                }
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                warp(target, warp, b, out);
                formator.sendMessage(target, config.load("warps", "messages.yml").getString("teleported").replace("%warp%", warp.getName()));
            }
        } else {
            formator.sendMessage(p, config.load("warps", "syntax.yml").getString("warp.teleport"));
        }

    }

    private void warp(ProxiedPlayer p, Warp warp, ByteArrayOutputStream b, DataOutputStream out) {
        try {
            if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                p.connect(ProxyServer.getInstance().getServerInfo(warp.getServer()));
            }
            out.writeUTF("WarpPlayer");
            out.writeUTF(String.valueOf(p.getUniqueId()));
            out.writeUTF(warp.getWorld());
            out.writeDouble(warp.getX());
            out.writeDouble(warp.getY());
            out.writeDouble(warp.getZ());
            out.writeFloat(warp.getYaw());
            out.writeFloat(warp.getPitch());
            final Warp wp = warp;
            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> ProxyServer.getInstance().getServerInfo(wp.getServer()).sendData("BungeeCord", b.toByteArray()), 100, TimeUnit.MILLISECONDS);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
