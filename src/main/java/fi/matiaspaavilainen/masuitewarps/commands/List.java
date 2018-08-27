package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class List extends Command {
    public List() {
        super("warps", "masuitewarps.warp.list", "listwarps", "waprslist");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        Warp w = new Warp();
        Formator formator = new Formator();

        String global = "&7Global warps: &b";
        String server = "&7Server warps: &b";
        String hidden = "&7Hidden warps: &b";

        global = global + w.all().stream().filter(Warp::isGlobal).map(Warp::getName).collect(Collectors.joining("&7, &b"));
        //server = server + w.all().stream().filter(wp -> !wp.isGlobal()).map(Warp::getName).collect(Collectors.joining("&7, &b"));
        hidden = hidden + w.all().stream().filter(Warp::isHidden).map(Warp::getName).collect(Collectors.joining("&7, &b"));

        cs.sendMessage(new TextComponent(formator.colorize(global)));
        cs.sendMessage(new TextComponent(formator.colorize(server)));
        cs.sendMessage(new TextComponent(formator.colorize(hidden)));

    }
}
