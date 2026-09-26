package org.noobfly.portakalwelcomer.camera;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public record CameraPose(double x, double y, double z, float yaw, float pitch) {
    public static CameraPose of(Location eye) {
        return new CameraPose(eye.getX(), eye.getY(), eye.getZ(), eye.getYaw(), eye.getPitch());
    }

    public static CameraPose lookAt(Vector from, Vector target) {
        double dx = target.getX() - from.getX();
        double dy = target.getY() - from.getY();
        double dz = target.getZ() - from.getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return new CameraPose(from.getX(), from.getY(), from.getZ(), yaw, pitch);
    }

    public Vector position() {
        return new Vector(this.x, this.y, this.z);
    }

    public Vector direction() {
        double yawRad = Math.toRadians(this.yaw);
        double pitchRad = Math.toRadians(this.pitch);
        return new Vector(-Math.sin(yawRad) * Math.cos(pitchRad), -Math.sin(pitchRad), Math.cos(yawRad) * Math.cos(pitchRad));
    }

    public static CameraPose lerp(CameraPose a, CameraPose b, double t) {
        float yawDelta = wrapDegrees(b.yaw - a.yaw);
        return new CameraPose(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t,
                a.z + (b.z - a.z) * t,
                wrapDegrees((float) (a.yaw + yawDelta * t)),
                (float) (a.pitch + (b.pitch - a.pitch) * t)
        );
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }
}
