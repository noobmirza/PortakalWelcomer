package org.noobfly.portakalwelcomer;

import org.bukkit.plugin.java.JavaPlugin;
import org.noobfly.portakalwelcomer.camera.CameraLockCommand;
import org.noobfly.portakalwelcomer.camera.CameraLockManager;
import org.noobfly.portakalwelcomer.camera.DeathCamManager;
import org.noobfly.portakalwelcomer.tour.TourManager;

public final class PortakalWelcomer extends JavaPlugin {
    private CameraLockManager cameraLockManager;
    private TourManager tourManager;
    private DeathCamManager deathCamManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.cameraLockManager = new CameraLockManager(this);
        this.tourManager = new TourManager(this);
        this.deathCamManager = new DeathCamManager(this, this.tourManager);
        this.getServer().getPluginManager().registerEvents(this.cameraLockManager, this);
        this.getServer().getPluginManager().registerEvents(this.tourManager, this);
        this.getServer().getPluginManager().registerEvents(this.deathCamManager, this);

        if (this.getCommand("portakalwelcomer") != null) {
            this.getCommand("portakalwelcomer").setExecutor(new CameraLockCommand(this, this.cameraLockManager, this.tourManager, this.deathCamManager));
        }
    }

    @Override
    public void onDisable() {
        if (this.deathCamManager != null) {
            this.deathCamManager.shutdown();
        }
        if (this.tourManager != null) {
            this.tourManager.shutdown();
        }
        if (this.cameraLockManager != null) {
            this.cameraLockManager.releaseAll();
        }
    }
}
