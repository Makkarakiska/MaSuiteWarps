package fi.matiaspaavilainen.masuitewarps.bungee.commands;

import fi.matiaspaavilainen.masuitecore.bungee.Utils;
import fi.matiaspaavilainen.masuitecore.core.channels.BungeePluginChannel;
import fi.matiaspaavilainen.masuitewarps.bungee.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;

public class Teleport {
    private Utils utils = new Utils();
    private MaSuiteWarps plugin;

    public Teleport(MaSuiteWarps p) {
        plugin = p;
    }

    public void warp(ProxiedPlayer p, Warp warp, String type, String permissions, boolean perm) {
        if (check(p, warp, perm)) return;
        if (warp.isHidden() && !permissions.contains("HIDDEN")) {
            plugin.formator.sendMessage(p, plugin.noPermission);
            return;
        }
        if (type.equals("sign")) {
            if (warp.isGlobal() && !permissions.contains("GLOBAL")) {
                plugin.formator.sendMessage(p, plugin.noPermission);
                return;
            }
            if (!warp.isGlobal() && !permissions.contains("SERVER")) {
                plugin.formator.sendMessage(p, plugin.noPermission);
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
        if (warp.getLocation().getServer() == null) {
            plugin.formator.sendMessage(p, plugin.warpNotFound);
            return true;
        }
        if (plugin.perWarpPermission) {
            if (!perm) {
                plugin.formator.sendMessage(p, plugin.noPermission);
                return true;
            }
        }
        if (!warp.isGlobal()) {
            if (!p.getServer().getInfo().getName().equals(warp.getLocation().getServer())) {
                plugin.formator.sendMessage(p, plugin.warpInOtherServer);
                return true;
            }
        }
        return false;
    }

    private void warpPlayer(ProxiedPlayer p, Warp warp) {
        BungeePluginChannel bsc = new BungeePluginChannel(plugin, plugin.getProxy().getServerInfo(warp.getLocation().getServer()), new Object[]{
                "WarpPlayer",
                p.getUniqueId().toString(),
                warp.getLocation().getWorld() + ":" + warp.getLocation().getX() + ":" + warp.getLocation().getY() + ":" + warp.getLocation().getZ() + ":" + warp.getLocation().getYaw() + ":" + warp.getLocation().getPitch()
        });
        if (!p.getServer().getInfo().getName().equals(warp.getLocation().getServer())) {
            p.connect(ProxyServer.getInstance().getServerInfo(warp.getLocation().getServer()));
            ProxyServer.getInstance().getScheduler().schedule(plugin, bsc::send, plugin.warpDelay, TimeUnit.MILLISECONDS);
        } else {
            bsc.send();
        }
        plugin.formator.sendMessage(p, plugin.teleported.replace("%warp%", warp.getName()));
    }
}
