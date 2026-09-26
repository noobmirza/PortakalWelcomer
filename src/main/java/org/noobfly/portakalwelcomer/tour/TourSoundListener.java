package org.noobfly.portakalwelcomer.tour;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.noobfly.portakalwelcomer.camera.CameraPose;

final class TourSoundListener extends PacketListenerAbstract {
    private static final double AT_PLAYER_SQ = 1.5 * 1.5;

    private final TourManager manager;

    TourSoundListener(TourManager manager) {
        super(PacketListenerPriority.NORMAL);
        this.manager = manager;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.SOUND_EFFECT || !(event.getPlayer() instanceof Player player)) {
            return;
        }
        TourSession session = this.manager.session(player);
        if (session == null) {
            return;
        }
        WrapperPlayServerSoundEffect sound = new WrapperPlayServerSoundEffect(event);
        Vector3d pos = sound.getPosition();
        Location at = player.getLocation();
        double dx = pos.getX() - at.getX();
        double dy = pos.getY() - at.getY();
        double dz = pos.getZ() - at.getZ();
        if (dx * dx + dy * dy + dz * dz > AT_PLAYER_SQ) {
            return;
        }
        CameraPose camera = session.cameraPose();
        sound.setPosition(new Vector3d(camera.x(), camera.y(), camera.z()));
        event.markForReEncode(true);
    }
}
