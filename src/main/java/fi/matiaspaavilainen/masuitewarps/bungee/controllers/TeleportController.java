package fi.matiaspaavilainen.masuitewarps.bungee.controllers;

import fi.matiaspaavilainen.masuitecore.bungee.Utils;
import fi.matiaspaavilainen.masuitewarps.bungee.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TeleportController {
    private Utils utils = new Utils();
    private MaSuiteWarps plugin;

    public TeleportController(MaSuiteWarps p) {
        plugin = p;
    }

    public void teleport(ProxiedPlayer player, String name, boolean hasAccessToHidden) {
        Warp warp = plugin.warpService.getWarp(name);
        if (checkWarp(player, warp, true, true, hasAccessToHidden)) {
            plugin.warpService.teleportToWarp(player, warp);
        }
    }

    public void teleportSign(ProxiedPlayer player, String name, boolean hasAccessToGlobal, boolean hasAccessToServer, boolean hasAccessToHidden) {
        Warp warp = plugin.warpService.getWarp(name);
        if (checkWarp(player, warp, hasAccessToGlobal, hasAccessToServer, hasAccessToHidden)) {
            plugin.warpService.teleportToWarp(player, warp);
        }

    }

    private boolean checkWarp(ProxiedPlayer player, Warp warp, boolean hasAccessToGlobal, boolean hasAccessToServer, boolean hasAccessToHidden) {
        if (player == null) {
            return false;
        }
        if (warp == null) {
            plugin.formator.sendMessage(player, plugin.warpNotFound);
            return false;
        }
        if (warp.isHidden() && !hasAccessToHidden) {
            plugin.formator.sendMessage(player, plugin.noPermission);
            return false;
        }

        if (warp.isGlobal() && !hasAccessToGlobal) {
            plugin.formator.sendMessage(player, plugin.noPermission);
            return false;
        }

        if (!warp.isGlobal() && !hasAccessToServer) {
            plugin.formator.sendMessage(player, plugin.noPermission);
            return false;
        }

        if (!warp.isGlobal()) {
            if (!player.getServer().getInfo().getName().equals(warp.getLocation().getServer())) {
                plugin.formator.sendMessage(player, plugin.warpInOtherServer);
                return false;
            }
        }
        return true;
    }
}
