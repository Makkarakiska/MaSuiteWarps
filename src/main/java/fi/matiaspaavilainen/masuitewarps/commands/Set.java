package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitecore.managers.Location;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Set {

    private Configuration config = new Configuration();
    private Formator formator = new Formator();


    public void setWarp(ProxiedPlayer p, String name, Location loc) {
        if(!p.hasPermission("masuitewarps.setwarp")){
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
        Warp wp = new Warp();
        wp = wp.find(name);
        Warp warp = new Warp(name, p.getServer().getInfo().getName(), loc, false, true);
        warp.create(warp);
        if (wp.getServer() != null) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-updated").replace("%warp%", warp.getName()));
        } else {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-created").replace("%warp%", warp.getName()));
        }
    }
    public void setWarp(ProxiedPlayer p, String name, Location loc, String type) {
        if(!p.hasPermission("masuitewarps.setwarp")){
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
        Warp wp = new Warp();
        wp = wp.find(name);
        boolean hidden = false;
        boolean global = true;
        if (wp.getServer() != null) {
            hidden = wp.isHidden();
            global = wp.isGlobal();
        }

        if (type.equalsIgnoreCase("hidden")) {
            hidden = !hidden;
        } else if (type.equalsIgnoreCase("global")) {
            global = !global;
        } else {
            return;
        }
        Warp warp = new Warp(name, p.getServer().getInfo().getName(), loc, hidden, global);
        warp.create(warp);
        if (wp.getServer() != null) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-updated").replace("%warp%", warp.getName()));
        } else {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-created").replace("%warp%", warp.getName()));
        }
    }
}
