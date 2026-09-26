package org.noobfly.portakalwelcomer.camera;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.noobfly.portakalwelcomer.tour.TourManager;
import org.noobfly.portakalwelcomer.util.ChatStyle;

import java.util.List;
import java.util.Locale;

public final class CameraLockCommand implements TabExecutor {
    private static final List<String> SUBCOMMANDS = List.of("tanitim", "kilit", "sinematik", "olum", "birak");
    private static final List<String> TARGETED = List.of("tanitim", "birak");

    private final JavaPlugin plugin;
    private final CameraLockManager manager;
    private final TourManager tourManager;
    private final DeathCamManager deathCam;

    public CameraLockCommand(JavaPlugin plugin, CameraLockManager manager, TourManager tourManager, DeathCamManager deathCam) {
        this.plugin = plugin;
        this.manager = manager;
        this.tourManager = tourManager;
        this.deathCam = deathCam;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("portakalwelcomer.admin")) {
            sender.sendMessage(ChatStyle.error("Bunun için yetkin yok."));
            return true;
        }

        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(usage(label));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            if (!TARGETED.contains(sub)) {
                sender.sendMessage(usage(label));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatStyle.error("Oyuncu bulunamadı."));
                return true;
            }
            this.handleTarget(sender, target, sub);
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatStyle.error("Bu komutu sadece oyuncular kullanabilir."));
            return true;
        }

        switch (sub) {
            case "tanitim" -> {
                this.manager.release(player);
                this.tourManager.start(player);
                player.sendMessage(ChatStyle.success("Tanıtım başlıyor. Durdurmak için <c>/" + label + " birak</c>"));
            }
            case "kilit" -> {
                this.tourManager.stop(player);
                this.manager.lock(player);
                player.sendMessage(ChatStyle.success("Kamera şu anki bakış yönüne kilitlendi. Açmak için <c>/" + label + " birak</c>"));
            }
            case "sinematik" -> {
                this.tourManager.stop(player);
                int ticks = this.manager.playCinematic(player);
                player.sendMessage(ChatStyle.success("Test sinematiği başladı, <v>{}</v> saniye sürecek. Durdurmak için <c>/" + label + " birak</c>", ticks / 20));
            }
            case "olum" -> {
                if (this.deathCam.toggle()) {
                    player.sendMessage(ChatStyle.success("Ölüm kamerası herkes için açıldı."));
                } else {
                    player.sendMessage(ChatStyle.success("Ölüm kamerası herkes için kapatıldı."));
                }
            }
            case "birak" -> {
                if (this.deathCam.release(player)) {
                    player.sendMessage(ChatStyle.success("Ölüm kamerası durduruldu."));
                } else if (this.tourManager.stop(player)) {
                    player.sendMessage(ChatStyle.success("Tanıtım durduruldu."));
                } else if (this.manager.release(player)) {
                    player.sendMessage(ChatStyle.success("Kamera kilidi kaldırıldı."));
                } else {
                    player.sendMessage(ChatStyle.warn("Kameran zaten kilitli değil."));
                }
            }
            default -> player.sendMessage(usage(label));
        }
        return true;
    }

    private void handleTarget(CommandSender sender, Player target, String sub) {
        String name = target.getName();
        target.getScheduler().run(this.plugin, task -> {
            if (sub.equals("tanitim")) {
                this.manager.release(target);
                this.tourManager.start(target);
                reply(sender, ChatStyle.success("<v>{}</v> tanıtıma sokuldu.", name));
            } else if (this.deathCam.release(target)) {
                reply(sender, ChatStyle.success("<v>{}</v> için ölüm kamerası durduruldu.", name));
            } else if (this.tourManager.stop(target)) {
                reply(sender, ChatStyle.success("<v>{}</v> için tanıtım durduruldu.", name));
            } else if (this.manager.release(target)) {
                reply(sender, ChatStyle.success("<v>{}</v> için kamera kilidi kaldırıldı.", name));
            } else {
                reply(sender, ChatStyle.warn("<v>{}</v> tanıtımda değil.", name));
            }
        }, () -> reply(sender, ChatStyle.error("Oyuncu bulunamadı.")));
    }

    private void reply(CommandSender sender, Component message) {
        if (sender instanceof Player player) {
            player.getScheduler().run(this.plugin, t -> player.sendMessage(message), null);
        } else {
            Bukkit.getGlobalRegionScheduler().run(this.plugin, t -> sender.sendMessage(message));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("portakalwelcomer.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2 && TARGETED.contains(args[0].toLowerCase(Locale.ROOT))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }

    private static Component usage(String label) {
        return ChatStyle.usage("Kullanım: <c>/" + label + " tanitim [oyuncu]</c>, <c>/" + label + " kilit</c>, <c>/" + label + " sinematik</c>, <c>/" + label + " olum</c> ya da <c>/" + label + " birak [oyuncu]</c>");
    }
}
