package dev.masa.masuitewarps.core.services;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import dev.masa.masuitecore.core.channels.BungeePluginChannel;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WarpService {

    @Getter
    private HashMap<String, Warp> warps = new HashMap<>();
    private Dao<Warp, Integer> warpDao;
    private MaSuiteWarps plugin;

    @SneakyThrows
    public WarpService(MaSuiteWarps plugin) {
        this.plugin = plugin;
        this.warpDao = DaoManager.createDao(plugin.getApi().getDatabaseService().getConnection(), Warp.class);
        TableUtils.createTableIfNotExists(plugin.getApi().getDatabaseService().getConnection(), Warp.class);
    }

    /**
     * Teleports {@link ProxiedPlayer} to {@link Warp}
     *
     * @param player player to teleport
     * @param warp   target warp
     * @param silent do we send a message to the player or not
     */
    public void teleportToWarp(ProxiedPlayer player, Warp warp, boolean silent) {
        this.teleport(player, warp, silent);
    }

    /**
     * Teleports {@link ProxiedPlayer} to {@link Warp}
     *
     * @param player player to teleport
     * @param warp   target warp
     * @param silent do we send a message to the player or not
     */
    private void teleport(ProxiedPlayer player, Warp warp, boolean silent) {
        new BungeePluginChannel(plugin,
                player.getServer().getInfo(),
                "MaSuiteTeleports",
                "GetLocation",
                player.getName(),
                player.getServer().getInfo().getName()).send();

        BungeePluginChannel bsc = new BungeePluginChannel(plugin, plugin.getProxy().getServerInfo(warp.getLocation().getServer()),
                "WarpPlayer",
                player.getUniqueId().toString(),
                warp.getLocation().serialize()
        );
        if (!player.getServer().getInfo().getName().equals(warp.getLocation().getServer())) {
            player.connect(plugin.getProxy().getServerInfo(warp.getLocation().getServer()));
            plugin.getProxy().getScheduler().schedule(plugin, () -> {
                bsc.send();
                plugin.utils.applyCooldown(plugin, player.getUniqueId(), "warps");
            }, plugin.config.load(null, "config.yml").getInt("teleportation-delay"), TimeUnit.MILLISECONDS);
        } else {
            bsc.send();
        }
        if (!silent) {
            plugin.formator.sendMessage(player, plugin.teleported.replace("%warp%", warp.getName()));
        }

    }

    /**
     * Gets warp by name
     *
     * @param name name of the warp
     * @return returns warp or null
     */
    public Warp getWarp(String name) {
        return this.loadWarp(name);
    }

    /**
     * Get all warps
     *
     * @return returns a list of warps from cache
     */
    public List<Warp> getAllWarps() {
        return new ArrayList<>(warps.values());
    }

    /**
     * Creates a warp
     *
     * @param warp warp to create
     * @return returns created warp
     */
    public Warp createWarp(Warp warp) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                warpDao.create(warp);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        warps.put(warp.getName(), warp);
        this.sendWarpToServers(warp);
        return warp;
    }

    /**
     * Updates the warp
     *
     * @param warp warp to update
     * @return returns updated warp
     */
    public Warp updateWarp(Warp warp) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                warpDao.update(warp);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        warps.put(warp.getName(), warp);

        this.sendWarpToServers(warp);
        return warp;
    }

    /**
     * Removes the warp
     *
     * @param warp warp to remove
     */
    public boolean removeWarp(Warp warp) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                warpDao.delete(warp);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
                ServerInfo serverInfo = entry.getValue();
                serverInfo.ping((result, error) -> {
                    if (error == null) {
                        new BungeePluginChannel(plugin, serverInfo, "DeleteWarp", warp.getName()).send();
                    }
                });
            }
        });

        warps.remove(warp.getName());
        return true;
    }

    /**
     * Initialize warps for use
     */
    @SneakyThrows
    public void initializeWarps() {
        warpDao.queryForAll().forEach(warp -> warps.put(warp.getName(), warp));
    }

    /**
     * Loads warp from database or cache by name
     *
     * @param name name of the warp
     * @return returns loaded warp or null
     */
    @SneakyThrows
    private Warp loadWarp(String name) {
        if (warps.containsKey(name)) {
            return warps.get(name);
        }

        Warp warp = warpDao.queryForEq("name", name).stream().findFirst().orElse(null);
        if (warp != null) {
            warps.put(name, warp);
        }

        return warp;
    }

    /**
     * Sends a list of warp to every server
     */
    public void sendAllWarpsToServers() {
        this.getAllWarps().forEach(this::sendWarpToServers);
    }

    /**
     * Send a {@link Warp} to every server
     *
     * @param warp warp to send
     */
    public void sendWarpToServers(Warp warp) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
                ServerInfo serverInfo = entry.getValue();
                serverInfo.ping((result, error) -> {
                    if (error == null) {
                        new BungeePluginChannel(plugin, serverInfo, "CreateWarp", warp.serialize()).send();
                    }
                });
            }
        });
    }
}
