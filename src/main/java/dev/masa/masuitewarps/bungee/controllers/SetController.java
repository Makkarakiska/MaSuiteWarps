package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitecore.core.channels.BungeePluginChannel;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;

public class SetController {

    private MaSuiteWarps plugin;

    public SetController(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public Warp setWarp(ProxiedPlayer player, String name, Location loc, boolean publicity, boolean type) {
        Warp warp = plugin.warpService.getWarp(name);

        boolean exists = warp != null;
        loc.setServer(player.getServer().getInfo().getName());

        if (warp == null) {
            warp = new Warp(name, publicity, type, loc);
        } else {
            warp.setHidden(publicity);
            warp.setGlobal(type);
        }

        return create(player, warp, exists);
    }

    private Warp create(ProxiedPlayer player, Warp warp, boolean exists) {
        if (exists) {
            plugin.warpService.updateWarp(warp);
            plugin.formator.sendMessage(player, plugin.warpUpdated.replace("%warp%", warp.getName()));
        } else {
            plugin.warpService.createWarp(warp);
            plugin.formator.sendMessage(player, plugin.warpCreated.replace("%warp%", warp.getName()));
        }

        for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
            ServerInfo serverInfo = entry.getValue();
            serverInfo.ping((result, error) -> {
                if (error == null) {
                    new BungeePluginChannel(plugin, serverInfo, "CreateWarp", warp.serialize()).send();
                }
            });
        }
        return warp;
    }
}
