package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.Debugger;
import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.Warp;
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
        Configuration config = new Configuration();
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
                            new Debugger().sendMessage("[MaSuite] [Warps] Sent delete request");
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
