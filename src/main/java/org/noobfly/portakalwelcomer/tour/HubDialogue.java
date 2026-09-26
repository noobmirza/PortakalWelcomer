package org.noobfly.portakalwelcomer.tour;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

final class HubDialogue {
    private HubDialogue() {
    }

    static boolean start(Player player, String speaker, String portrait, List<String> pages, boolean manual,
                         Consumer<Player> onFinish) {
        Plugin hub = hub();
        if (hub == null) {
            return false;
        }
        try {
            Object result;
            try {
                Method start = hub.getClass().getMethod("startNpcDialogue", Player.class, String.class, String.class,
                        List.class, List.class, List.class, Consumer.class, boolean.class);
                result = start.invoke(hub, player, speaker, portrait, pages, List.of(), List.of(), onFinish, manual);
            } catch (NoSuchMethodException older) {
                Method start = hub.getClass().getMethod("startNpcDialogue", Player.class, String.class, String.class,
                        List.class, List.class, List.class, Consumer.class);
                result = start.invoke(hub, player, speaker, portrait, pages, List.of(), List.of(), onFinish);
            }
            return result instanceof Boolean b && b;
        } catch (ReflectiveOperationException e) {
            Bukkit.getLogger().log(Level.WARNING, "[PortakalWelcomer] PortakalHub diyaloğu açılamadı", e);
            return false;
        }
    }

    static boolean isActive(Player player) {
        Plugin hub = hub();
        if (hub == null) {
            return false;
        }
        try {
            Object result = hub.getClass().getMethod("isInNpcDialogue", Player.class).invoke(hub, player);
            return result instanceof Boolean b && b;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static Plugin hub() {
        Plugin hub = Bukkit.getPluginManager().getPlugin("portakalhub");
        return hub != null && hub.isEnabled() ? hub : null;
    }
}
