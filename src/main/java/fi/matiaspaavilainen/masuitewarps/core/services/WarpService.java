package fi.matiaspaavilainen.masuitewarps.core.services;

import fi.matiaspaavilainen.masuitecore.core.channels.BungeePluginChannel;
import fi.matiaspaavilainen.masuitewarps.bungee.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.core.HibernateUtil;
import fi.matiaspaavilainen.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class WarpService {

    private EntityManager entityManager = HibernateUtil.getEntityManager();
    public HashMap<String, Warp> warps = new HashMap<>();

    private MaSuiteWarps plugin;

    public WarpService(MaSuiteWarps plugin) {
        this.plugin = plugin;
    }

    public void teleportToWarp(ProxiedPlayer player, Warp warp) {
        this.teleport(player, warp);
    }

    private void teleport(ProxiedPlayer player, Warp warp) {
        BungeePluginChannel bsc = new BungeePluginChannel(plugin, plugin.getProxy().getServerInfo(warp.getLocation().getServer()),
                new Object[]{
                        "WarpPlayer",
                        player.getUniqueId().toString(),
                        warp.getLocation().getWorld() + ":" + warp.getLocation().getX() + ":" + warp.getLocation().getY() + ":" + warp.getLocation().getZ() + ":" + warp.getLocation().getYaw() + ":" + warp.getLocation().getPitch()
                });
        if (!player.getServer().getInfo().getName().equals(warp.getLocation().getServer())) {
            player.connect(ProxyServer.getInstance().getServerInfo(warp.getLocation().getServer()));
            ProxyServer.getInstance().getScheduler().schedule(plugin, bsc::send, plugin.warpDelay, TimeUnit.MILLISECONDS);
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
     * Creates a warp
     *
     * @param warp warp to create
     * @return returns created warp
     */
    public Warp createWarp(Warp warp) {
        entityManager.getTransaction().begin();
        entityManager.persist(warp);
        entityManager.getTransaction().commit();
        warps.put(warp.getName(), warp);

        return warp;
    }

    /**
     * Updates the warp
     *
     * @param warp warp to update
     * @return returns updated warp
     */
    public Warp updateWarp(Warp warp) {
        entityManager.getTransaction().begin();
        entityManager.merge(warp);
        entityManager.getTransaction().commit();
        warps.put(warp.getName(), warp);

        return warp;
    }

    /**
     * Removes the warp
     *
     * @param warp warp to remove
     */
    public void removeWarp(Warp warp) {
        entityManager.getTransaction().begin();
        entityManager.remove(warp);
        entityManager.getTransaction().commit();
        warps.remove(warp.getName());
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
}
