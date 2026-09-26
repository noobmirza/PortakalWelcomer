package org.noobfly.portakalwelcomer.camera;

import org.bukkit.util.Vector;

public interface Shot {
    int ticks();

    CameraPose at(double t);

    static double ease(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    static Shot transition(CameraPose from, Shot to, int ticks) {
        return of(ticks, t -> CameraPose.lerp(from, to.at(0.0), ease(t)));
    }

    static Shot orbit(Vector focus, int ticks,
                      double fromDegrees, double toDegrees,
                      double fromHeight, double toHeight,
                      double fromRadius, double toRadius) {
        return of(ticks, t -> {
            double e = ease(t);
            double angle = Math.toRadians(fromDegrees + (toDegrees - fromDegrees) * e);
            double height = fromHeight + (toHeight - fromHeight) * e;
            double radius = fromRadius + (toRadius - fromRadius) * e;
            Vector eye = new Vector(
                    focus.getX() - Math.sin(angle) * radius,
                    focus.getY() + height,
                    focus.getZ() + Math.cos(angle) * radius
            );
            return CameraPose.lookAt(eye, focus);
        });
    }

    static Shot of(int ticks, java.util.function.DoubleFunction<CameraPose> pose) {
        return new Shot() {
            @Override
            public int ticks() {
                return ticks;
            }

            @Override
            public CameraPose at(double t) {
                return pose.apply(t);
            }
        };
    }
}
