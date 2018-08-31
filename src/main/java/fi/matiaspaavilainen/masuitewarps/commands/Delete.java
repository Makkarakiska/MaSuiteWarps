package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Delete extends Command {
    public Delete() {
        super("delwarp", "masuitewarps.delwarp", "deletewarp", "warpdel");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (!(cs instanceof ProxiedPlayer)) {
            return;
        }
        Formator formator = new Formator();
        Configuration config = new Configuration();
        ProxiedPlayer p = (ProxiedPlayer) cs;
        if (args.length == 1) {
            Warp warp = new Warp();
            warp = warp.find(args[0]);
            if (warp.getServer() == null) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
                return;
            }
            if (warp.delete(args[0])) {
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-deleted"));
            } else {
                formator.sendMessage(p, "&cAn error occured. Please check console for more details");
            }
        } else {
            formator.sendMessage(p, config.load("warps", "syntax.yml").getString("warp.delete"));
        }
    }
}
