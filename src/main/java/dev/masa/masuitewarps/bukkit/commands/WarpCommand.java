package dev.masa.masuitewarps.bukkit.commands;

import dev.masa.masuitecore.acf.BaseCommand;
import dev.masa.masuitecore.acf.annotation.*;
import dev.masa.masuitecore.acf.bukkit.contexts.OnlinePlayer;
import dev.masa.masuitecore.core.adapters.BukkitAdapter;
import dev.masa.masuitecore.core.channels.BukkitPluginChannel;
import dev.masa.masuitewarps.bukkit.MaSuiteWarps;
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
    @Conditions("cooldown:type=warps,bypass=masuitewarps.cooldown.override")
    public void teleportWarpCommand(CommandSender sender, @Single String name,
                                    @Optional @CommandPermission("masuitewarps.warp.other") OnlinePlayer onlinePlayer,
                                    @Optional @Single @CommandPermission("masuitewarps.warp.silent") String silentArg) {

        boolean silent = false;
        if (silentArg != null && silentArg.equalsIgnoreCase("-s")) {
            silent = true;
        }

        if (!(sender instanceof Player) || onlinePlayer != null) {
            new BukkitPluginChannel(plugin, onlinePlayer.player, "Warp", onlinePlayer.player.getName(), name, true, true, true, silent).send();
            return;
        }
        Player player = (Player) sender;

        // Check if player has permission to teleport to warp if per server warps is enabled
        if (plugin.perServerWarps) {
            if (!player.hasPermission("masuitewarps.warp.to." + name) && !player.hasPermission("masuitewarps.warp.to.*")) {
                sendNoPermissionMessage(player);
                return;
            }
        }

        boolean finalSilent = silent;
        plugin.api.getWarmupService().applyWarmup(player, "masuitewarps.warmup.override", "warps", success -> {
            if (success) {
                new BukkitPluginChannel(plugin, player, "MaSuiteTeleports", "GetLocation", player.getName(), BukkitAdapter.adapt(player.getLocation()).serialize()).send();
                new BukkitPluginChannel(plugin, player, "Warp", player.getName(), name.toLowerCase(),
                        player.hasPermission("masuitewarps.warp.global"),
                        player.hasPermission("masuitewarps.warp.server"),
                        player.hasPermission("masuitewarps.warp.hidden"),
                        finalSilent).send();
            }
        });
    }

    @CommandAlias("setwarp")
    @CommandPermission("masuitewarps.warp.set")
    @Description("Creates a new warp or updates an existing warp")
    @CommandCompletion("@warps hidden|public server|global")
    public void setWarpCommand(Player player, @Single String name, @Single String publicity, @Single String type) {
        Location loc = player.getLocation();
        String stringLocation = BukkitAdapter.adapt(loc).serialize();

        if (!publicity.equalsIgnoreCase("hidden") && !publicity.equalsIgnoreCase("public")) {
            return;
        }

        if (!type.equalsIgnoreCase("server") && !type.equalsIgnoreCase("global")) {
            return;
        }

        if (publicity.equalsIgnoreCase("hidden") && !player.hasPermission("masuitewarps.warp.set.hidden")) {
            sendNoPermissionMessage(player);
            return;
        }

        if (publicity.equalsIgnoreCase("public") && !player.hasPermission("masuitewarps.warp.set.public")) {
            sendNoPermissionMessage(player);
            return;
        }

        if (type.equalsIgnoreCase("server") && !player.hasPermission("masuitewarps.warp.set.server")) {
            sendNoPermissionMessage(player);
            return;
        }

        if (type.equalsIgnoreCase("global") && !player.hasPermission("masuitewarps.warp.set.global")) {
            sendNoPermissionMessage(player);
            return;
        }

        new BukkitPluginChannel(plugin, player, "SetWarp", player.getName(), name, stringLocation, publicity.equalsIgnoreCase("hidden"), type.equalsIgnoreCase("global")).send();
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
    @Description("Lists all of the warps")
    @CommandCompletion("@warps")
    public void listWarpCommand(Player player) {
        new BukkitPluginChannel(plugin, player, "ListWarps", player.getName(), player.hasPermission("masuitewarps.list.global"), player.hasPermission("masuitewarps.list.server"), player.hasPermission("masuitewarps.list.hidden")).send();
    }

    private void sendNoPermissionMessage(Player player) {
        plugin.formator.sendMessage(player, plugin.config.load(null, "messages.yml").getString("no-permission"));
    }

}
