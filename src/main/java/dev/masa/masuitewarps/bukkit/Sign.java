package dev.masa.masuitewarps.bukkit;

import dev.masa.masuitecore.core.channels.BukkitPluginChannel;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class Sign implements Listener {

    private MaSuiteWarps plugin;

    public Sign(MaSuiteWarps p) {
        plugin = p;
    }

    private String[] formats() {
        FileConfiguration fc = plugin.config.load("warps", "config.yml");
        return new String[]{fc.getString("warp-sign.first"),
                fc.getString("warp-sign.second"),
                fc.getString("warp-sign.third"),
                fc.getString("warp-sign.fourth")};
    }

    @EventHandler
    public void onSignPlaced(SignChangeEvent e) {
        if (!e.getPlayer().hasPermission("masuitewarps.warp.sign.create")) {
            return;
        }
        if (e.getLine(1).equalsIgnoreCase("[Warp]") && e.getLine(2) != null) {
            String warpLine = e.getLine(2);
            for (int i = 0; i < 4; i++) {
                e.setLine(i, colorize(formats()[i].replace("%warp%", warpLine)));
            }
        }
    }

    @EventHandler
    public void onSignClicked(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (block != null && block.getType().toString().contains("SIGN")) {
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();
            if (checkSign(sign)) {

                String warp = ChatColor.stripColor(sign.getLine(getWarpLine()));

                new BukkitPluginChannel(plugin, player, "Warp", player.getName(), warp,
                        player.hasPermission("masuitewarps.warp.sign.global"),
                        player.hasPermission("masuitewarps.warp.sign.server"),
                        player.hasPermission("masuitewarps.warp.sign.hidden"), false).send();
            }

        }

    }

    private int getWarpLine() {
        int warpLine = 0;

        for (int i = 0; i < 4; i++) {
            if (ChatColor.stripColor(colorize(formats()[i])).equals(ChatColor.stripColor(colorize("%warp%")))) {
                warpLine = i;
                break;
            }
        }
        return warpLine;
    }

    private Boolean checkSign(org.bukkit.block.Sign sign) {
        List<Boolean> lines = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i == getWarpLine()) {
                continue;
            }
            if (sign.getLine(i).equals(colorize(formats()[i]))) {
                lines.add(true);
            }
        }
        return lines.size() == 3;

    }

    private String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
