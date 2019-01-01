package fi.matiaspaavilainen.masuitewarps.bungee.commands;

import fi.matiaspaavilainen.masuitecore.bungee.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.configuration.BungeeConfiguration;
import fi.matiaspaavilainen.masuitecore.core.objects.Location;
import fi.matiaspaavilainen.masuitewarps.bungee.Warp;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Set {

    private BungeeConfiguration config = new BungeeConfiguration();
    private Formator formator = new Formator();


    public void setWarp(ProxiedPlayer p, String name, Location loc) {
        Warp wp = new Warp();
        wp = wp.find(name);
        Warp warp = new Warp(name, p.getServer().getInfo().getName(), loc, false, true);
        create(p, wp, warp);
    }

    public void setWarp(ProxiedPlayer p, String name, Location loc, String type) {
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
        create(p, wp, warp);
    }

    private void create(ProxiedPlayer p, Warp wp, Warp warp) {
        warp.create();
        if (wp.getServer() != null) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-updated").replace("%warp%", warp.getName()));
        } else {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-created").replace("%warp%", warp.getName()));
        }
    }
}
