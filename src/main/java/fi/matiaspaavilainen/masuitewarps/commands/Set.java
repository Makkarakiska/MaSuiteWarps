package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitecore.listeners.MaSuitePlayerLocation;
import fi.matiaspaavilainen.masuitecore.managers.Location;
import fi.matiaspaavilainen.masuitecore.managers.MaSuitePlayer;
import fi.matiaspaavilainen.masuitewarps.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.TimeUnit;

public class Set extends Command {

    private MaSuiteWarps plugin;

    public Set(MaSuiteWarps p) {
        super("setwarp", "masuitewarps.setwarp");
        plugin = p;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        Configuration config = new Configuration();
        Formator formator = new Formator();
        if (!(cs instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer sender = (ProxiedPlayer) cs;
        if (args.length == 1) {
            MaSuitePlayer msp = new MaSuitePlayer().find(sender.getUniqueId());
            msp.requestLocation();
            ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    Location loc = MaSuitePlayerLocation.locations.get(sender.getUniqueId());
                    Warp warp = new Warp(args[0], sender.getServer().getInfo().getName(), loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), false, true);
                    warp.create(warp);
                    formator.sendMessage(sender, config.load("warps", "messages.yml").getString("warp-created").replace("%warp%", warp.getName()));
                }
            }, 2, TimeUnit.SECONDS)
            ;

            MaSuitePlayerLocation.locations.remove(sender.getUniqueId());

        } else if (args.length == 3) {
            MaSuitePlayer msp = new MaSuitePlayer().find(sender.getUniqueId());
            msp.requestLocation();
            Location loc = MaSuitePlayerLocation.locations.get(sender.getUniqueId());

            boolean hidden = false;
            boolean global = true;
            if (args[1].equalsIgnoreCase("hidden")) {
                hidden = args[2].equalsIgnoreCase("true");
            } else if (args[1].equalsIgnoreCase("global")) {
                global = args[2].equalsIgnoreCase("true");
            } else {
                formator.sendMessage(sender, config.load("warps", "syntax.yml").getString("warp.set"));
            }

            Warp warp = new Warp(args[0], sender.getServer().getInfo().getName(), loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), hidden, global);
            warp.create(warp);
            formator.sendMessage(sender, config.load("warps", "messages.yml").getString("warp-created").replace("%warp%", warp.getName()));
            MaSuitePlayerLocation.locations.remove(sender.getUniqueId());
        } else {
            formator.sendMessage(sender, config.load("warps", "syntax.yml").getString("warp.set"));
        }

    }
}
