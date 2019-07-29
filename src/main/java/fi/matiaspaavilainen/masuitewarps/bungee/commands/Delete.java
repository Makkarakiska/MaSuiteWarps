package fi.matiaspaavilainen.masuitewarps.bungee.commands;

import fi.matiaspaavilainen.masuitecore.bungee.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.channels.BungeePluginChannel;
import fi.matiaspaavilainen.masuitewarps.bungee.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.core.objects.Warp;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;

public class Delete {

    private MaSuiteWarps plugin;

    public Delete(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public void deleteWarp(ProxiedPlayer p, String name) {
        Formator formator = new Formator();
        Warp warp = new Warp();
        warp = warp.find(name);
        if (warp.getServer() == null) {
            formator.sendMessage(p, plugin.warpNotFound);
            return;
        }
        if (warp.delete(name)) {
            formator.sendMessage(p, plugin.warpDeleted.replace("%warp%", warp.getName()));
            for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
                ServerInfo serverInfo = entry.getValue();
                serverInfo.ping((result, error) -> {
                    if (error == null) {
                        new BungeePluginChannel(plugin, serverInfo, new Object[]{"DelWarp", name}).send();
                    }
                });
            }
        } else {
            formator.sendMessage(p, "&cAn error occurred. Please check console for more details");
        }
    }
}
