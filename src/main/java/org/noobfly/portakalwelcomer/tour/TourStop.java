package org.noobfly.portakalwelcomer.tour;

import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Function;

public record TourStop(String speaker, String portrait, List<String> pages, Function<Vector, StopShot> shot,
                       Vector anchor, int minTicks, boolean manual) {
    public TourStop waitForShift() {
        return new TourStop(this.speaker, this.portrait, this.pages, this.shot, this.anchor, this.minTicks, true);
    }

    private static final double EYE_HEIGHT = 1.62;

    public static TourStop dolly(String speaker, String portrait, List<String> pages,
                                 Vector eye, Vector focus, double back, double rise, int ticks, Vector anchor) {
        Vector away = eye.clone().subtract(focus).setY(0);
        if (away.lengthSquared() > 1.0E-6) {
            away.normalize();
        }
        Vector from = eye.clone().add(away.multiply(back)).add(new Vector(0.0, rise, 0.0));
        return new TourStop(speaker, portrait, pages, previous -> StopShot.dolly(from, eye, focus, ticks),
                anchor != null ? anchor : feet(eye), ticks, false);
    }

    public static TourStop path(String speaker, String portrait, List<String> pages,
                                Vector from, Vector to, Vector focus, int ticks) {
        return path(speaker, portrait, pages, from, to, focus, ticks, feet(to));
    }

    public static TourStop path(String speaker, String portrait, List<String> pages,
                                Vector from, Vector to, Vector focus, int ticks, Vector anchor) {
        return new TourStop(speaker, portrait, pages, previous -> StopShot.dolly(from, to, focus, ticks), anchor, ticks, false);
    }

    public static TourStop glide(String speaker, String portrait, List<String> pages,
                                 List<Vector> points, List<Vector> looks, int ticks, Vector anchor) {
        return new TourStop(speaker, portrait, pages, previous -> StopShot.path(points, looks, ticks), anchor, ticks, false);
    }

    private static Vector feet(Vector eye) {
        return eye.clone().subtract(new Vector(0.0, EYE_HEIGHT, 0.0));
    }
}
