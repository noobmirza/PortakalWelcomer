package org.noobfly.portakalwelcomer.camera;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCamera;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Sadece tek oyuncunun istemcisinde var olan item display; Set Camera paketiyle o oyuncunun bakış noktası olur.
public final class FakeCamera {
    private static final int POS_ROT_INTERPOLATION_INDEX = 10;

    private final Player viewer;
    private final int interpolationTicks;
    private int entityId;

    public FakeCamera(Player viewer, CameraPose pose, int interpolationTicks) {
        this.viewer = viewer;
        this.interpolationTicks = interpolationTicks;
        this.spawn(pose);
    }

    public void cutTo(CameraPose pose) {
        int old = this.entityId;
        this.spawn(pose);
        send(new WrapperPlayServerDestroyEntities(old));
    }

    private void spawn(CameraPose pose) {
        this.entityId = Bukkit.getUnsafe().nextEntityId(this.viewer.getWorld());
        send(new WrapperPlayServerSpawnEntity(
                this.entityId,
                Optional.of(UUID.randomUUID()),
                EntityTypes.ITEM_DISPLAY,
                new Vector3d(pose.x(), pose.y(), pose.z()),
                pose.pitch(),
                pose.yaw(),
                pose.yaw(),
                0,
                Optional.empty()
        ));
        if (this.interpolationTicks > 0) {
            send(new WrapperPlayServerEntityMetadata(this.entityId, List.of(
                    new EntityData<>(POS_ROT_INTERPOLATION_INDEX, EntityDataTypes.INT, this.interpolationTicks)
            )));
        }
        send(new WrapperPlayServerCamera(this.entityId));
    }

    public void moveTo(CameraPose pose) {
        send(new WrapperPlayServerEntityTeleport(this.entityId,
                new Vector3d(pose.x(), pose.y(), pose.z()), pose.yaw(), pose.pitch(), false));
    }

    public void remove() {
        if (this.viewer.isOnline()) {
            send(new WrapperPlayServerCamera(this.viewer.getEntityId()));
            send(new WrapperPlayServerDestroyEntities(this.entityId));
        }
    }

    private void send(PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(this.viewer, packet);
    }
}
