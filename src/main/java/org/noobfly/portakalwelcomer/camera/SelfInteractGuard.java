package org.noobfly.portakalwelcomer.camera;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

// Menzil engelinden sızan, oyuncunun kendi entity id'sine giden saldırı ve etkileşim paketlerini düşürür.
public final class SelfInteractGuard extends PacketListenerAbstract {
    private final Predicate<Player> guarded;

    public SelfInteractGuard(Predicate<Player> guarded) {
        super(PacketListenerPriority.LOWEST);
        this.guarded = guarded;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        boolean attack = event.getPacketType() == PacketType.Play.Client.ATTACK;
        if (!attack && event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player) || !this.guarded.test(player)) {
            return;
        }
        int target = attack ? new WrapperPlayClientAttack(event).getEntityId() : new WrapperPlayClientInteractEntity(event).getEntityId();
        if (target == player.getEntityId()) {
            event.setCancelled(true);
        }
    }
}
