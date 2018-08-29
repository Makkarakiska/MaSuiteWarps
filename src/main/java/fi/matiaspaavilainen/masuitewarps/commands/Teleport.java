package fi.matiaspaavilainen.masuitewarps.commands;

import fi.matiaspaavilainen.masuitecore.chat.Formator;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.Warp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Teleport extends Command {
    public Teleport() {
        super("warp", "masuitewarps.warp");
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if(!(cs instanceof ProxiedPlayer)){
         return;
        }
        if(args.length == 1){
            ProxiedPlayer p = (ProxiedPlayer) cs;
            Formator formator = new Formator();
            Configuration config = new Configuration();
            Warp warp = new Warp();
            warp = warp.find(args[0]);
            if(warp.getServer() == null){
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("warp-not-found"));
            }else{
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                try {
                    if (!p.getServer().getInfo().getName().equals(warp.getServer())) {
                        p.connect(ProxyServer.getInstance().getServerInfo(warp.getName()));
                    }
                    out.writeUTF("WarpPlayer");
                    out.writeUTF(String.valueOf(p.getUniqueId()));
                    out.writeUTF(warp.getWorld());
                    out.writeDouble(warp.getX());
                    out.writeDouble(warp.getY());
                    out.writeDouble(warp.getZ());
                    out.writeFloat(warp.getYaw());
                    out.writeFloat(warp.getPitch());
                    p.getServer().sendData("BungeeCord", b.toByteArray());

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                formator.sendMessage(p, config.load("warps", "messages.yml").getString("teleported").replace("%warp%", warp.getName()));
            }

        }
    }
}
