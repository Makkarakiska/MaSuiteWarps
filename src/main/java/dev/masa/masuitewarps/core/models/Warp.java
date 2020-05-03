package dev.masa.masuitewarps.core.models;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;
import dev.masa.masuitecore.core.objects.Location;
import lombok.*;

import javax.persistence.Table;


@NoArgsConstructor
@RequiredArgsConstructor
@Data
@Table(name = "masuite_warps")
public class Warp {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(unique = true, canBeNull = false)
    private String name;

    @NonNull
    @DatabaseField
    private Boolean hidden;

    @NonNull
    @DatabaseField
    private Boolean global;

    @DatabaseField
    private String server;
    @DatabaseField
    private String world;
    @DatabaseField
    private Double x;
    @DatabaseField
    private Double y;
    @DatabaseField
    private Double z;
    @DatabaseField
    private Float yaw = 0.0F;
    @DatabaseField
    private Float pitch = 0.0F;

    public Warp(String name, boolean publicity, boolean type, Location location) {
        this.name = name;
        this.hidden = publicity;
        this.global = type;
        this.setLocation(location);
    }

    public Location getLocation() {
        return new Location(server, world, x, y, z, yaw, pitch);
    }

    public void setLocation(Location loc) {
        this.server = loc.getServer();
        this.world = loc.getWorld();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isGlobal() {
        return this.global;
    }

    public String serialize() {
        return new Gson().toJson(this);
    }

    public Warp deserialize(String json) {
        return new Gson().fromJson(json, Warp.class);
    }
}
