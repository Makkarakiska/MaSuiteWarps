package fi.matiaspaavilainen.masuitewarps.bungee.commands;

import fi.matiaspaavilainen.masuitecore.bungee.Utils;
import fi.matiaspaavilainen.masuitecore.bungee.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.channels.BungeePluginChannel;
import fi.matiaspaavilainen.masuitecore.core.configuration.BungeeConfiguration;
import fi.matiaspaavilainen.masuitewarps.bungee.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.bungee.Warp;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;

public class Teleport {
    private Formator formator = new Formator();
    private BungeeConfiguration config = new BungeeConfiguration();
    private Utils utils = new Utils();
    private MaSuiteWarps plugin;

    public Teleport(MaSuiteWarps p) {
        plugin = p;
    }

    public void warp(ProxiedPlayer p, Warp warp, String type, String permissions, boolean perm) {
        if (check(p, warp, perm)) return;
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
                if (check(p, warp, true)) return;
            }
        }
        if (utils.isOnline(p)) {
            warpPlayer(p, warp);
        }
    }

    private boolean check(ProxiedPlayer p, Warp warp, boolean perm) {
        if (warp.getServer() == null) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
            return true;
        }
        if (config.load("warps", "settings.yml").getBoolean("enable-per-warp-permission")) {
            if (!perm) {
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
        BungeePluginChannel bsc = new BungeePluginChannel(plugin, plugin.getProxy().getServerInfo(warp.getServer()), new Object[]{
                "WarpPlayer",
                p.getUniqueId().toString(),
                warp.getLocation().getWorld() + ":" + warp.getLocation().getX() + ":" + warp.getLocation().getY() + ":" + warp.getLocation().getZ() + ":" + warp.getLocation().getYaw() + ":" + warp.getLocation().getPitch()
        });
        if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
            p.connect(ProxyServer.getInstance().getServerInfo(warp.getServer()));
            ProxyServer.getInstance().getScheduler().schedule(plugin, bsc::send, 500, TimeUnit.MILLISECONDS);
        } else {
            bsc.send();
        }
        formator.sendMessage(p, config.load("warps", "messages.yml").getString("teleported").replace("%warp%", warp.getName()));
    }
}
