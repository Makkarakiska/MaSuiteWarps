package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Set;
import java.util.stream.Collectors;

public class List extends Command {
    public List() {
        super("warps", "masuitewarps.list", "listwarps", "warplist");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if(!(cs instanceof ProxiedPlayer)){
            return;
        }
        Warp w = new Warp();
        ProxiedPlayer p = (ProxiedPlayer) cs;
        Formator formator = new Formator();
        Configuration config = new Configuration();
        String global = config.load("warps", "messages.yml").getString("warp.global");
        String server = config.load("warps", "messages.yml").getString("warp.server");
        String hidden = config.load("warps", "messages.yml").getString("warp.hidden");

        Set<Warp> warps = w.all();
        global = global + warps.stream().filter(Warp::isGlobal).map(Warp::getName).collect(Collectors.joining(config.load("warps", "messages.yml").getString("warp.split")));
        server = server + warps.stream().filter(wp -> wp.isGlobal().equals(false) && wp.getServer().equals(p.getServer().getInfo().getName())).map(Warp::getName).collect(Collectors.joining(config.load("warps", "messages.yml").getString("warp.split")));
        hidden = hidden + warps.stream().filter(Warp::isHidden).map(Warp::getName).collect(Collectors.joining(config.load("warps", "messages.yml").getString("warp.split")));

        if(cs.hasPermission("masuitewarps.list.global")) {
            formator.sendMessage(p, global);
        }
        if(cs.hasPermission("masuitewarps.list.server")) {
            formator.sendMessage(p, server);
        }
        if(cs.hasPermission("masuitewarps.list.hidden")) {
            formator.sendMessage(p, hidden);
        }
    }
}
