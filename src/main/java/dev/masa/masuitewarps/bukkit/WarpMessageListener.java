package dev.masa.masuitewarps.bukkit;

import dev.masa.masuitecore.core.adapters.BukkitAdapter;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitewarps.core.models.Warp;
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
                Location loc = new Location().deserialize(in.readUTF());

                org.bukkit.Location bukkitLocation = BukkitAdapter.adapt(loc);
                if (bukkitLocation.getWorld() == null) {
                    System.out.println("[MaSuite] [Warps] [World=" + loc.getWorld() + "] World  could not be found!");
                    return;
                }
                p.teleport(bukkitLocation);
            }
            if (subchannel.equals("CreateWarp")) {
                Warp warp = new Warp();
                warp = warp.deserialize(in.readUTF().toLowerCase());
                plugin.warps.put(warp.getName(), warp);
            }
            if (subchannel.equals("WarpCooldown")) {
                Player p = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
                if (p == null) {
                    return;
                }
            }
            if (subchannel.equals("SetPerWarpFlag")) {
                plugin.perServerWarps = in.readBoolean();
            }
            if (subchannel.equals("DelWarp")) {
                plugin.warps.remove(in.readUTF());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
