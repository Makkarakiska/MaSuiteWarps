package fi.matiaspaavilainen.masuitewarps.bukkit;

import fi.matiaspaavilainen.masuitecore.acf.PaperCommandManager;
import fi.matiaspaavilainen.masuitecore.bukkit.MaSuiteCore;
import fi.matiaspaavilainen.masuitecore.bukkit.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.Updator;
import fi.matiaspaavilainen.masuitecore.core.channels.BukkitPluginChannel;
import fi.matiaspaavilainen.masuitecore.core.configuration.BukkitConfiguration;
import fi.matiaspaavilainen.masuitecore.core.utils.CommandManagerUtil;
import fi.matiaspaavilainen.masuitewarps.bukkit.commands.WarpCommand;
import fi.matiaspaavilainen.masuitewarps.core.models.Warp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class MaSuiteWarps extends JavaPlugin implements Listener {

    public static HashSet<UUID> warmups = new HashSet<>();
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

    public String onActiveCommand = "";

    @Override
    public void onEnable() {

        // Create configs
        config.create(this, "warps", "config.yml");
        config.create(this, "warps", "messages.yml");

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new WarpCommand(this));
        manager.getCommandCompletions().registerCompletion("warps", c -> {
            List<String> warpNames = new ArrayList<>();
            for (Warp home : warps.values()) {
                warpNames.add(home.getName());
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

        onActiveCommand = config.load(null, "messages.yml").getString("on-active-command");

        MaSuiteCore.cooldownService.addCooldownLength("warps", config.load("warps", "config.yml").getInt("cooldown"));
    }

    @EventHandler
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
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (warps.isEmpty()) {
            getServer().getScheduler().runTaskLaterAsynchronously(this, () -> new BukkitPluginChannel(this, e.getPlayer(), "RequestWarps").send(), 100);
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

    public String checkPermissions(Player p) {
        StringJoiner types = new StringJoiner("");
        if (p.hasPermission("masuitewarps.list.global")) {
            types.add("GLOBAL");
        }
        if (p.hasPermission("masuitewarps.list.server")) {
            types.add("SERVER");
        }
        if (p.hasPermission("masuitewarps.list.hidden")) {
            types.add("HIDDEN");
        }
        return types.toString();
    }
}
