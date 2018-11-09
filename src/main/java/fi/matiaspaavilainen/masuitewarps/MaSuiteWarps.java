package fi.matiaspaavilainen.masuitewarps;

import fi.matiaspaavilainen.masuitecore.Debugger;
import fi.matiaspaavilainen.masuitecore.Updator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitecore.managers.Location;
import fi.matiaspaavilainen.masuitewarps.commands.Delete;
import fi.matiaspaavilainen.masuitewarps.commands.List;
import fi.matiaspaavilainen.masuitewarps.commands.Set;
import fi.matiaspaavilainen.masuitewarps.commands.Teleport;
import fi.matiaspaavilainen.masuitewarps.database.Database;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MaSuiteWarps extends Plugin implements Listener {

    static Database db = new Database();
    private Debugger debugger = new Debugger();
    @Override
    public void onEnable() {
        super.onEnable();
        Configuration config = new Configuration();
        config.create(this, "warps", "messages.yml");
        config.create(this, "warps", "settings.yml");
        getProxy().getPluginManager().registerListener(this, this);
        db.connect();
        db.createTable("warps",
                "(id INT(10) unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) UNIQUE NOT NULL, server VARCHAR(100) NOT NULL, world VARCHAR(100) NOT NULL, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, hidden TINYINT(1), global TINYINT(1)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        new Updator().checkVersion(getDescription(), "60454");
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
            String types = in.readUTF();
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            List list = new List();
            list.listWarp(p, types);
            debugger.sendMessage("[MaSuite] [Warps] Listed warps for " + p.getName());
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
            teleport.warp(p, warp, "sign", permissions);
            debugger.sendMessage("[MaSuite] [Warps] [WarpSign] Warping " + p.getName());
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
            teleport.warp(p, warp, "command", permissions);
            debugger.sendMessage("[MaSuite] [Warps] [WarpCommand] Warping " + p.getName());
            sendCooldown(p);
        }
        if (subchannel.equals("WarpPlayerCommand")) {
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            String s = in.readUTF();
            Warp warp = new Warp();
            warp = warp.find(in.readUTF());
            Teleport teleport = new Teleport(this);
            teleport.warp(p, s, warp, "command");
            debugger.sendMessage("[MaSuite] [Warps] [WarpPlayerCommand] Warping " + p.getName());
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
            debugger.sendMessage("[MaSuite] [Warps] Set warp");
        }
        if (subchannel.equals("DelWarp")) {
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            Delete delete = new Delete();
            delete.deleteWarp(p, in.readUTF());
            updateWarps();
            debugger.sendMessage("[MaSuite] [Warps] Deleted warp");
        }
        if (subchannel.equals("RequestWarps")) {
            updateWarps();
        }
    }

    private void updateWarps() {
        Warp w = new Warp();
        StringBuilder warps = new StringBuilder();
        w.all().forEach(warp -> warps.append(warp.getName()).append(":"));
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("ListWarpsForPlayers");
            out.writeUTF(warps.toString());
            for (Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet()) {
                ServerInfo serverInfo = entry.getValue();
                serverInfo.ping((result, error) -> {
                    if (error == null) {
                        serverInfo.sendData("BungeeCord", b.toByteArray());
                        debugger.sendMessage("[MaSuite] [Warps] Sent list of warp");
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCooldown(ProxiedPlayer p) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("WarpCooldown");
            out.writeUTF(p.getUniqueId().toString());
            out.writeLong(System.currentTimeMillis());
            ProxyServer.getInstance().getScheduler().schedule(this, () -> p.getServer().sendData("BungeeCord", b.toByteArray()), 500, TimeUnit.MILLISECONDS);
            debugger.sendMessage("[MaSuite] [Warps] Sent cooldown to" + p.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
