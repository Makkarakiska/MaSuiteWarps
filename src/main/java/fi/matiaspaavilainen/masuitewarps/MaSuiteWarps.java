package fi.matiaspaavilainen.masuitewarps;

import fi.matiaspaavilainen.masuitecore.MaSuiteCore;
import fi.matiaspaavilainen.masuitecore.Updator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.commands.Delete;
import fi.matiaspaavilainen.masuitewarps.commands.List;
import fi.matiaspaavilainen.masuitewarps.commands.Set;
import fi.matiaspaavilainen.masuitewarps.commands.Teleport;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MaSuiteWarps extends Plugin implements Listener {

    @Override
    public void onEnable() {
        super.onEnable();
        Configuration config = new Configuration();
        config.create(this, "warps","syntax.yml");
        config.create(this, "warps","messages.yml");
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new Set(this));
        getProxy().getPluginManager().registerCommand(this, new Teleport(this));
        getProxy().getPluginManager().registerCommand(this, new List());
        getProxy().getPluginManager().registerCommand(this, new Delete());
        MaSuiteCore.db.createTable("warps",
                "(id INT(10) unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) UNIQUE NOT NULL, server VARCHAR(100) NOT NULL, world VARCHAR(100) NOT NULL, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, hidden TINYINT(1), global TINYINT(1)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        new Updator().checkVersion(this.getDescription(), "60454");
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) throws IOException {
        if(!e.getTag().equals("BungeeCord")){
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
        String subchannel = in.readUTF();
        if(subchannel.equals("WarpSign")){
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(in.readUTF());
            if(p == null){
                return;
            }
            ProxyServer.getInstance().getPluginManager().dispatchCommand(p, "warp " + in.readUTF());
        }
    }
}
