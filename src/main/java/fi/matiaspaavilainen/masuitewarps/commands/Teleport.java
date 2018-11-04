package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.Utils;
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
    private Utils utils = new Utils();
    private MaSuiteWarps plugin;

    public Teleport(MaSuiteWarps p) {
        plugin = p;
    }

    public void warp(ProxiedPlayer p, Warp warp, String type, String permissions) {
        if (check(p, warp, p)) return;
        if (warp.isHidden() && !permissions.contains("HIDDEN")) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
        if (type.equals("sign")) {
            if (warp.isGlobal() && !permissions.contains("GLOBAL")) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
                return;
            }
            if (!warp.isGlobal() && !permissions.contains("SERVER")) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
                return;
            }
        }
        warpPlayer(p, warp);
    }

    public void warp(ProxiedPlayer p, String s, Warp warp, String type) {
        if (!s.equals("console")) {
            ProxiedPlayer sender = ProxyServer.getInstance().getPlayer(s);
            if (sender == null) {
                return;
            }
            if (utils.isOnline(p, sender)) {
                if (check(p, warp, sender)) return;
            }
        }
        if (utils.isOnline(p)) {
            warpPlayer(p, warp);
        }

    }

    private boolean check(ProxiedPlayer p, Warp warp, ProxiedPlayer sender) {
        if (warp.getServer() == null) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
            return true;
        }
        if (config.load("warps", "settings.yml").getBoolean("enable-per-warp-permission")) {
            if (!sender.hasPermission("masuitewarps.warp.to." + warp.getName().toLowerCase()) && !sender.hasPermission("masuitewarps.warp.to.*")) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
                return true;
            }
        }

        if (!warp.isGlobal()) {
            if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-in-other-server"));
                return true;
            }

        }
        return false;
    }

    private void warpPlayer(ProxiedPlayer p, Warp warp) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {

            if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                p.connect(ProxyServer.getInstance().getServerInfo(warp.getServer()));
            }
            out.writeUTF("WarpPlayer");
            out.writeUTF(p.getUniqueId().toString());
            out.writeUTF(warp.getLocation().getWorld()
                    + ":" + warp.getLocation().getX()
                    + ":" + warp.getLocation().getY()
                    + ":" + warp.getLocation().getZ()
                    + ":" + warp.getLocation().getYaw()
                    + ":" + warp.getLocation().getPitch());
            if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                ProxyServer.getInstance().getScheduler().schedule(plugin, () -> p.getServer().sendData("BungeeCord", b.toByteArray()), 500, TimeUnit.MILLISECONDS);
            } else {
                p.getServer().sendData("BungeeCord", b.toByteArray());
            }
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("teleported").replace("%warp%", warp.getName()));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
