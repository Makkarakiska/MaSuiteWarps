package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
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
            Configuration config = new Configuration();
            Warp warp = new Warp();
            warp = warp.find(args[0]);
            if(warp.getServer() == null){
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
            }else{
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("teleported").replace("%warp%", warp.getName()));
            }

        }
    }
}
