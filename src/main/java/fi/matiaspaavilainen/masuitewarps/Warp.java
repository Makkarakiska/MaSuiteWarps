package fi.matiaspaavilainen.masuitewarps;

import fi.matiaspaavilainen.masuitecore.MaSuiteCore;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitecore.database.Database;
import fi.matiaspaavilainen.masuitecore.managers.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class Warp {

    private Database db = MaSuiteWarps.db;
    private Connection connection = null;
    private PreparedStatement statement = null;
    private Configuration config = new Configuration();
    private String tablePrefix = config.load(null, "config.yml").getString("database.table-prefix");
    // Info
    private int id;
    private String name;
    private String server;
    private Boolean hidden;
    private Boolean global;
    //Location

    private Location location;


    public Warp() {
    }

    public Warp(String name, String server, Location loc, Boolean hidden, Boolean global) {
        this.name = name;
        this.server = server;
        this.hidden = hidden;
        this.global = global;

        this.location = loc;
    }

    public void create(Warp warp) {
        String insert = "INSERT INTO " + tablePrefix +
                "warps (name, server, world, x, y, z, yaw, pitch, hidden, global) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = ?, server = ?, world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, hidden = ?, global = ?;";
        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement(insert);
            statement.setString(1, warp.getName());
            statement.setString(2, warp.getServer());
            statement.setString(3, warp.getLocation().getWorld());
            statement.setDouble(4, warp.getLocation().getX());
            statement.setDouble(5, warp.getLocation().getY());
            statement.setDouble(6, warp.getLocation().getZ());
            statement.setFloat(7, warp.getLocation().getYaw());
            statement.setFloat(8, warp.getLocation().getPitch());
            statement.setBoolean(9, warp.isHidden());
            statement.setBoolean(10, warp.isGlobal());
            statement.setString(11, warp.getName());
            statement.setString(12, warp.getServer());
            statement.setString(13, warp.getLocation().getWorld());
            statement.setDouble(14, warp.getLocation().getX());
            statement.setDouble(15, warp.getLocation().getY());
            statement.setDouble(16, warp.getLocation().getZ());
            statement.setFloat(17, warp.getLocation().getYaw());
            statement.setFloat(18, warp.getLocation().getPitch());
            statement.setBoolean(19, warp.isHidden());
            statement.setBoolean(20, warp.isGlobal());
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
        }
    }

    public Warp find(String name) {
        Warp warp = new Warp();
        ResultSet rs = null;

        try {
            connection = MaSuiteCore.db.hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "warps WHERE name = ?");
            statement.setString(1, name);
            rs = statement.executeQuery();

            if(rs == null){
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

    public Boolean delete(String name){
        try {
            connection = MaSuiteCore.db.hikari.getConnection();
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

    public Set<Warp> all(){
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Boolean isGlobal() {
        return this.global;
    }

    public void setGlobal(Boolean global) {
        this.global = global;
    }

    public Boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
