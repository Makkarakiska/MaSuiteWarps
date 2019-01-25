package fi.matiaspaavilainen.masuitewarps.core.objects;

import fi.matiaspaavilainen.masuitecore.core.database.ConnectionManager;
import fi.matiaspaavilainen.masuitecore.core.database.Database;
import fi.matiaspaavilainen.masuitecore.core.objects.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class Warp {

    private Database db = ConnectionManager.db;
    private Connection connection = null;
    private PreparedStatement statement = null;
    private String tablePrefix;
    // Info
    private int id;
    private String name;
    private String server;
    private Boolean hidden;
    private Boolean global;

    //Location
    private Location location;

    /**
     * An empty constructor for Warp
     */
    public Warp() {
        this.tablePrefix = db.getTablePrefix();
    }

    /**
     * Constructor for Warp
     *
     * @param name   name of the warp
     * @param server server of the warp
     * @param loc    location of the warp
     * @param hidden is warp hidden or not
     * @param global is warp global or not
     */
    public Warp(String name, String server, Location loc, boolean hidden, boolean global) {
        this.name = name;
        this.server = server;
        this.hidden = hidden;
        this.global = global;
        this.location = loc;
        this.tablePrefix = db.getTablePrefix();
    }

    /**
     * A constructor for Bukkit side Warp
     * <p>
     * Note: Use only when bridge mode is enabled!
     * </p>
     */
    public Warp(String name, boolean hidden, boolean global) {
        this.name = name;
        this.hidden = hidden;
        this.global = global;
    }

    /**
     * Create Warp
     */
    public void create() {
        String insert = "INSERT INTO " + tablePrefix +
                "warps (name, server, world, x, y, z, yaw, pitch, hidden, global) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = ?, server = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, hidden = ?, global = ?;";
        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement(insert);
            statement.setString(1, this.name);
            statement.setString(2, this.server);
            statement.setString(3, this.location.getWorld());
            statement.setDouble(4, this.location.getX());
            statement.setDouble(5, this.location.getY());
            statement.setDouble(6, this.location.getZ());
            statement.setFloat(7, this.location.getYaw());
            statement.setFloat(8, this.location.getPitch());
            statement.setBoolean(9, this.hidden);
            statement.setBoolean(10, this.global);
            statement.setString(11, this.name);
            statement.setString(12, this.server);
            statement.setString(13, this.location.getWorld());
            statement.setDouble(14, this.location.getX());
            statement.setDouble(15, this.location.getY());
            statement.setDouble(16, this.location.getZ());
            statement.setFloat(17, this.location.getYaw());
            statement.setFloat(18, this.location.getPitch());
            statement.setBoolean(19, this.hidden);
            statement.setBoolean(20, this.global);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Find warp by name
     *
     * @param name name of the warp
     * @return warp with given information
     */
    public Warp find(String name) {
        Warp warp = new Warp();
        ResultSet rs = null;

        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "warps WHERE name = ?");
            statement.setString(1, name);
            rs = statement.executeQuery();

            if (rs == null) {
                return new Warp();
            }
            while (rs.next()) {
                warp.setId(rs.getInt("id"));
                warp.setName(rs.getString("name"));
                warp.setServer(rs.getString("server"));
                warp.setLocation(new Location(rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch")));
                warp.setHidden(rs.getBoolean("hidden"));
                warp.setGlobal(rs.getBoolean("global"));
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return warp;
    }

    /**
     * DeleteCommand warp by name
     *
     * @param name name of the warp
     * @return if deleting successful
     */
    public Boolean delete(String name) {
        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "warps WHERE name = ?");
            statement.setString(1, name);
            statement.execute();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * Get all warps from database
     *
     * @return SetCommand of warps
     */
    public Set<Warp> all() {
        Set<Warp> warps = new HashSet<>();
        ResultSet rs = null;

        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "warps;");
            rs = statement.executeQuery();
            while (rs.next()) {
                Warp warp = new Warp();
                warp.setId(rs.getInt("id"));
                warp.setName(rs.getString("name"));
                warp.setServer(rs.getString("server"));
                warp.setLocation(new Location(rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch")));
                warp.setHidden(rs.getBoolean("hidden"));
                warp.setGlobal(rs.getBoolean("global"));
                warps.add(warp);
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return warps;
    }

    /**
     * @return id of the warp
     */
    public int getId() {
        return id;
    }

    /**
     * @param id id of the warp
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return name of the warp
     */
    public String getName() {
        return name;
    }

    /**
     * @param name name of the warp
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return server of the warp
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server server of the warp
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * @return is warp global or not
     */
    public Boolean isGlobal() {
        return this.global;
    }

    /**
     * @param global is warp global or not
     */
    public void setGlobal(Boolean global) {
        this.global = global;
    }

    /**
     * @return is warp hidden or not
     */
    public Boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * @return location of the warp
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location location of the warp
     */
    public void setLocation(Location location) {
        this.location = location;
    }
}
