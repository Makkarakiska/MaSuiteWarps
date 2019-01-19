package fi.matiaspaavilainen.masuitewarps.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    private MaSuiteWarps plugin;

    public TabCompleter(MaSuiteWarps p) {
        plugin = p;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if(cmd.getName().equalsIgnoreCase("warp") || cmd.getName().equalsIgnoreCase("delwarp")){
            if (args.length == 1) {
                List<String> warps = new ArrayList<>();
                MaSuiteWarps.warps.values().forEach(warp -> {
                    String type = "global";
                    if(!warp.isGlobal()){
                        type = "server";
                    }
                    if(warp.isHidden() && !sender.hasPermission("masuitewarps.list.hidden")){
                        return;
                    }
                    if(!sender.hasPermission("masuitewarps.list." + type)){
                        return;
                    }
                    warps.add(warp.getName());
                });
                return new ArrayList<>(warps);
            }
        }
        return null;
    }
}
