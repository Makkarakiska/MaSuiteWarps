package fi.matiaspaavilainen.masuitewarps.bukkit;

import fi.matiaspaavilainen.masuitecore.core.objects.Location;
import fi.matiaspaavilainen.masuitewarps.core.models.Warp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class WarpMessageListener implements org.bukkit.plugin.messaging.PluginMessageListener {

    private MaSuiteWarps plugin;

    public WarpMessageListener(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        String subchannel = null;
        try {
            subchannel = in.readUTF();
            if (subchannel.equals("WarpPlayer")) {
                Player p = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
                if (p == null) {
                    return;
                }
                String[] loc = in.readUTF().split(":");
                if (Bukkit.getWorld(loc[0]) == null) {
                    System.out.println("[MaSuite] [Warps] [World=" + loc[0] + "] World  could not be found!");
                    return;
                }
                p.teleport(new org.bukkit.Location(Bukkit.getWorld(loc[0]),
                        Double.parseDouble(loc[1]),
                        Double.parseDouble(loc[2]),
                        Double.parseDouble(loc[3]),
                        Float.parseFloat(loc[4]),
                        Float.parseFloat(loc[5])));
            }
            if (subchannel.equals("CreateWarp")) {
                String w = in.readUTF().toLowerCase();
                String[] warp = w.split(":");
                plugin.warps.put(warp[0].toLowerCase(), new Warp(warp[0], Boolean.valueOf(warp[6]), Boolean.valueOf(warp[7]), new Location(warp[1], warp[2], Double.parseDouble(warp[3]), Double.parseDouble(warp[4]), Double.parseDouble(warp[5]))));
            }
            if (subchannel.equals("WarpCooldown")) {
                Player p = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
                if (p == null) {
                    return;
                }
            }
            if (subchannel.equals("DelWarp")) {
                plugin.warps.remove(in.readUTF());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
