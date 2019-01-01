package fi.matiaspaavilainen.masuitewarps.bungee.commands;

import fi.matiaspaavilainen.masuitecore.bungee.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.configuration.BungeeConfiguration;
import fi.matiaspaavilainen.masuitewarps.bungee.Warp;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class Delete {

    public void deleteWarp(ProxiedPlayer p, String name) {
        Formator formator = new Formator();
        BungeeConfiguration config = new BungeeConfiguration();
        Warp warp = new Warp();
        warp = warp.find(name);
        if (warp.getServer() == null) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
            return;
        }
        if (warp.delete(name)) {
            formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-deleted"));
            try (ByteArrayOutputStream b = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream(b)) {
                out.writeUTF("DelWarp");
                out.writeUTF(name);
                for (Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServers().entrySet()) {
                    ServerInfo serverInfo = entry.getValue();
                    serverInfo.ping((result, error) -> {
                        if (error == null) {
                            serverInfo.sendData("BungeeCord", b.toByteArray());
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            formator.sendMessage(p, "&cAn error occured. Please check console for more details");
        }
    }
}
