package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Set extends Command {
    public Set() {
        super("setwarp", "masuitewarps.setwarp");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        Configuration config = new Configuration();
        Formator formator = new Formator();
        if(!(cs instanceof ProxiedPlayer)){
            return;
        }
        ProxiedPlayer sender = (ProxiedPlayer) cs;
        if(args.length == 1){
            Warp warp = new Warp(args[0], sender.getServer().getInfo().getName(), "world", 100.0, 100.0, 100.0, 10.0, 100.0, false, true);
            warp.create(warp);
        }
        else if(args.length == 2){
            if(args[1].equalsIgnoreCase("hidden") || args[1].equalsIgnoreCase("global")){
                Warp warp = new Warp(args[0], sender.getServer().getInfo().getName(), "world", 100.0, 100.0, 100.0, 10.0, 100.0, false, true);
                warp.create(warp);
            }else{
                formator.sendMessage(sender, config.load("warps", "syntax.yml").getString("warp.set"));
            }

        }
        else{
            // config.load("warps", "syntax.yml").getString("warp.set")
            formator.sendMessage(sender, "test");
        }

    }
}
