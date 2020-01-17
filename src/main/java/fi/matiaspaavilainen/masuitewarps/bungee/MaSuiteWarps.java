package fi.matiaspaavilainen.masuitewarps.bungee;

import fi.matiaspaavilainen.masuitecore.bungee.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.Updator;
import fi.matiaspaavilainen.masuitecore.core.channels.BungeePluginChannel;
import fi.matiaspaavilainen.masuitecore.core.configuration.BungeeConfiguration;
import fi.matiaspaavilainen.masuitecore.core.objects.Location;
import fi.matiaspaavilainen.masuitewarps.bungee.commands.*;
import fi.matiaspaavilainen.masuitewarps.bungee.controllers.TeleportController;
import fi.matiaspaavilainen.masuitewarps.core.models.Warp;
import fi.matiaspaavilainen.masuitewarps.core.services.WarpService;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class MaSuiteWarps extends Plugin implements Listener {

    public WarpService warpService;

    public HashMap<String, Warp> warps = new HashMap<>();
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


        //new Warp().all().forEach(warp -> warps.put(warp.getName().toLowerCase(), warp));
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
        Teleport teleport = new Teleport(this);
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
        if (subchannel.equals("WarpSign")) {
            String permissions = in.readUTF();
            ProxiedPlayer p = getProxy().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            String warp = in.readUTF();
            if (warps.get(warp.toLowerCase()) == null) {
                formator.sendMessage(p, warpNotFound);
                return;
            }
            teleport.warp(p, warps.get(warp), "sign", permissions, in.readBoolean());
            sendCooldown(p);
        }
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
            String[] location = in.readUTF().split(":");
            SetController set = new SetController(this);
            if (i == 3) {
                warps.put(name.toLowerCase(), set.setWarp(p, name,
                        new Location(location[0], Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3]), Float.parseFloat(location[4]), Float.parseFloat(location[5])), in.readUTF()));
                set.setWarp(p, name, new Location(location[0], Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3]), Float.parseFloat(location[4]), Float.parseFloat(location[5])), in.readUTF());
                updateWarps();
            } else if (i == 2) {
                warps.put(name.toLowerCase(), set.setWarp(p, name,
                        new Location(location[0], Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3]), Float.parseFloat(location[4]), Float.parseFloat(location[5]))));
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
            warps.remove(warpName);
            updateWarps();
        }
        if (subchannel.equals("RequestWarps")) {
            updateWarps();
        }
    }

    private void updateWarps() {
        warps.forEach((name, warp) -> {
                    StringJoiner info = new StringJoiner(":");
                    Location loc = warp.getLocation();
                    info.add(warp.getName())
                            .add(warp.getLocation().getServer())
                            .add(loc.getWorld())
                            .add(loc.getX().toString())
                            .add(loc.getY().toString())
                            .add(loc.getZ().toString())
                            .add(warp.isGlobal() + "")
                            .add(warp.isHidden() + "");
                    for (Map.Entry<String, ServerInfo> entry : getProxy().getServers().entrySet()) {
                        ServerInfo serverInfo = entry.getValue();
                        serverInfo.ping((result, error) -> {
                            if (error == null) {
                                new BungeePluginChannel(this, serverInfo, "CreateWarp", info.toString()).send();
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
