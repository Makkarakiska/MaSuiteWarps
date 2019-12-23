package fi.matiaspaavilainen.masuitewarps.bukkit.commands;

import fi.matiaspaavilainen.masuitecore.acf.BaseCommand;
import fi.matiaspaavilainen.masuitecore.acf.annotation.*;
import fi.matiaspaavilainen.masuitecore.acf.bukkit.contexts.OnlinePlayer;
import fi.matiaspaavilainen.masuitecore.core.channels.BukkitPluginChannel;
import fi.matiaspaavilainen.masuitewarps.bukkit.MaSuiteWarps;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand extends BaseCommand {

    private MaSuiteWarps plugin;

    public WarpCommand(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }


    // TODO: Add warmup, cooldown and last location
    @CommandAlias("warp")
    @CommandPermission("masuitewarps.warp")
    @Description("Warps to target")
    @CommandCompletion("@warps")
    public void teleportWarpCommand(CommandSender sender, String warp, @Optional @CommandPermission("masuitewarps.warp.other") OnlinePlayer onlinePlayer) {
        if (!plugin.warps.containsKey(warp)) {
            // TODO: Return not found message
            return;
        }
        if (!(sender instanceof Player) || onlinePlayer != null) {
            new BukkitPluginChannel(plugin, onlinePlayer.player, new Object[]{"WarpCommand", onlinePlayer.player.getName(),  warp, true}).send();
            return;
        }

        Player player = (Player) sender;

        if(player.hasPermission("masuitewarps.warp.to." + warp) || player.hasPermission("masuitewarps.warp.to.*")){
            // TODO: Add no permission message
            return;
        }
        new BukkitPluginChannel(plugin, player, new Object[]{"WarpCommand", player.getName(), warp.toLowerCase(), player.hasPermission("masuitewarps.warp.hidden")}).send();
    }

}
