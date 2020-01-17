package fi.matiaspaavilainen.masuitewarps.core.services;

import fi.matiaspaavilainen.masuitecore.core.channels.BungeePluginChannel;
import fi.matiaspaavilainen.masuitecore.core.utils.HibernateUtil;
import fi.matiaspaavilainen.masuitewarps.bungee.MaSuiteWarps;
import fi.matiaspaavilainen.masuitewarps.core.models.Warp;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WarpService {

    private EntityManager entityManager = HibernateUtil.addClasses(Warp.class).getEntityManager();
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
                "WarpPlayer",
                player.getUniqueId().toString(),
                warp.getLocation().getWorld() + ":" + warp.getLocation().getX() + ":" + warp.getLocation().getY() + ":" + warp.getLocation().getZ() + ":" + warp.getLocation().getYaw() + ":" + warp.getLocation().getPitch()
        );
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
    public boolean removeWarp(Warp warp) {
        entityManager.getTransaction().begin();
        entityManager.remove(warp);
        entityManager.getTransaction().commit();
        warps.remove(warp.getName());
        return true;
    }

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
}
