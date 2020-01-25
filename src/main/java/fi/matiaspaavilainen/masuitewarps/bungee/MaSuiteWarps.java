package fi.matiaspaavilainen.masuitewarps.bungee;

import fi.matiaspaavilainen.masuitecore.bungee.Utils;
import fi.matiaspaavilainen.masuitecore.bungee.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.Updator;
import fi.matiaspaavilainen.masuitecore.core.channels.BungeePluginChannel;
import fi.matiaspaavilainen.masuitecore.core.configuration.BungeeConfiguration;
import fi.matiaspaavilainen.masuitecore.core.objects.Location;
import fi.matiaspaavilainen.masuitewarps.bungee.controllers.DeleteController;
import fi.matiaspaavilainen.masuitewarps.bungee.controllers.ListController;
import fi.matiaspaavilainen.masuitewarps.bungee.controllers.SetController;
import fi.matiaspaavilainen.masuitewarps.bungee.controllers.TeleportController;
import fi.matiaspaavilainen.masuitewarps.core.services.WarpService;
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

    public WarpService warpService;

    public Utils utils = new Utils();
    public BungeeConfiguration config = new BungeeConfiguration();
    public Formator formator = new Formator();

    public boolean perWarpPermission = false;

    public String warpNotFound = "";
    public String noPermission = "";
    public String warpInOtherServer = "";
    public String teleported = "";

    public String listHeaderGlobal = "";
    public String listHeaderServer = "";
    public String listHeaderHidden = "";

    public String listWarpName = "";
    public String listHoverText = "";
    public String listWarpSplitter = "";

    public String warpCreated = "";
    public String warpUpdated = "";
    public String warpDeleted = "";

    public int warpDelay = 500;

    @Override
    public void onEnable() {
        // Configuration
        BungeeConfiguration config = new BungeeConfiguration();
        config.create(this, "warps", "messages.yml");
        config.create(this, "warps", "settings.yml");
        config.addDefault("warps/settings.yml", "warp-delay", 750);
        getProxy().getPluginManager().registerListener(this, this);

        warpService = new WarpService(this);


        warpService.initializeWarps();

        // Send list of warp
        updateWarps();

        // Updator
        new Updator(getDescription().getVersion(), getDescription().getName(), "60454").checkUpdates();

        perWarpPermission = config.load("warps", "settings.yml").getBoolean("enable-per-warp-permission");
        warpNotFound = config.load("warps", "messages.yml").getString("warp-not-found");
        noPermission = config.load("warps", "messages.yml").getString("no-permission");
        warpInOtherServer = config.load("warps", "messages.yml").getString("warp-in-other-server");
        teleported = config.load("warps", "messages.yml").getString("teleported");

        listHeaderGlobal = config.load("warps", "messages.yml").getString("warp.global");
        listHeaderServer = config.load("warps", "messages.yml").getString("warp.server");
        listHeaderHidden = config.load("warps", "messages.yml").getString("warp.hidden");

        listWarpName = config.load("warps", "messages.yml").getString("warp.name");
        listHoverText = config.load("warps", "messages.yml").getString("warp-hover-text");
        listWarpSplitter = config.load("warps", "messages.yml").getString("warp.split");

        warpCreated = config.load("warps", "messages.yml").getString("warp-created");
        warpUpdated = config.load("warps", "messages.yml").getString("warp-updated");
        warpDeleted = config.load("warps", "messages.yml").getString("warp-deleted");

        warpDelay = config.load("warps", "settings.yml").getInt("warp-delay");
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) throws IOException {
        if (!e.getTag().equals("BungeeCord")) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
        String subchannel = in.readUTF();
        TeleportController teleportController = new TeleportController(this);
        if (subchannel.equals("ListWarps")) {
            String types = in.readUTF();
            ProxiedPlayer p = getProxy().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            ListController list = new ListController(this);
            list.listWarp(p, types);
        }
        // TODO: FIX Warp sign
        /*if (subchannel.equals("WarpSign")) {
            String permissions = in.readUTF();
            ProxiedPlayer p = getProxy().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            String warp = in.readUTF();

            teleportController.teleportSign(p, warp, "sign", permissions, in.readBoolean());
            sendCooldown(p);
        }*/
        if (subchannel.equals("WarpCommand")) {
            teleportController.teleport(getProxy().getPlayer(in.readUTF()), in.readUTF(), in.readBoolean());
        }
        if (subchannel.equals("SetWarp")) {
            int i = in.readInt();
            ProxiedPlayer p = getProxy().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            String name = in.readUTF();
            Location location = new Location().deserialize(in.readUTF());
            SetController set = new SetController(this);
            if (i == 3) {
                set.setWarp(p, name, location, in.readUTF());
                updateWarps();
            } else if (i == 2) {
                set.setWarp(p, name, location);
                updateWarps();
            }
        }
        if (subchannel.equals("DelWarp")) {
            ProxiedPlayer p = getProxy().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            DeleteController delete = new DeleteController(this);
            String warpName = in.readUTF();
            delete.deleteWarp(p, warpName);
            updateWarps();
        }
        if (subchannel.equals("RequestWarps")) {
            updateWarps();
        }
    }

    private void updateWarps() {
        this.warpService.getAllWarps().forEach(warp -> {
                    for (Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet()) {
                        ServerInfo serverInfo = entry.getValue();
                        serverInfo.ping((result, error) -> {
                            if (error == null) {
                                new BungeePluginChannel(this, serverInfo, "CreateWarp", warp.serialize()).send();
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
