package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Set;
import java.util.stream.Collectors;

public class List extends Command {
    public List() {
        super("warps", "", "listwarps", "warplist");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!(cs instanceof ProxiedPlayer)) {
            return;
        }
        Warp w = new Warp();
        ProxiedPlayer p = (ProxiedPlayer) cs;
        Formator formator = new Formator();
        Configuration config = new Configuration();
        TextComponent global = new TextComponent(formator.colorize(config.load("warps", "messages.yml").getString("warp.global")));
        TextComponent server = new TextComponent(formator.colorize(config.load("warps", "messages.yml").getString("warp.server")));
        TextComponent hidden = new TextComponent(formator.colorize(config.load("warps", "messages.yml").getString("warp.hidden")));

        Set<Warp> warps = w.all();
        int i = 0;
        String split = formator.colorize(config.load("warps", "messages.yml").getString("warp.split"));
        for (Warp warp : warps) {
            if (warp.isGlobal() && warp.isHidden().equals(false)) {
                if (i++ == warps.size() - 1) {
                    TextComponent hc = new TextComponent(formator.colorize(config.load("warps", "messages.yml").getString("warp.name").replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(config.load("warps", "messages.yml").getString("warp-hover-text").replace("%warp%", warp.getName()))).create()));
                    global.addExtra(hc);
                } else {
                    TextComponent hc = new TextComponent(formator.colorize(config.load("warps", "messages.yml").getString("warp.name").replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(config.load("warps", "messages.yml").getString("warp-hover-text").replace("%warp%", warp.getName()))).create()));
                    global.addExtra(hc);
                    global.addExtra(split);
                }
            }
            if (warp.isGlobal().equals(false) && warp.getServer().equals(p.getServer().getInfo().getName()) && warp.isHidden().equals(false)) {
                if (i++ == warps.size() - 1) {
                    TextComponent hc = new TextComponent(formator.colorize(config.load("warps", "messages.yml").getString("warp.name").replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(config.load("warps", "messages.yml").getString("warp-hover-text").replace("%warp%", warp.getName()))).create()));
                    server.addExtra(hc);
                } else {
                    TextComponent hc = new TextComponent(formator.colorize(config.load("warps", "messages.yml").getString("warp.name").replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(config.load("warps", "messages.yml").getString("warp-hover-text").replace("%warp%", warp.getName()))).create()));
                    server.addExtra(hc);
                    server.addExtra(split);
                }
            }
            if (warp.isHidden()) {
                if (i++ == warps.size() - 1) {
                    TextComponent hc = new TextComponent(formator.colorize(config.load("warps", "messages.yml").getString("warp.name").replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(config.load("warps", "messages.yml").getString("warp-hover-text").replace("%warp%", warp.getName()))).create()));
                    hidden.addExtra(hc);
                } else {
                    TextComponent hc = new TextComponent(formator.colorize(config.load("warps", "messages.yml").getString("warp.name").replace("%warp%", warp.getName())));
                    hc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp.getName()));
                    hc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(formator.colorize(config.load("warps", "messages.yml").getString("warp-hover-text").replace("%warp%", warp.getName()))).create()));
                    hidden.addExtra(hc);
                    hidden.addExtra(split);
                }
            }
        }

        if (cs.hasPermission("masuitewarps.list.global")) {
            p.sendMessage(global);
        }
        if (cs.hasPermission("masuitewarps.list.server")) {
            p.sendMessage(server);
        }
        if (cs.hasPermission("masuitewarps.list.hidden")) {
            p.sendMessage(hidden);
        }
    }
}
