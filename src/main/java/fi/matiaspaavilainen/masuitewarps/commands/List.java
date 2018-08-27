package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.Map;

public class List extends Command {
    public List() {
        super("warps", "masuitewarps.warp", "listwarps", "waprslist");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        Warp w = new Warp();
        System.out.println(w.all());
    }
}
