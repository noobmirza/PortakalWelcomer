package org.noobfly.portakalwelcomer.command;

import org.noobfly.portakalwelcomer.util.ChatStyle;
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
            sender.sendMessage(ChatStyle.error("Bu komutu sadece oyuncular kullanabilir."));
            return true;
        }

        if (!player.hasPermission("portakalwelcomer.admin")) {
            player.sendMessage(ChatStyle.error("Bunun için yetkin yok."));
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("test")) {
            player.sendMessage(ChatStyle.usage("Kullanım: <c>/portakalwelcomer test</c>"));
            return true;
        }

        this.cinematicManager.startTest(player);
        return true;
    }
}
