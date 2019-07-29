package fi.matiaspaavilainen.masuitewarps.bungee.commands;

import fi.matiaspaavilainen.masuitecore.bungee.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.configuration.BungeeConfiguration;
import fi.matiaspaavilainen.masuitewarps.bungee.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.core.objects.Warp;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Set;

public class List {

    private MaSuiteWarps plugin;
    public List(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public void listWarp(ProxiedPlayer p, String permissions) {
        Warp w = new Warp();
        Formator formator = new Formator();
        TextComponent global = new TextComponent(formator.colorize(plugin.listHeaderGlobal));
        TextComponent server = new TextComponent(formator.colorize(plugin.listHeaderServer));
        TextComponent hidden = new TextComponent(formator.colorize(plugin.listHeaderHidden));

        Set<Warp> warps = w.all();
        int i = 0;
        String split = formator.colorize(plugin.listWarpSplitter);
        for (Warp warp : warps) {
            if (warp.isGlobal() && !warp.isHidden()) {
                if (i++ == warps.size() - 1) {
                    TextComponent hc = new TextComponent(formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    global.addExtra(hc);
                } else {
                    TextComponent hc = new TextComponent(formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    global.addExtra(hc);
                    global.addExtra(split);
                }
            }
            if (!warp.isGlobal() && warp.getServer().equals(p.getServer().getInfo().getName()) && !warp.isHidden()) {
                if (i++ == warps.size() - 1) {
                    TextComponent hc = new TextComponent(formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    server.addExtra(hc);
                } else {
                    TextComponent hc = new TextComponent(formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    server.addExtra(hc);
                    server.addExtra(split);
                }
            }
            if (warp.isHidden()) {
                if (i++ == warps.size() - 1) {
                    TextComponent hc = new TextComponent(formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
                    hidden.addExtra(hc);
                } else {
                    TextComponent hc = new TextComponent(formator.colorize(plugin.listWarpName.replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(plugin.listHoverText.replace("%warp%", warp.getName()))).create()));
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
