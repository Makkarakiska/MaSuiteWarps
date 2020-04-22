package dev.masa.masuitewarps.bungee;

import dev.masa.masuitecore.bungee.Utils;
import dev.masa.masuitecore.bungee.chat.Formator;
import dev.masa.masuitecore.core.Updator;
import dev.masa.masuitecore.core.api.MaSuiteCoreAPI;
import dev.masa.masuitecore.core.channels.BungeePluginChannel;
import dev.masa.masuitecore.core.configuration.BungeeConfiguration;
import dev.masa.masuitecore.core.objects.Location;
import dev.masa.masuitewarps.bungee.controllers.DeleteController;
import dev.masa.masuitewarps.bungee.controllers.ListController;
import dev.masa.masuitewarps.bungee.controllers.SetController;
import dev.masa.masuitewarps.bungee.controllers.TeleportController;
import dev.masa.masuitewarps.core.services.WarpService;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

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

    private TeleportController teleportController = new TeleportController(this);
    private SetController set = new SetController(this);
    private DeleteController delete = new DeleteController(this);
    private ListController list = new ListController(this);

    @Getter
    private MaSuiteCoreAPI api = new MaSuiteCoreAPI();

    @Override
    public void onEnable() {
        // Configuration
        config.create(this, "warps", "messages.yml");
        config.create(this, "warps", "settings.yml");
        config.addDefault("warps/settings.yml", "warp-delay", 750);
        getProxy().getPluginManager().registerListener(this, this);

        warpService = new WarpService(this);


        warpService.initializeWarps();

        // Send list of warp
        this.warpService.sendAllWarpsToServers();

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

        if (subchannel.equals("ListWarps")) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            if (player == null) {
                return;
            }
            list.listWarp(player, in.readBoolean(), in.readBoolean(), in.readBoolean());
        }

        if (subchannel.equals("Warp")) {
            teleportController.teleport(getProxy().getPlayer(in.readUTF()), in.readUTF(), in.readBoolean(), in.readBoolean(), in.readBoolean(), in.readBoolean());
        }

        if (subchannel.equals("CheckPerWarpFlag")) {
            ProxiedPlayer player = getProxy().getPlayer(in.readUTF());
            new BungeePluginChannel(this, player.getServer().getInfo(), "SetPerWarpFlag", perWarpPermission).send();
        }

        if (subchannel.equals("SetWarp")) {
            ProxiedPlayer p = getProxy().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            String name = in.readUTF();
            Location location = new Location().deserialize(in.readUTF());

            set.setWarp(p, name, location, in.readBoolean(), in.readBoolean());
        }
        if (subchannel.equals("DelWarp")) {
            ProxiedPlayer p = getProxy().getPlayer(in.readUTF());
            if (p == null) {
                return;
            }
            delete.deleteWarp(p, in.readUTF());
        }
        if (subchannel.equals("RequestWarps")) {
            this.warpService.sendAllWarpsToServers();
        }
    }
}
