package fi.matiaspaavilainen.masuitewarps.bukkit;

public class Warp {

    private String name;
    private Boolean hidden;
    private Boolean global;

    /**
     * An empty constructor for Warp
     */
    public Warp() {
    }

    /**
     * Constructor for Warp
     *
     * @param name   name of the warp
     * @param global is warp global or not
     * @param hidden is warp hidden or not
     */
    public Warp(String name, Boolean global, Boolean hidden) {
        this.name = name;
        this.global = global;
        this.hidden = hidden;
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
     * @return is warp global or not
     */
    public Boolean isGlobal() {
        return global;
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
        return hidden;
    }

    /**
     * @param hidden is warp hidden or not
     */
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
}
