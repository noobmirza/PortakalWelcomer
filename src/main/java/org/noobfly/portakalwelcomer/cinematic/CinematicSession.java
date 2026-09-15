package org.noobfly.portakalwelcomer.cinematic;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;

import java.util.List;

public final class CinematicSession {
    public enum Phase {
        MOVE,
        HOLD
    }

    private final Player player;
    private final ItemDisplay camera;
    private final GameMode previousGameMode;
    private final Location previousLocation;
    private final List<WaypointStop> stops;

    private int stopIndex;
    private Phase phase;
    private int phaseTicks;
    private int moveDurationTicks;
    private float currentYaw;
    private float currentPitch;

    public CinematicSession(Player player, ItemDisplay camera, GameMode previousGameMode,
                             Location previousLocation, List<WaypointStop> stops,
                             float initialYaw, float initialPitch) {
        this.player = player;
        this.camera = camera;
        this.previousGameMode = previousGameMode;
        this.previousLocation = previousLocation;
        this.stops = stops;
        this.stopIndex = 0;
        this.phase = Phase.HOLD;
        this.phaseTicks = 0;
        this.moveDurationTicks = 0;
        this.currentYaw = initialYaw;
        this.currentPitch = initialPitch;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ItemDisplay getCamera() {
        return this.camera;
    }

    public GameMode getPreviousGameMode() {
        return this.previousGameMode;
    }

    public Location getPreviousLocation() {
        return this.previousLocation;
    }

    public List<WaypointStop> getStops() {
        return this.stops;
    }

    public WaypointStop getCurrentStop() {
        return this.stops.get(this.stopIndex);
    }

    public boolean hasNextStop() {
        return this.stopIndex + 1 < this.stops.size();
    }

    public void advanceToNextStop() {
        this.stopIndex++;
    }

    public Phase getPhase() {
        return this.phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
        this.phaseTicks = 0;
    }

    public int getPhaseTicks() {
        return this.phaseTicks;
    }

    public void incrementPhaseTicks() {
        this.phaseTicks++;
    }

    public int getMoveDurationTicks() {
        return this.moveDurationTicks;
    }

    public void setMoveDurationTicks(int moveDurationTicks) {
        this.moveDurationTicks = moveDurationTicks;
    }

    public float getCurrentYaw() {
        return this.currentYaw;
    }

    public float getCurrentPitch() {
        return this.currentPitch;
    }

    public void setCurrentYaw(float currentYaw) {
        this.currentYaw = currentYaw;
    }

    public void setCurrentPitch(float currentPitch) {
        this.currentPitch = currentPitch;
    }
}
