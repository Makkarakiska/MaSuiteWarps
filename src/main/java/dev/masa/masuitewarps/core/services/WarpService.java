package dev.masa.masuitewarps.core.services;

import dev.masa.masuitecore.core.channels.BungeePluginChannel;
import dev.masa.masuitecore.core.utils.HibernateUtil;
import dev.masa.masuitewarps.bungee.MaSuiteWarps;
import dev.masa.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WarpService {

    private EntityManager entityManager = HibernateUtil.addClasses(Warp.class).getEntityManager();
    public HashMap<String, Warp> warps = new HashMap<>();

    private MaSuiteWarps plugin;

    public WarpService(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    /**
     * Teleports {@link ProxiedPlayer} to {@link Warp}
     *
     * @param player player to teleport
     * @param warp   target warp
     */
    public void teleportToWarp(ProxiedPlayer player, Warp warp) {
        this.teleport(player, warp);
    }

    /**
     * Teleports {@link ProxiedPlayer} to {@link Warp}
     *
     * @param player player to teleport
     * @param warp   target warp
     */
    private void teleport(ProxiedPlayer player, Warp warp) {
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
            }, plugin.warpDelay, TimeUnit.MILLISECONDS);
        } else {
            bsc.send();
        }
        plugin.formator.sendMessage(player, plugin.teleported.replace("%warp%", warp.getName()));
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
            entityManager.getTransaction().begin();
            entityManager.persist(warp);
            entityManager.getTransaction().commit();
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
            entityManager.getTransaction().begin();
            entityManager.merge(warp);
            entityManager.getTransaction().commit();
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
            entityManager.getTransaction().begin();
            entityManager.remove(warp);
            entityManager.getTransaction().commit();

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
    public void initializeWarps() {
        List<Warp> warpList = entityManager.createQuery("SELECT w FROM Warp w", Warp.class).getResultList();
        warpList.forEach(warp -> warps.put(warp.getName(), warp));
    }

    /**
     * Loads warp from database or cache by name
     *
     * @param name name of the warp
     * @return returns loaded warp or null
     */
    private Warp loadWarp(String name) {
        if (warps.containsKey(name)) {
            return warps.get(name);
        }

        Warp warp = entityManager.createNamedQuery("findWarp", Warp.class).setParameter("name", name).getResultList().stream().findFirst().orElse(null);
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
