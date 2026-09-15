package org.noobfly.portakalwelcomer.cinematic;

public record WaypointStop(String name, double x, double y, double z, float yaw, float pitch, int holdTicks) {
    public WaypointStop(String name, double x, double y, double z, float yaw, float pitch) {
        this(name, x, y, z, yaw, pitch, CinematicManager.DEFAULT_HOLD_TICKS);
    }
}
