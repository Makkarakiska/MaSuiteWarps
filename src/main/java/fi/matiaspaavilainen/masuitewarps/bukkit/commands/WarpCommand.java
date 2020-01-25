package fi.matiaspaavilainen.masuitewarps.bukkit.commands;

import fi.matiaspaavilainen.masuitecore.acf.BaseCommand;
import fi.matiaspaavilainen.masuitecore.acf.annotation.*;
import fi.matiaspaavilainen.masuitecore.acf.bukkit.contexts.OnlinePlayer;
import fi.matiaspaavilainen.masuitecore.core.channels.BukkitPluginChannel;
import fi.matiaspaavilainen.masuitewarps.bukkit.MaSuiteWarps;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand extends BaseCommand {

    private MaSuiteWarps plugin;

    public WarpCommand(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("warp")
    @CommandPermission("masuitewarps.warp")
    @Description("Warps to target")
    @CommandCompletion("@warps @masuite_players")
    public void teleportWarpCommand(CommandSender sender, @Single String warp, @Optional @Single @CommandPermission("masuitewarps.warp.other") OnlinePlayer onlinePlayer) {
        if (!(sender instanceof Player) || onlinePlayer != null) {
            new BukkitPluginChannel(plugin, onlinePlayer.player, "WarpCommand", onlinePlayer.player.getName(), warp, true).send();
            return;
        }
        Player player = (Player) sender;
        new BukkitPluginChannel(plugin, player, "WarpCommand", player.getName(), warp.toLowerCase(), player.hasPermission("masuitewarps.warp.hidden")).send();
    }

    @CommandAlias("setwarp")
    @CommandPermission("masuitewarps.warp.set")
    @Description("Creates a new warp or updates an existing warp")
    @CommandCompletion("@warps hidden|global")
    public void setWarpCommand(Player player, @Single String name, @Optional @Single String setting) {
        Location loc = player.getLocation();
        String stringLocation = loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
        if (setting == null) {
            new BukkitPluginChannel(plugin, player, "SetWarp", 2, player.getName(), name, stringLocation).send();
            return;
        }

        if (setting.equalsIgnoreCase("hidden") || setting.equalsIgnoreCase("global")) {
            if (setting.equalsIgnoreCase("hidden") && !player.hasPermission("masuitewarps.setwarp.hidden")) {
                plugin.formator.sendMessage(player, plugin.noPermission);
                return;
            }
            if (setting.equalsIgnoreCase("global") && !player.hasPermission("masuitewarps.setwarp.global")) {
                plugin.formator.sendMessage(player, plugin.noPermission);
                return;
            }
            if (!setting.equalsIgnoreCase("global") && !setting.equalsIgnoreCase("hidden") && !player.hasPermission("masuitewarps.setwarp.server")) {
                plugin.formator.sendMessage(player, plugin.noPermission);
                return;
            }
            new BukkitPluginChannel(plugin, player, "SetWarp", 3, player.getName(), name, stringLocation, setting).send();
        }
    }

    @CommandAlias("delwarp|warpdel|deletewarp")
    @CommandPermission("masuitewarps.warp.delete")
    @Description("Deletes a warp")
    @CommandCompletion("@warps")
    public void delWarpCommand(Player player, @Single String name) {
        new BukkitPluginChannel(plugin, player, "DelWarp", player.getName(), name).send();
    }

    @CommandAlias("warps|listwarps|warplist")
    @CommandPermission("masuitewarps.warp.list")
    @Description("Deletes a warp")
    @CommandCompletion("@warps")
    public void listWarpCommand(Player player) {
        new BukkitPluginChannel(plugin, player, "ListWarps", plugin.checkPermissions(player), player.getName()).send();
    }

}
