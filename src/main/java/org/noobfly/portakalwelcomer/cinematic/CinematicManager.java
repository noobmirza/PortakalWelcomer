package org.noobfly.portakalwelcomer.cinematic;

import org.noobfly.portakalwelcomer.util.ChatStyle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CinematicManager {
    static final int DEFAULT_HOLD_TICKS = 60;

    private static final String WORLD_NAME = "Spawn";
    private static final double FAR_DISTANCE_THRESHOLD = 40.0;
    private static final int NEAR_GLIDE_TICKS = 30;
    private static final float ROTATION_SMOOTHING_FACTOR = 0.10f;

    private static final List<WaypointStop> SPAWN_TOUR = List.of(
            new WaypointStop("Kabak Anıtı - Ana Giriş", 503.4, 111.0, 522.0, 0f, -12f, 90),
            new WaypointStop("Kale Kapısı", 503.4, 128.0, 555.0, 0f, 5f, 70),
            new WaypointStop("Pazar Sokağı - Kasalar", 500.9, 111.5, 558.6, 180f, 0f, 80),
            new WaypointStop("Yıldız Işığı Kasası", 510.1, 111.5, 547.5, -90f, 0f, 60),
            new WaypointStop("Dünyaya Geçiş Tüneli", 502.5, 112.0, 554.8, 90f, -55f, 60),
            new WaypointStop("Kütüphane", 453.0, 91.5, 477.9, 90f, 0f, 90),
            new WaypointStop("Gizli Zindan", 364.7, 84.5, 285.1, -90f, 0f, 90),
            new WaypointStop("Tablo Odası", 374.4, 83.8, 282.9, -90f, 0f, 100)
    );

    private final JavaPlugin plugin;
    private final Map<UUID, CinematicSession> activeSessions = new ConcurrentHashMap<>();

    public CinematicManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isInCinematic(Player player) {
        return this.activeSessions.containsKey(player.getUniqueId());
    }

    public void startTest(Player player) {
        if (this.isInCinematic(player)) {
            player.sendMessage(ChatStyle.error("Zaten bir sinematik içindesin."));
            return;
        }

        World resolvedWorld = Bukkit.getWorld(WORLD_NAME);
        if (resolvedWorld == null) {
            resolvedWorld = player.getWorld();
            player.sendMessage(ChatStyle.error("\"{}\" dünyası bulunamadı, mevcut dünyada gösteriliyor.", WORLD_NAME));
        }
        final World world = resolvedWorld;

        WaypointStop first = SPAWN_TOUR.get(0);
        Location cameraSpawn = toLocation(world, first);

        GameMode previousGameMode = player.getGameMode();
        Location previousLocation = player.getLocation().clone();

        player.teleportAsync(cameraSpawn).thenAccept(success -> {
            if (!success || !player.isOnline()) {
                return;
            }

            ItemDisplay camera = world.spawn(cameraSpawn, ItemDisplay.class, display -> {
                display.setItemStack(new ItemStack(org.bukkit.Material.AIR));
                display.setGravity(false);
                display.setSilent(true);
                display.setPersistent(false);
                display.setTeleportDuration(0);
            });

            CinematicSession session = new CinematicSession(player, camera, previousGameMode,
                    previousLocation, SPAWN_TOUR, first.yaw(), first.pitch());
            this.activeSessions.put(player.getUniqueId(), session);

            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(camera);
            player.sendMessage(ChatStyle.info("Karşılama sinematiği başlıyor..."));
            player.sendMessage(ChatStyle.info("<d>» </d>{}", first.name()));

            camera.getScheduler().runAtFixedRate(this.plugin, task -> this.tick(session),
                    () -> this.activeSessions.remove(player.getUniqueId(), session), 1L, 1L);
        });
    }

    private void tick(CinematicSession session) {
        if (!session.getPlayer().isOnline() || !session.getCamera().isValid()) {
            this.finish(session);
            return;
        }

        Player player = session.getPlayer();
        if (player.getSpectatorTarget() != session.getCamera()) {
            player.setSpectatorTarget(session.getCamera());
        }

        session.incrementPhaseTicks();

        switch (session.getPhase()) {
            case MOVE -> this.tickMove(session);
            case HOLD -> this.tickHold(session);
        }
    }

    private void tickMove(CinematicSession session) {
        WaypointStop target = session.getCurrentStop();

        float smoothedYaw = lerpAngle(session.getCurrentYaw(), target.yaw(), ROTATION_SMOOTHING_FACTOR);
        float smoothedPitch = (float) lerp(session.getCurrentPitch(), target.pitch(), ROTATION_SMOOTHING_FACTOR);
        session.setCurrentYaw(smoothedYaw);
        session.setCurrentPitch(smoothedPitch);

        Location current = session.getCamera().getLocation();
        current.setYaw(smoothedYaw);
        current.setPitch(smoothedPitch);
        session.getCamera().teleport(current);

        if (session.getPhaseTicks() >= session.getMoveDurationTicks()) {
            session.setPhase(CinematicSession.Phase.HOLD);
        }
    }

    private void tickHold(CinematicSession session) {
        WaypointStop target = session.getCurrentStop();

        float smoothedYaw = lerpAngle(session.getCurrentYaw(), target.yaw(), ROTATION_SMOOTHING_FACTOR);
        float smoothedPitch = (float) lerp(session.getCurrentPitch(), target.pitch(), ROTATION_SMOOTHING_FACTOR);
        session.setCurrentYaw(smoothedYaw);
        session.setCurrentPitch(smoothedPitch);

        Location current = session.getCamera().getLocation();
        current.setYaw(smoothedYaw);
        current.setPitch(smoothedPitch);
        session.getCamera().teleport(current);

        if (session.getPhaseTicks() < target.holdTicks()) {
            return;
        }

        if (!session.hasNextStop()) {
            this.finish(session);
            return;
        }

        session.advanceToNextStop();
        this.beginMoveToCurrentStop(session);
    }

    private void beginMoveToCurrentStop(CinematicSession session) {
        WaypointStop next = session.getCurrentStop();
        Location nextLocation = toLocation(session.getCamera().getWorld(), next);
        double distance = session.getCamera().getLocation().distance(nextLocation);

        if (distance >= FAR_DISTANCE_THRESHOLD) {
            session.getCamera().setTeleportDuration(0);
            session.getCamera().teleport(nextLocation);
            session.setCurrentYaw(next.yaw());
            session.setCurrentPitch(next.pitch());
            session.setMoveDurationTicks(0);
        } else {
            session.getCamera().setTeleportDuration(NEAR_GLIDE_TICKS);
            session.getCamera().teleport(new Location(nextLocation.getWorld(), nextLocation.getX(),
                    nextLocation.getY(), nextLocation.getZ(), session.getCurrentYaw(), session.getCurrentPitch()));
            session.setMoveDurationTicks(NEAR_GLIDE_TICKS);
        }

        session.getPlayer().sendMessage(ChatStyle.info("<d>» </d>{}", next.name()));
        session.setPhase(CinematicSession.Phase.MOVE);
    }

    private void finish(CinematicSession session) {
        ItemDisplay camera = session.getCamera();
        camera.getScheduler().run(this.plugin, retiredTask -> camera.remove(), null);

        Player player = session.getPlayer();
        this.activeSessions.remove(player.getUniqueId(), session);

        if (!player.isOnline()) {
            return;
        }

        player.getScheduler().run(this.plugin, restoreTask -> {
            player.setSpectatorTarget(null);
            player.setGameMode(session.getPreviousGameMode());
            player.teleportAsync(session.getPreviousLocation());
            player.sendMessage(ChatStyle.info("Karşılama sinematiği bitti."));
        }, null);
    }

    public void shutdownAll() {
        for (CinematicSession session : this.activeSessions.values()) {
            this.finish(session);
        }
    }

    private static Location toLocation(World world, WaypointStop stop) {
        return new Location(world, stop.x(), stop.y(), stop.z(), stop.yaw(), stop.pitch());
    }

    private static double lerp(double from, double to, double progress) {
        return from + (to - from) * progress;
    }

    private static float lerpAngle(float currentDegrees, float targetDegrees, float factor) {
        float delta = ((targetDegrees - currentDegrees + 540f) % 360f) - 180f;
        return currentDegrees + delta * factor;
    }
}
