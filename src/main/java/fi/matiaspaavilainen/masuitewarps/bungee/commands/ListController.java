package fi.matiaspaavilainen.masuitewarps.bungee.commands;

import fi.matiaspaavilainen.masuitewarps.bungee.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.core.models.Warp;
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

    public void listWarp(ProxiedPlayer p, String permissions) {
        TextComponent global = new TextComponent(plugin.formator.colorize(plugin.listHeaderGlobal));
        TextComponent server = new TextComponent(plugin.formator.colorize(plugin.listHeaderServer));
        TextComponent hidden = new TextComponent(plugin.formator.colorize(plugin.listHeaderHidden));

        List<Warp> warps = plugin.warpService.getAllWarps();

        int i = 0;
        String split = plugin.formator.colorize(plugin.listWarpSplitter);
        for (Warp warp : plugin.warpService.getAllWarps()) {
            if (warp.isGlobal() && !warp.isHidden()) {
                if (i++ == warps.size() - 1) {
                    TextComponent hc = new TextComponent(plugin.formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    global.addExtra(hc);
                } else {
                    TextComponent hc = new TextComponent(plugin.formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    global.addExtra(hc);
                    global.addExtra(split);
                }
            }
            if (!warp.isGlobal() && warp.getLocation().getServer().equals(p.getServer().getInfo().getName()) && !warp.isHidden()) {
                if (i++ == warps.size() - 1) {
                    TextComponent hc = new TextComponent(plugin.formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    server.addExtra(hc);
                } else {
                    TextComponent hc = new TextComponent(plugin.formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    server.addExtra(hc);
                    server.addExtra(split);
                }
            }
            if (warp.isHidden()) {
                if (i++ == warps.size() - 1) {
                    TextComponent hc = new TextComponent(plugin.formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    hidden.addExtra(hc);
                } else {
                    TextComponent hc = new TextComponent(plugin.formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    hidden.addExtra(hc);
                    hidden.addExtra(split);
                }
            }
        }

        if (permissions.contains("GLOBAL")) {
            p.sendMessage(global);
        }
        if (permissions.contains("SERVER")) {
            p.sendMessage(server);
        }
        if (permissions.contains("HIDDEN")) {
            p.sendMessage(hidden);
        }
    }
}
