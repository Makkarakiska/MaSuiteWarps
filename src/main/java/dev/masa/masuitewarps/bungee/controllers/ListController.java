package dev.masa.masuitewarps.bungee.controllers;

import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class ListController {

    private MaSuiteWarps plugin;

    public ListController(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public void listWarp(ProxiedPlayer p, boolean hasAccessToGlobal, boolean hasAccessToServer, boolean hasAccessToHidden) {
        TextComponent global = new TextComponent(plugin.formator.colorize(plugin.listHeaderGlobal));
        TextComponent server = new TextComponent(plugin.formator.colorize(plugin.listHeaderServer));
        TextComponent hidden = new TextComponent(plugin.formator.colorize(plugin.listHeaderHidden));

        List<Warp> warps = plugin.warpService.getAllWarps();

        int i = 0;
        String split = plugin.formator.colorize(plugin.listWarpSplitter);
        for (Warp warp : warps) {
            if (warp.isGlobal() && !warp.isHidden()) {
                global.addExtra(buildAndAddListElement(warp));
                if (i != warps.size() - 1) {
                    global.addExtra(split);
                }
            }
            if (!warp.isGlobal() && warp.getLocation().getServer().equals(p.getServer().getInfo().getName()) && !warp.isHidden()) {
                server.addExtra(buildAndAddListElement(warp));
                if (i != warps.size() - 1) {
                    server.addExtra(split);
                }
            }
            if (warp.isHidden()) {
                hidden.addExtra(buildAndAddListElement(warp));
                if (i != warps.size() - 1) {
                    hidden.addExtra(split);
                }
            }
            i++;
        }

        if (hasAccessToGlobal) {
            p.sendMessage(global);
        }
        if (hasAccessToServer) {
            p.sendMessage(server);
        }
        if (hasAccessToHidden) {
            p.sendMessage(hidden);
        }
    }

    private TextComponent buildAndAddListElement(Warp warp) {
        TextComponent textComponent = new TextComponent(plugin.formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));

        return textComponent;
    }
}
