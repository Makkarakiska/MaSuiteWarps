package fi.matiaspaavilainen.masuitewarps;

import fi.matiaspaavilainen.masuitecore.MaSuiteCore;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.commands.List;
import fi.matiaspaavilainen.masuitewarps.commands.Set;
import fi.matiaspaavilainen.masuitewarps.commands.Teleport;
import net.md_5.bungee.api.plugin.Plugin;

public class MaSuiteWarps extends Plugin {

    @Override
    public void onEnable() {
        super.onEnable();
        Configuration config = new Configuration();
        config.create(this, "warps","syntax.yml");
        getProxy().getPluginManager().registerCommand(this, new Set());
        getProxy().getPluginManager().registerCommand(this, new Teleport());
        getProxy().getPluginManager().registerCommand(this, new List());
        MaSuiteCore.db.createTable("warps",
                "(id INT(10) unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) UNIQUE NOT NULL, server VARCHAR(255) NOT NULL, world VARCHAR(255) NOT NULL, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, hidden TINYINT(1), global TINYINT(1));");
    }
}
