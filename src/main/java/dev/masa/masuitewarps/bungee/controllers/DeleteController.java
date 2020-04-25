package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitecore.core.channels.BungeePluginChannel;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;

public class DeleteController {

    private MaSuiteWarps plugin;

    public DeleteController(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public void deleteWarp(ProxiedPlayer player, String name) {
        Warp warp = plugin.warpService.getWarp(name);
        if (warp == null) {
            plugin.formator.sendMessage(player, plugin.warpNotFound);
            return;
        }
        if (plugin.warpService.removeWarp(warp)) {
            plugin.formator.sendMessage(player, plugin.warpDeleted.replace("%warp%", warp.getName()));
            for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
                ServerInfo serverInfo = entry.getValue();
                serverInfo.ping((result, error) -> {
                    if (error == null) {
                        new BungeePluginChannel(plugin, serverInfo, "DelWarp", name).send();
                    }
                });
            }
        } else {
            plugin.formator.sendMessage(player, "&cAn error occurred. Please check console for more details");
        }
    }
}
