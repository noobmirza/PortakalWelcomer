package org.noobfly.portakalwelcomer.tour;

import org.bukkit.util.Vector;
import org.noobfly.portakalwelcomer.camera.CameraPose;
import org.noobfly.portakalwelcomer.camera.Shot;

import java.util.List;

public interface StopShot {
    CameraPose at(double tick);

    static StopShot dolly(Vector from, Vector to, Vector focus, int ticks) {
        return tick -> {
            double e = Shot.ease(tick / ticks);
            return CameraPose.lookAt(from.clone().add(to.clone().subtract(from).multiply(e)), focus);
        };
    }

    static StopShot path(List<Vector> points, List<Vector> looks, int ticks) {
        return tick -> {
            double s = Shot.ease(tick / ticks) * (points.size() - 1);
            return CameraPose.lookAt(catmullRom(points, s), catmullRom(looks, s));
        };
    }

    private static Vector catmullRom(List<Vector> p, double s) {
        int last = p.size() - 1;
        int i = Math.min(last - 1, (int) Math.floor(s));
        double u = s - i;
        Vector p0 = p.get(Math.max(0, i - 1));
        Vector p1 = p.get(i);
        Vector p2 = p.get(i + 1);
        Vector p3 = p.get(Math.min(last, i + 2));
        double u2 = u * u;
        double u3 = u2 * u;
        return p1.clone().multiply(2.0)
                .add(p2.clone().subtract(p0).multiply(u))
                .add(p0.clone().multiply(2.0).subtract(p1.clone().multiply(5.0)).add(p2.clone().multiply(4.0)).subtract(p3).multiply(u2))
                .add(p1.clone().multiply(3.0).subtract(p0).subtract(p2.clone().multiply(3.0)).add(p3).multiply(u3))
                .multiply(0.5);
    }
}
