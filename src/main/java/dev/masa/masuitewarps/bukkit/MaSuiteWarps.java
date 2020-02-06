package dev.masa.masuitewarps.bukkit;

import dev.masa.masuitecore.acf.PaperCommandManager;
import dev.masa.masuitecore.bukkit.MaSuiteCore;
import dev.masa.masuitecore.bukkit.chat.Formator;
import dev.masa.masuitecore.core.Updator;
import dev.masa.masuitecore.core.channels.BukkitPluginChannel;
import dev.masa.masuitecore.core.configuration.BukkitConfiguration;
import dev.masa.masuitecore.core.utils.CommandManagerUtil;
import dev.masa.masuitewarps.bukkit.commands.WarpCommand;
import dev.masa.masuitewarps.core.models.Warp;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class MaSuiteWarps extends JavaPlugin implements Listener {

    public HashMap<String, Warp> warps = new HashMap<>();

    public BukkitConfiguration config = new BukkitConfiguration();
    public Formator formator = new Formator();

    public String warpNotFound = "";
    public String noPermission = "";
    public String teleportationStarted = "";
    public String teleportationCancelled = "";

    public String inCooldown = "";
    public int cooldownTime = 0;
    public int warmupTime = 0;

    private boolean requestedPerServerWarps = false;
    public boolean perServerWarps = false;


    @Override
    public void onEnable() {

        // Create configs
        config.create(this, "warps", "config.yml");
        config.create(this, "warps", "messages.yml");

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new WarpCommand(this));
        manager.getCommandCompletions().registerCompletion("warps", c -> {
            List<String> warpNames = new ArrayList<>();
            for (Warp warp : warps.values()) {
                if(perServerWarps && (!c.getPlayer().hasPermission("masuitewarps.warp.to." + warp.getName()) || !(c.getPlayer().hasPermission("masuitewarps.warp.to.*")))){
                    continue;
                }
                warpNames.add(warp.getName());
            }
            return warpNames;
        });

        CommandManagerUtil.registerMaSuitePlayerCommandCompletion(manager);
        CommandManagerUtil.registerCooldownCondition(manager);

        // Register listeners
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new WarpMessageListener(this));

        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new Sign(this), this);

        new Updator(getDescription().getVersion(), getDescription().getName(), "60454").checkUpdates();

        requestWarps();

        warpNotFound = config.load("warps", "messages.yml").getString("warp-not-found");
        noPermission = config.load(null, "messages.yml").getString("no-permission");
        teleportationStarted = config.load("warps", "messages.yml").getString("teleportation-started");
        teleportationCancelled = config.load("warps", "messages.yml").getString("teleportation-cancelled");

        cooldownTime = getConfig().getInt("cooldown");
        inCooldown = config.load("warps", "config.yml").getString("cooldown");
        warmupTime = config.load("warps", "config.yml").getInt("warmup");

        MaSuiteCore.cooldownService.addCooldownLength("warps", config.load("warps", "config.yml").getInt("cooldown"));
    }

    // TODO: Add proper warmup
    /*@EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (warmupTime > 0) {
            if (warmups.contains(e.getPlayer().getUniqueId())) {

                // Not move
                if (e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockY() == e.getFrom().getBlockY() && e.getTo().getBlockZ() == e.getFrom().getBlockZ())
                    return;
                formator.sendMessage(e.getPlayer(), teleportationCancelled);
                warmups.remove(e.getPlayer().getUniqueId());
            }
        }
    }*/

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (warps.isEmpty()) {
            getServer().getScheduler().runTaskLaterAsynchronously(this, () -> new BukkitPluginChannel(this, e.getPlayer(), "RequestWarps").send(), 100);
        }
        if (!requestedPerServerWarps) {
            getServer().getScheduler().runTaskLaterAsynchronously(this, () -> new BukkitPluginChannel(this, e.getPlayer(), "CheckPerWarpFlag", e.getPlayer().getName()).send(), 100);
            requestedPerServerWarps = true;
        }
    }

    private void requestWarps() {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("RequestWarps");
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> getServer().sendPluginMessage(this, "BungeeCord", b.toByteArray()), 0, 3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
