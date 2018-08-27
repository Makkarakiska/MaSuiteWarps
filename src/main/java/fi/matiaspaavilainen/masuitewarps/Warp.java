package fi.matiaspaavilainen.masuitewarps;

import fi.matiaspaavilainen.masuitecore.MaSuiteCore;
import fi.matiaspaavilainen.masuitecore.config.Configuration;
import fi.matiaspaavilainen.masuitecore.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class Warp {

    Database db = MaSuiteCore.db;
    Connection connection = null;
    PreparedStatement statement = null;
    Configuration config = new Configuration();
    String tablePrefix = config.load(null, "config.yml").getString("database.table-prefix");
    // Info
    private int id;
    private String name;
    private String server;
    private Boolean hidden;
    private Boolean global;
    //Location
    private String world;
    private Double x;
    private Double y;
    private Double z;
    private Double yaw;
    private Double pitch;

    public Warp() {
    }

    public Warp(String name, String server, String world, Double x, Double y, Double z, Double yaw, Double pitch, Boolean hidden, Boolean global) {
        this.name = name;
        this.server = server;
        this.hidden = hidden;
        this.global = global;

        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void create(Warp warp) {
        String insert = "INSERT INTO " + tablePrefix +
                "warps (name, server, world, x, y, z, yaw, pitch, hidden, global) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try {
            connection = db.hikari.getConnection();
            statement = connection.prepareStatement(insert);
            statement.setString(1, warp.getName());
            statement.setString(2, warp.getServer());
            statement.setString(3, warp.getWorld());
            statement.setDouble(4, warp.getX());
            statement.setDouble(5, warp.getY());
            statement.setDouble(6, warp.getZ());
            statement.setDouble(7, warp.getY());
            statement.setDouble(8, warp.getZ());
            statement.setBoolean(9, warp.isHidden());
            statement.setBoolean(10, warp.isGlobal());
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
            statement = connection.prepareStatement("SELECT * FROM masuite_warps WHERE name LIKE ?");
            statement.setString(1, name);
            rs = statement.executeQuery();

            while (rs.next()) {
                warp.setId(rs.getInt("id"));
                warp.setName(rs.getString("name"));
                warp.setServer(rs.getString("server"));
                warp.setWorld(rs.getString("world"));
                warp.setId(rs.getInt("id"));
                warp.setId(rs.getInt("id"));
                warp.setX(rs.getDouble("x"));
                warp.setY(rs.getDouble("y"));
                warp.setZ(rs.getDouble("z"));
                warp.setYaw(rs.getDouble("yaw"));
                warp.setPitch(rs.getDouble("pitch"));
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

    public HashMap<Integer, Warp> all(){
        HashMap<Integer, Warp> warps = new HashMap<>();
        ResultSet rs = null;

        try {
            connection = MaSuiteCore.db.hikari.getConnection();
            statement = connection.prepareStatement("SELECT * FROM masuite_warps");
            rs = statement.executeQuery();
            while (rs.next()) {
                Warp warp = new Warp();
                warp.setId(rs.getInt("id"));
                warp.setName(rs.getString("name"));
                warp.setServer(rs.getString("server"));
                warp.setWorld(rs.getString("world"));
                warp.setId(rs.getInt("id"));
                warp.setId(rs.getInt("id"));
                warp.setX(rs.getDouble("x"));
                warp.setY(rs.getDouble("y"));
                warp.setZ(rs.getDouble("z"));
                warp.setYaw(rs.getDouble("yaw"));
                warp.setPitch(rs.getDouble("pitch"));
                warps.put(warp.getId(), warp);
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

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public Double getYaw() {
        return yaw;
    }

    public void setYaw(Double yaw) {
        this.yaw = yaw;
    }

    public Double getPitch() {
        return pitch;
    }

    public void setPitch(Double pitch) {
        this.pitch = pitch;
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
}
