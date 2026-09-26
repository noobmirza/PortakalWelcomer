package org.noobfly.portakalwelcomer.camera;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.List;

public final class Reach {
    private static final List<Attribute> ATTRIBUTES = List.of(Attribute.ENTITY_INTERACTION_RANGE, Attribute.BLOCK_INTERACTION_RANGE);

    private Reach() {
    }

    public static void disable(Player player, NamespacedKey key) {
        set(player, key, false);
    }

    public static void restore(Player player, NamespacedKey key) {
        set(player, key, true);
    }

    private static void set(Player player, NamespacedKey key, boolean restore) {
        for (Attribute attribute : ATTRIBUTES) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            instance.removeModifier(key);
            if (!restore) {
                instance.addTransientModifier(new AttributeModifier(key, -1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
            }
        }
    }
}
