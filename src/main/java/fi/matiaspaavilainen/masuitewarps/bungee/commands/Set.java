package fi.matiaspaavilainen.masuitewarps.bungee.commands;

import fi.matiaspaavilainen.masuitecore.core.channels.BungeePluginChannel;
import fi.matiaspaavilainen.masuitecore.core.objects.Location;
import fi.matiaspaavilainen.masuitewarps.bungee.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.core.objects.Warp;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;
import java.util.StringJoiner;

public class Set {

    private MaSuiteWarps plugin;

    public Set(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public Warp setWarp(ProxiedPlayer p, String name, Location loc) {
        Warp wp = new Warp().find(name);
        Warp warp = new Warp(name, p.getServer().getInfo().getName(), loc, false, true);
        return create(p, wp, warp);
    }

    public Warp setWarp(ProxiedPlayer p, String name, Location loc, String type) {
        Warp wp = new Warp().find(name);
        boolean hidden = false;
        boolean global = true;
        if (wp.getServer() != null) {
            hidden = wp.isHidden();
            global = wp.isGlobal();
        }

        if (type.equalsIgnoreCase("hidden")) {
            hidden = !hidden;
        } else if (type.equalsIgnoreCase("global")) {
            global = !global;
        } else {
            return null;
        }
        Warp warp = new Warp(name, p.getServer().getInfo().getName(), loc, hidden, global);
        return create(p, wp, warp);
    }

    private Warp create(ProxiedPlayer p, Warp wp, Warp warp) {
        warp.create();
        if (wp.getServer() != null) {
            plugin.formator.sendMessage(p, plugin.warpUpdated.replace("%warp%", warp.getName()));
        } else {
            plugin.formator.sendMessage(p, plugin.warpCreated.replace("%warp%", warp.getName()));
        }

        StringJoiner info = new StringJoiner(":");
        Location loc = warp.getLocation();
        info.add(warp.getName())
                .add(warp.getServer())
                .add(loc.getWorld())
                .add(loc.getX().toString())
                .add(loc.getY().toString())
                .add(loc.getZ().toString())
                .add(warp.isGlobal().toString())
                .add(warp.isHidden().toString());
        for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
            ServerInfo serverInfo = entry.getValue();
            serverInfo.ping((result, error) -> {
                if (error == null) {
                    new BungeePluginChannel(plugin, serverInfo, new Object[]{"CreateWarp", info.toString()}).send();
                }
            });
        }
        return warp;
    }
}
