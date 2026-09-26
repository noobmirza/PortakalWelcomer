package org.noobfly.portakalwelcomer.camera;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CameraLockManager implements Listener {
    private static final int SEND_INTERVAL_TICKS = 2;
    private static final int INTERPOLATION_TICKS = SEND_INTERVAL_TICKS + 1;

    private final Plugin plugin;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public CameraLockManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void lock(Player player) {
        this.release(player);

        FakeCamera camera = new FakeCamera(player, CameraPose.of(player.getEyeLocation()), 0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, false));

        this.sessions.put(player.getUniqueId(), new Session(camera, null, () -> player.removePotionEffect(PotionEffectType.INVISIBILITY)));
    }

    public int playCinematic(Player player) {
        this.release(player);

        CameraPose start = CameraPose.of(player.getEyeLocation());
        List<Shot> shots = buildTestShots(player, start);
        int total = shots.stream().mapToInt(Shot::ticks).sum();

        FakeCamera camera = new FakeCamera(player, start, INTERPOLATION_TICKS);

        float walkSpeed = player.getWalkSpeed();
        float flySpeed = player.getFlySpeed();
        player.setWalkSpeed(0.0F);
        player.setFlySpeed(0.0F);

        int[] tick = {0};
        ScheduledTask task = player.getScheduler().runAtFixedRate(this.plugin, scheduled -> {
            tick[0]++;
            if (tick[0] >= total) {
                this.release(player);
                return;
            }
            if (tick[0] % SEND_INTERVAL_TICKS == 0) {
                camera.moveTo(poseAt(shots, Math.min(total, tick[0] + INTERPOLATION_TICKS)));
            }
        }, null, 1L, 1L);

        this.sessions.put(player.getUniqueId(), new Session(camera, task, () -> {
            player.setWalkSpeed(walkSpeed);
            player.setFlySpeed(flySpeed);
        }));
        return total;
    }

    public boolean release(Player player) {
        Session session = this.sessions.remove(player.getUniqueId());
        if (session == null) {
            return false;
        }

        if (session.task() != null) {
            session.task().cancel();
        }
        session.camera().remove();
        if (player.isOnline()) {
            session.restore().run();
        }
        return true;
    }

    public void releaseAll() {
        for (UUID uuid : List.copyOf(this.sessions.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                this.release(player);
            } else {
                Session session = this.sessions.remove(uuid);
                if (session != null && session.task() != null) {
                    session.task().cancel();
                }
            }
        }
    }

    private static List<Shot> buildTestShots(Player player, CameraPose start) {
        Vector focus = player.getLocation().toVector().add(new Vector(0.0, 1.2, 0.0));
        double facing = player.getLocation().getYaw();

        List<Shot> shots = new ArrayList<>();
        Shot orbit = Shot.orbit(focus, 200, facing, facing + 360, 1.5, 1.5, 6.0, 6.0);
        shots.add(Shot.transition(start, orbit, 40));
        shots.add(orbit);
        shots.add(Shot.orbit(focus, 160, facing + 360, facing + 540, 1.5, 10.0, 6.0, 8.0));
        shots.add(Shot.orbit(focus, 100, facing + 540, facing + 540, 10.0, 18.0, 8.0, 5.0));
        Shot dive = Shot.orbit(focus, 140, facing + 540, facing + 720, 18.0, 0.3, 5.0, 2.2);
        shots.add(dive);
        shots.add(Shot.transition(dive.at(1.0), Shot.of(1, t -> start), 50));
        return shots;
    }

    private static CameraPose poseAt(List<Shot> shots, int tick) {
        int remaining = tick;
        for (Shot shot : shots) {
            if (remaining <= shot.ticks()) {
                return shot.at((double) remaining / shot.ticks());
            }
            remaining -= shot.ticks();
        }
        Shot last = shots.getLast();
        return last.at(1.0);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.release(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        this.release(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        this.release(event.getPlayer());
    }

    private record Session(FakeCamera camera, ScheduledTask task, Runnable restore) {
    }
}
