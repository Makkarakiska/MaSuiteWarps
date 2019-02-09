package fi.matiaspaavilainen.masuitewarps.bungee;

import fi.matiaspaavilainen.masuitecore.core.Updator;
import fi.matiaspaavilainen.masuitecore.core.channels.BungeePluginChannel;
import fi.matiaspaavilainen.masuitecore.core.configuration.BungeeConfiguration;
import fi.matiaspaavilainen.masuitecore.core.database.ConnectionManager;
import fi.matiaspaavilainen.masuitecore.core.objects.Location;
import fi.matiaspaavilainen.masuitewarps.bungee.commands.Delete;
import fi.matiaspaavilainen.masuitewarps.bungee.commands.List;
import fi.matiaspaavilainen.masuitewarps.bungee.commands.Set;
import fi.matiaspaavilainen.masuitewarps.bungee.commands.Teleport;
import fi.matiaspaavilainen.masuitewarps.core.objects.Warp;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class MaSuiteWarps extends Plugin implements Listener {

    @Override
    public void onEnable() {
        // Configuration
        BungeeConfiguration config = new BungeeConfiguration();
        config.create(this, "warps", "messages.yml");
        config.create(this, "warps", "settings.yml");
        getProxy().getPluginManager().registerListener(this, this);

        // Database

        ConnectionManager.db.createTable("warps",
                "(id INT(10) unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) UNIQUE NOT NULL, server VARCHAR(100) NOT NULL, world VARCHAR(100) NOT NULL, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, hidden TINYINT(1), global TINYINT(1)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        // Send list of warp
        updateWarps();

        // Updator
        new Updator(new String[]{getDescription().getVersion(), getDescription().getName(), "60454"}).checkUpdates();
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) throws IOException {
        if (!e.getTag().equals("BungeeCord")) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
        String subchannel = in.readUTF();
        if (subchannel.equals("ListWarps")) {
            String types = in.readUTF();
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            List list = new List();
            list.listWarp(p, types);
        }
        if (subchannel.equals("WarpSign")) {
            String permissions = in.readUTF();
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            Warp warp = new Warp();
            warp = warp.find(in.readUTF());
            Teleport teleport = new Teleport(this);
            teleport.warp(p, warp, "sign", permissions, in.readBoolean());
            sendCooldown(p);
        }
        if (subchannel.equals("WarpCommand")) {
            String permissions = in.readUTF();
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }

            Warp warp = new Warp();
            warp = warp.find(in.readUTF());
            Teleport teleport = new Teleport(this);
            teleport.warp(p, warp, "command", permissions, in.readBoolean());
            sendCooldown(p);
        }
        if (subchannel.equals("WarpPlayerCommand")) {
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            String s = in.readUTF();
            Warp warp = new Warp();
            warp = warp.find(in.readUTF());
            Teleport teleport = new Teleport(this);
            teleport.warp(p, s, warp, "command");
        }
        if (subchannel.equals("SetWarp")) {
            int i = in.readInt();
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            String name = in.readUTF();
            String[] location = in.readUTF().split(":");
            Set set = new Set(this);
            if (i == 3) {
                set.setWarp(p, name, new Location(location[0], Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3]), Float.parseFloat(location[4]), Float.parseFloat(location[5])), in.readUTF());
                updateWarps();
            } else if (i == 2) {
                set.setWarp(p, name,
                        new Location(location[0], Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3]), Float.parseFloat(location[4]), Float.parseFloat(location[5])));
                updateWarps();
            }
        }
        if (subchannel.equals("DelWarp")) {
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            Delete delete = new Delete(this);
            delete.deleteWarp(p, in.readUTF());
            updateWarps();
        }
        if (subchannel.equals("RequestWarps")) {
            updateWarps();
        }
    }

    private void updateWarps() {
        Warp w = new Warp();
        w.all().forEach(warp -> {
            StringJoiner info = new StringJoiner(":");
            Location loc = warp.getLocation();
            info.add(warp.getName())
                    .add(warp.getServer())
                    .add(loc.getWorld())
                    .add(loc.getX().toString())
                    .add(loc.getY().toString())
                    .add(loc.getZ().toString())
                    .add(warp.isGlobal().toString())
                    .add(warp.isHidden().toString());
                    for (Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet()) {
                        ServerInfo serverInfo = entry.getValue();
                        serverInfo.ping((result, error) -> {
                            if (error == null) {
                                new BungeePluginChannel(this, serverInfo, new Object[]{"CreateWarp", info.toString()}).send();
                            }
                        });
                    }

                }
        );
    }

    private void sendCooldown(ProxiedPlayer p) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("WarpCooldown");
            out.writeUTF(p.getUniqueId().toString());
            out.writeLong(System.currentTimeMillis());
            getProxy().getScheduler().schedule(this, () -> p.getServer().sendData("BungeeCord", b.toByteArray()), 500, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
