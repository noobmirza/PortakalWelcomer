package org.noobfly.portakalwelcomer;

import org.bukkit.plugin.java.JavaPlugin;
import org.noobfly.portakalwelcomer.cinematic.CinematicManager;
import org.noobfly.portakalwelcomer.command.WelcomerTestCommand;

public final class PortakalWelcomer extends JavaPlugin {
    private CinematicManager cinematicManager;

    @Override
    public void onEnable() {
        this.cinematicManager = new CinematicManager(this);

        if (this.getCommand("portakalwelcomer") != null) {
            this.getCommand("portakalwelcomer").setExecutor(new WelcomerTestCommand(this.cinematicManager));
        }
    }

    @Override
    public void onDisable() {
        if (this.cinematicManager != null) {
            this.cinematicManager.shutdownAll();
        }
    }

    public CinematicManager getCinematicManager() {
        return this.cinematicManager;
    }
}
