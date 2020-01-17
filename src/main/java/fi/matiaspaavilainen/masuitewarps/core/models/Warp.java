package fi.matiaspaavilainen.masuitewarps.core.models;

import fi.matiaspaavilainen.masuitecore.core.objects.Location;
import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
@Table(name = "masuite_warps")

@NamedQuery(
        name = "findWarp",
        query = "SELECT w FROM Warp w WHERE w.name = :name ORDER BY w.name"
)
public class Warp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    @Column(name = "name", unique = true)
    private String name;

    @NonNull
    @Column(name = "hidden")
    private Boolean hidden;

    @NonNull
    @Column(name = "global")
    private Boolean global;

    @Embedded
    @AttributeOverrides(value = {
            @AttributeOverride(name = "x", column = @Column(name = "x")),
            @AttributeOverride(name = "y", column = @Column(name = "y")),
            @AttributeOverride(name = "z", column = @Column(name = "z")),
            @AttributeOverride(name = "yaw", column = @Column(name = "yaw")),
            @AttributeOverride(name = "pitch", column = @Column(name = "pitch")),
            @AttributeOverride(name = "server", column = @Column(name = "server"))
    })
    @NonNull
    private Location location;

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isGlobal() {
        return this.global;
    }
}
