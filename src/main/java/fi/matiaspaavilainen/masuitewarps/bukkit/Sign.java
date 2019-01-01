package fi.matiaspaavilainen.masuitewarps.bukkit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Sign implements Listener {

    private MaSuiteWarps plugin;

    public Sign(MaSuiteWarps p) {
        plugin = p;
    }

    private String[] formats() {
        return new String[]{plugin.getConfig().getString("warp-sign.first"),
                plugin.getConfig().getString("warp-sign.second"),
                plugin.getConfig().getString("warp-sign.third"),
                plugin.getConfig().getString("warp-sign.fourth")};
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
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();
        if (b.getType() == Material.LEGACY_SIGN_POST || b.getType() == Material.WALL_SIGN) {
            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) b.getState();
            if (checkSign(sign)) {
                StringJoiner types = new StringJoiner("");
                if (p.hasPermission("masuitewarps.warp.sign.global")) {
                    types.add("GLOBAL");
                }
                if (p.hasPermission("masuitewarps.warp.sign.server")) {
                    types.add("SERVER");
                }
                if (p.hasPermission("masuitewarps.warp.sign.hidden")) {
                    types.add("HIDDEN");
                }
                try (ByteArrayOutputStream bs = new ByteArrayOutputStream();
                     DataOutputStream out = new DataOutputStream(bs)) {
                    out.writeUTF("WarpSign");
                    out.writeUTF(types.toString());
                    out.writeUTF(p.getName());
                    out.writeUTF(ChatColor.stripColor(sign.getLine(getWarpLine())));
                    p.sendPluginMessage(plugin, "BungeeCord", bs.toByteArray());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
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
