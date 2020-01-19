package fi.matiaspaavilainen.masuitewarps.bungee.commands;

import fi.matiaspaavilainen.masuitecore.core.channels.BungeePluginChannel;
import fi.matiaspaavilainen.masuitecore.core.objects.Location;
import fi.matiaspaavilainen.masuitewarps.bungee.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;

public class SetController {

    private MaSuiteWarps plugin;

    private boolean exists = false;

    public SetController(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public Warp setWarp(ProxiedPlayer player, String name, Location loc) {
        Warp warp = plugin.warpService.getWarp(name);
        loc.setServer(player.getServer().getInfo().getName());
        if (warp == null) {
            warp = new Warp(name, false, true, loc);
        } else {
            warp.setLocation(loc);
            exists = true;
        }
        return create(player, warp);
    }

    public Warp setWarp(ProxiedPlayer player, String name, Location loc, String type) {
        Warp warp = plugin.warpService.getWarp(name);

        loc.setServer(player.getServer().getInfo().getName());

        boolean hidden = false;
        boolean global = true;
        if (warp != null) {
            hidden = warp.isHidden();
            global = warp.isGlobal();
            exists = true;
        }

        if (type.equalsIgnoreCase("hidden")) {
            hidden = !hidden;
        } else if (type.equalsIgnoreCase("global")) {
            global = !global;
        } else {
            return null;
        }

        if (warp == null) {
            warp = new Warp(name, hidden, global, loc);
        }

        return create(player, warp);
    }

    private Warp create(ProxiedPlayer player, Warp warp) {
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
