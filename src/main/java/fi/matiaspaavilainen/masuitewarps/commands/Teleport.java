package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Teleport extends Command {
    public Teleport() {
        super("warp", "masuitewarps.warp");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if(!(cs instanceof ProxiedPlayer)){
         return;
        }
        if(args.length == 1){
            ProxiedPlayer p = (ProxiedPlayer) cs;
            Formator formator = new Formator();
            Warp warp = new Warp();
            warp = warp.find(args[0]);
            System.out.println(args[0] + " " + warp.getName() + warp.getServer());
            p.sendMessage(formator.colorize("&7Teleported to &b" + warp.getName()));
        }
    }
}
