package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Delete{

    public void deleteWarp(ProxiedPlayer p, String name) {
        Formator formator = new Formator();
        Configuration config = new Configuration();
        if (!p.hasPermission("masuitewarps.delwarp")) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("no-permission"));
            return;
        }
        Warp warp = new Warp();
        warp = warp.find(name);
        if (warp.getServer() == null) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
            return;
        }
        if (warp.delete(name)) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-deleted"));
        } else {
            formator.sendMessage(p, "&cAn error occured. Please check console for more details");
        }
        //formator.sendMessage(p, config.load("warps", "syntax.yml").getString("warp.delete"));
    }
}
