package org.noobfly.portakalwelcomer.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.noobfly.portakalwelcomer.cinematic.CinematicManager;

public final class WelcomerTestCommand implements CommandExecutor {
    private final CinematicManager cinematicManager;

    public WelcomerTestCommand(CinematicManager cinematicManager) {
        this.cinematicManager = cinematicManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cBu komut sadece oyuncular tarafindan kullanilabilir.");
            return true;
        }

        if (!player.hasPermission("portakalwelcomer.admin")) {
            player.sendMessage("§cBu komutu kullanma yetkin yok.");
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("test")) {
            player.sendMessage("§cKullanim: /portakalwelcomer test");
            return true;
        }

        this.cinematicManager.startTest(player);
        return true;
    }
}
