package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class List extends Command {
    public List() {
        super("warps", "masuitewarps.warp", "listwarps", "waprslist");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        System.out.println(new Warp().all());
    }
}
