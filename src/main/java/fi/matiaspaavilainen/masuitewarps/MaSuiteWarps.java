package fi.matiaspaavilainen.masuitewarps;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fi.matiaspaavilainen.masuitecore.Updator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitecore.database.Database;
import fi.matiaspaavilainen.masuitecore.managers.Location;
import fi.matiaspaavilainen.masuitewarps.commands.Delete;
import fi.matiaspaavilainen.masuitewarps.commands.List;
import fi.matiaspaavilainen.masuitewarps.commands.Set;
import fi.matiaspaavilainen.masuitewarps.commands.Teleport;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

public class MaSuiteWarps extends Plugin implements Listener {

    static Database db = new Database();

    @Override
    public void onEnable() {
        super.onEnable();
        Configuration config = new Configuration();
        config.create(this, "warps", "syntax.yml");
        config.create(this, "warps", "messages.yml");
        config.create(this, "warps", "settings.yml");
        getProxy().getPluginManager().registerListener(this, this);
        db.connect();
        db.createTable("warps",
                "(id INT(10) unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) UNIQUE NOT NULL, server VARCHAR(100) NOT NULL, world VARCHAR(100) NOT NULL, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, hidden TINYINT(1), global TINYINT(1)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        new Updator().checkVersion(this.getDescription(), "60454");
        updateWarps();
    }

    public void onDisable() {
        db.hikari.close();
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) throws IOException {
        if (!e.getTag().equals("BungeeCord")) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
        String subchannel = in.readUTF();
        if (subchannel.equals("ListWarps")) {
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            List list = new List();
            list.listWarp(p);
        }
        if (subchannel.equals("WarpSign")) {

            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            Warp warp = new Warp();
            warp = warp.find(in.readUTF());
            Teleport teleport = new Teleport();
            teleport.warp(p, warp, "sign");
            sendCooldown(p);
        }
        if (subchannel.equals("WarpCommand")) {
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            Warp warp = new Warp();
            warp = warp.find(in.readUTF());
            Teleport teleport = new Teleport();
            teleport.warp(p, warp, "command");
            sendCooldown(p);
        }
        if (subchannel.equals("WarpPlayerCommand")) {
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            ProxiedPlayer sender = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null || sender == null) {
                return;
            }
            Warp warp = new Warp();
            warp = warp.find(in.readUTF());
            Teleport teleport = new Teleport();
            teleport.warp(p, sender, warp, "command");
        }
        if (subchannel.equals("SetWarp")) {
            int i = in.readInt();
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            String name = in.readUTF();
            String[] location = in.readUTF().split(":");
            Set set = new Set();
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
            Delete delete = new Delete();
            delete.deleteWarp(p, in.readUTF());
            updateWarps();
        }
        if(subchannel.equals("RequestWarps")){
            updateWarps();
        }
    }

    private void updateWarps() {
        Warp w = new Warp();
        StringBuilder warps = new StringBuilder();
        w.all().forEach(warp -> warps.append(warp.getName()).append(":"));
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ListWarpsForPlayers");
        out.writeUTF(warps.toString());
        for (Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet()) {
            ServerInfo serverInfo = entry.getValue();
            serverInfo.ping((result, error) -> {
                if (error != null) {
                } else {
                    serverInfo.sendData("BungeeCord", out.toByteArray());
                }
            });
        }
    }

    private void sendCooldown(ProxiedPlayer p) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("WarpCooldown");
        out.writeUTF(String.valueOf(p.getUniqueId()));
        try {
            Thread.sleep(200);
            out.writeLong(System.currentTimeMillis());
            p.getServer().sendData("BungeeCord", out.toByteArray());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
