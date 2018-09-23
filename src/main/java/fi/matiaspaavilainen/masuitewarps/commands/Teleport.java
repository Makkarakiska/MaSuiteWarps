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
        if(!p.hasPermission("masuitewarps.warp")){
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        if (warp.getServer() == null) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
            return;
        }
        if(config.load("warps","settings.yml").getBoolean("enable-per-warp-permission")){
            if(!p.hasPermission("masuitewarps.warp.to." + warp.getName().toLowerCase()) && !p.hasPermission("masuitewarps.warp.to.*")){
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
        if(type.equals("sign") && (!p.hasPermission("masuitewarps.warp.hidden.sign.use") || !p.hasPermission("masuitewarps.warp.global.sign.use") || !p.hasPermission("masuitewarps.warp.server.sign.use"))){
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
        if (type.equals("command") && warp.isHidden() && !p.hasPermission("masuitewarps.warp.hidden")) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
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
            ProxyServer.getInstance().getScheduler().schedule(new MaSuiteWarps(), () -> ProxyServer.getInstance().getServerInfo(wp.getServer()).sendData("BungeeCord", b.toByteArray()), 100, TimeUnit.MILLISECONDS);
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("teleported").replace("%warp%", warp.getName()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public void warp(ProxiedPlayer p, ProxiedPlayer sender, Warp warp, String type) {
        if(!sender.hasPermission("masuitewarps.warp.others")){
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        if (warp.getServer() == null) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
            return;
        }
        if(config.load("warps","settings.yml").getBoolean("enable-per-warp-permission")){
            if(!sender.hasPermission("masuitewarps.warp.to." + warp.getName().toLowerCase()) && !sender.hasPermission("masuitewarps.warp.to.*")){
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
        if(type.equals("sign") && (!sender.hasPermission("masuitewarps.warp.hidden.sign.use") || !sender.hasPermission("masuitewarps.warp.global.sign.use") || !sender.hasPermission("masuitewarps.warp.server.sign.use"))){
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
        if (type.equals("command") && warp.isHidden() && !sender.hasPermission("masuitewarps.warp.hidden")) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
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
            ProxyServer.getInstance().getScheduler().schedule(new MaSuiteWarps(), () -> ProxyServer.getInstance().getServerInfo(wp.getServer()).sendData("BungeeCord", b.toByteArray()), 100, TimeUnit.MILLISECONDS);
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("teleported").replace("%warp%", warp.getName()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
