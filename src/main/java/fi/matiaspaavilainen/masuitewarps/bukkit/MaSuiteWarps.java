package fi.matiaspaavilainen.masuitewarps.bukkit;

import fi.matiaspaavilainen.masuitecore.bukkit.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.channels.BukkitPluginChannel;
import fi.matiaspaavilainen.masuitecore.core.configuration.BukkitConfiguration;
import fi.matiaspaavilainen.masuitewarps.bukkit.commands.DeleteCommand;
import fi.matiaspaavilainen.masuitewarps.bukkit.commands.ListCommand;
import fi.matiaspaavilainen.masuitewarps.bukkit.commands.SetCommand;
import fi.matiaspaavilainen.masuitewarps.bukkit.commands.TeleportCommand;
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
    private Formator formator = new Formator();

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


        registerCommands();
        requestWarps();
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
        if (getConfig().getInt("warmup") > 0) {
            if (warmups.contains(e.getPlayer().getUniqueId())) {
                if (e.getFrom() != e.getTo()) {
                    formator.sendMessage(e.getPlayer(), config.load("warps", "messages.yml").getString("teleportation-cancelled"));
                    warmups.remove(e.getPlayer().getUniqueId());
                }
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
            formator.sendMessage(cs, config.load(null, "messages.yml").getString("on-active-command"));
            return true;
        }

        plugin.in_command.add(cs);
        return false;
    }
}
