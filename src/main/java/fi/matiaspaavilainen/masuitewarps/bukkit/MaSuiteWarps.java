package fi.matiaspaavilainen.masuitewarps.bukkit;

import fi.matiaspaavilainen.masuitecore.bukkit.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.Updator;
import fi.matiaspaavilainen.masuitecore.core.channels.BukkitPluginChannel;
import fi.matiaspaavilainen.masuitecore.core.configuration.BukkitConfiguration;
import fi.matiaspaavilainen.masuitewarps.bukkit.commands.DeleteCommand;
import fi.matiaspaavilainen.masuitewarps.bukkit.commands.ListCommand;
import fi.matiaspaavilainen.masuitewarps.bukkit.commands.SetCommand;
import fi.matiaspaavilainen.masuitewarps.bukkit.commands.TeleportCommand;
import fi.matiaspaavilainen.masuitewarps.core.objects.Warp;
import org.bukkit.command.CommandSender;
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
    public static HashMap<String, Warp> warps = new HashMap<>();
    public static HashMap<UUID, Long> cooldowns = new HashMap<>();
    public final List<CommandSender> in_command = new ArrayList<>();

    public BukkitConfiguration config = new BukkitConfiguration();
    public Formator formator = new Formator();

    public String warpNotFound = "";
    public String noPermission = "";
    public String teleportationStarted = "";
    public String teleportationCancelled = "";

    public String inCooldown = "";
    public int cooldownTime = 0;
    public int warmupTime = 0;

    public String teleportSyntax = "";
    public String setSyntax = "";
    public String deleteSyntax = "";

    public String onActiveCommand = "";

    @Override
    public void onEnable() {

        // Create configs
        config.create(this, "warps", "config.yml");
        config.create(this, "warps", "messages.yml");
        config.create(this, "warps", "syntax.yml");

        // Register listeners
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new WarpMessageListener(this));

        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new Sign(this), this);

        new Updator(new String[]{getDescription().getVersion(), getDescription().getName(), "60454"}).checkUpdates();

        registerCommands();
        requestWarps();

        warpNotFound = config.load("warps", "messages.yml").getString("warp-not-found");
        noPermission = config.load(null, "messages.yml").getString("no-permission");
        teleportationStarted = config.load("warps", "messages.yml").getString("teleportation-started");
        teleportationCancelled = config.load("warps", "messages.yml").getString("teleportation-cancelled");

        cooldownTime = getConfig().getInt("cooldown");
        inCooldown = config.load("warps", "config.yml").getString("cooldown");
        warmupTime = config.load("warps", "config.yml").getInt("warmup");

        teleportSyntax = config.load("warps", "syntax.yml").getString("warp.teleport");
        setSyntax = config.load("warps", "syntax.yml").getString("warp.set");
        deleteSyntax = config.load("warps", "syntax.yml").getString("warp.delete");

        onActiveCommand = config.load(null, "messages.yml").getString("on-active-command");
    }

    private void registerCommands() {
        getCommand("warp").setExecutor(new TeleportCommand(this));
        getCommand("warp").setTabCompleter(new TabCompleter(this));
        getCommand("setwarp").setExecutor(new SetCommand(this));
        getCommand("delwarp").setExecutor(new DeleteCommand(this));
        getCommand("delwarp").setTabCompleter(new TabCompleter(this));
        getCommand("warps").setExecutor(new ListCommand(this));
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
            getServer().getScheduler().runTaskLaterAsynchronously(this, () -> new BukkitPluginChannel(this, e.getPlayer(), new Object[]{"RequestWarps"}).send(), 100);
        }
    }

    private void requestWarps() {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("RequestWarps");
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> getServer().sendPluginMessage(this, "BungeeCord", b.toByteArray()), 0, 3000);
        } catch (
                IOException e) {
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

    public boolean checkCooldown(CommandSender cs, MaSuiteWarps plugin) {
        if (plugin.in_command.contains(cs)) {
            formator.sendMessage(cs, onActiveCommand);
            return true;
        }

        plugin.in_command.add(cs);
        return false;
    }
}
