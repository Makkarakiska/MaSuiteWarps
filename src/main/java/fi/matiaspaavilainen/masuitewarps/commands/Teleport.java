package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Teleport {
    private Formator formator = new Formator();
    private Configuration config = new Configuration();

    public void warp(ProxiedPlayer p, Warp warp, String type) {
        if (!p.hasPermission("masuitewarps.warp")) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }

        if (warp.getServer() == null) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
            return;
        }
        if (config.load("warps", "settings.yml").getBoolean("enable-per-warp-permission")) {
            if (!p.hasPermission("masuitewarps.warp.to." + warp.getName().toLowerCase()) && !p.hasPermission("masuitewarps.warp.to.*")) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
                return;
            }
        }

        if (!warp.isGlobal()) {
            if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-in-other-server"));
                return;
            }

        }
        if (type.equals("sign") && (!p.hasPermission("masuitewarps.warp.hidden.sign.use") || !p.hasPermission("masuitewarps.warp.global.sign.use") || !p.hasPermission("masuitewarps.warp.server.sign.use"))) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
        if (type.equals("command") && warp.isHidden() && !p.hasPermission("masuitewarps.warp.hidden")) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
        warpPlayer(p, warp);
    }

    public void warp(ProxiedPlayer p, String s, Warp warp, String type) {
        if (!s.equals("console")) {
            ProxiedPlayer sender = ProxyServer.getInstance().getPlayer(s);
            if (sender == null) {
                return;
            }
            if (!sender.hasPermission("masuitewarps.warp.others")) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
                return;
            }

            if (warp.getServer() == null) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
                return;
            }
            if (config.load("warps", "settings.yml").getBoolean("enable-per-warp-permission")) {
                if (!sender.hasPermission("masuitewarps.warp.to." + warp.getName().toLowerCase()) && !sender.hasPermission("masuitewarps.warp.to.*")) {
                    formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
                    return;
                }
            }

            if (!warp.isGlobal()) {
                if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                    formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-in-other-server"));
                    return;
                }

            }
            if (type.equals("sign") && (!sender.hasPermission("masuitewarps.warp.hidden.sign.use") || !sender.hasPermission("masuitewarps.warp.global.sign.use") || !sender.hasPermission("masuitewarps.warp.server.sign.use"))) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
                return;
            }
            if (type.equals("command") && warp.isHidden() && !sender.hasPermission("masuitewarps.warp.hidden")) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
                return;
            }
        }
        warpPlayer(p, warp);
    }

    private void warpPlayer(ProxiedPlayer p, Warp warp) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                p.connect(ProxyServer.getInstance().getServerInfo(warp.getServer()));
            }
            out.writeUTF("WarpPlayer");
            out.writeUTF(String.valueOf(p.getUniqueId()));
            out.writeUTF(warp.getLocation().getWorld());
            out.writeDouble(warp.getLocation().getX());
            out.writeDouble(warp.getLocation().getY());
            out.writeDouble(warp.getLocation().getZ());
            out.writeFloat(warp.getLocation().getYaw());
            out.writeFloat(warp.getLocation().getPitch());
            final Warp wp = warp;
            if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                ProxyServer.getInstance().getScheduler().schedule(new MaSuiteWarps(), () -> ProxyServer.getInstance().getServerInfo(wp.getServer()).sendData("BungeeCord", b.toByteArray()), 500, TimeUnit.MILLISECONDS);
            } else {
                ProxyServer.getInstance().getServerInfo(wp.getServer()).sendData("BungeeCord", b.toByteArray());
            }
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("teleported").replace("%warp%", warp.getName()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
