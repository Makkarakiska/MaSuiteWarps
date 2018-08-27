package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.Map;

public class List extends Command {
    public List() {
        super("warps", "masuitewarps.warp.list", "listwarps", "waprslist");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        Warp w = new Warp();
        Formator formator = new Formator();

        StringBuilder global = new StringBuilder();
        StringBuilder hidden = new StringBuilder();

        global.append("&7Global warps: ");
        hidden.append("&7Hidden warps: ");

        for(Map.Entry<Integer, Warp> wp : w.all().entrySet()){
            Warp warp = wp.getValue();
            System.out.println(warp.getName() + warp.isHidden());
            hidden.append("&b").append(warp.getName()).append("&7, ");
            /*if(warp.isHidden()){
                hidden.append("&b").append(warp.getName()).append("&7, ");
            }
            if(warp.isGlobal()){
                global.append("&b").append(warp.getName()).append("&7, ");
            }*/
        }
        cs.sendMessage(new TextComponent(formator.colorize(global.toString())));
        cs.sendMessage(new TextComponent(formator.colorize(hidden.toString())));

    }
}
