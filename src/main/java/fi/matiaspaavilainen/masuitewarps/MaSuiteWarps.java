package fi.matiaspaavilainen.masuitewarps;

import fi.matiaspaavilainen.masuitecore.MaSuiteCore;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitewarps.commands.Delete;
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
        config.create(this, "warps","messages.yml");
        getProxy().getPluginManager().registerCommand(this, new Set(this));
        getProxy().getPluginManager().registerCommand(this, new Teleport(this));
        getProxy().getPluginManager().registerCommand(this, new List());
        getProxy().getPluginManager().registerCommand(this, new Delete());
        MaSuiteCore.db.createTable("warps",
                "(id INT(10) unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) UNIQUE NOT NULL, server VARCHAR(100) NOT NULL, world VARCHAR(100) NOT NULL, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT, hidden TINYINT(1), global TINYINT(1)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
    }
}
