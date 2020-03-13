package dev.masa.masuitewarps.bukkit;

import dev.masa.masuitecore.acf.PaperCommandManager;
import dev.masa.masuitecore.bukkit.chat.Formator;
import dev.masa.masuitecore.core.Updator;
import dev.masa.masuitecore.core.api.MaSuiteCoreBukkitAPI;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MaSuiteWarps extends JavaPlugin implements Listener {

    public MaSuiteCoreBukkitAPI api = new MaSuiteCoreBukkitAPI();

    public HashMap<String, Warp> warps = new HashMap<>();

    public BukkitConfiguration config = new BukkitConfiguration();
    public Formator formator = new Formator();

    private boolean requestedPerServerWarps = false;
    public boolean perServerWarps = false;


    @Override
    public void onEnable() {
        // Create configs
        config.create(this, "warps", "config.yml");

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

        api.getCooldownService().addCooldownLength("warps", config.load("warps", "config.yml").getInt("cooldown"));
        api.getWarmupService().addWarmupTime("warps", config.load("warps", "config.yml").getInt("warmup"));
    }

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

}
