package org.noobfly.portakalwelcomer.tour;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.noobfly.portakalwelcomer.camera.CameraPose;
import org.noobfly.portakalwelcomer.camera.FakeCamera;
import org.noobfly.portakalwelcomer.camera.Reach;
import org.noobfly.portakalwelcomer.camera.Shot;
import org.noobfly.portakalwelcomer.util.ChatStyle;

import java.util.List;

// Tek oyuncunun turu: kamera sahte entity, gerçek oyuncu görünmez, donuk ve ölümsüz; kamera uzak duraklara kesmeyle gider.
final class TourSession {
    private static final double EYE_HEIGHT = 1.62;
    private static final int SEND_INTERVAL_TICKS = 2;
    private static final int INTERPOLATION_TICKS = SEND_INTERVAL_TICKS + 1;
    private static final int FOLLOW_INTERVAL_TICKS = 10;
    private static final int MIN_STOP_TICKS = 30;
    private static final double HURRY_TICKS = 20.0;
    // Bu mesafeden uzak duraklara uçulmaz, kesilir; duvarların içinden geçmesin.
    private static final double CUT_DISTANCE = 60.0;
    private static final int CUT_HOLD_TICKS = 20;
    private static final int FALLBACK_PAGE_TICKS = 100;

    private enum State { HOP, ARRIVING, STOP, FINISHING, DONE }

    private final Plugin plugin;
    private final TourManager manager;
    private final Player player;
    private final List<TourStop> stops;
    private final Location origin;
    private final CameraPose originPose;
    private final FakeCamera camera;
    private final Saved saved;
    private final NamespacedKey reachKey;
    private ScheduledTask task;

    private State state = State.HOP;
    private int next;
    private int tick;
    private Shot hop;
    private StopShot stopShot;
    private boolean cut;
    private int hold;
    private double clock;
    private double rate;
    private boolean hurry;
    private volatile CameraPose pose;
    private volatile boolean dialogueStarted;
    private volatile boolean dialogueDone;
    private int dialogueStartTick;
    private int fallbackEndTick = -1;

    TourSession(Plugin plugin, TourManager manager, Player player, List<TourStop> stops, Location origin, boolean resume) {
        this.plugin = plugin;
        this.manager = manager;
        this.player = player;
        this.stops = stops;
        this.origin = origin.clone();
        this.originPose = CameraPose.of(player.getEyeLocation());
        this.pose = this.originPose;

        this.saved = resume ? Saved.defaults(player) : Saved.of(player);
        this.reachKey = new NamespacedKey(plugin, "tanitim_menzil");
        Reach.disable(player, this.reachKey);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, false));
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setWalkSpeed(0.0F);
        player.setFlySpeed(0.0F);
        player.setInvulnerable(true);
        player.setCollidable(false);
        manager.equipmentHider().hide(player);

        this.camera = new FakeCamera(player, this.originPose, INTERPOLATION_TICKS);
        this.beginHop(0);
        this.task = player.getScheduler().runAtFixedRate(plugin, t -> this.tick(), null, 1L, 1L);
    }

    Location origin() {
        return this.origin;
    }

    CameraPose cameraPose() {
        return this.pose;
    }

    private void tick() {
        if (!this.player.isOnline()) {
            this.cancelTask();
            return;
        }
        this.tick++;
        switch (this.state) {
            case HOP -> {
                double duration = this.hop.ticks();
                this.pose = this.hop.at(Math.min(1.0, this.tick / duration));
                if (this.tick % SEND_INTERVAL_TICKS == 0) {
                    this.camera.moveTo(this.hop.at(Math.min(1.0, (this.tick + INTERPOLATION_TICKS) / duration)));
                }
                if (this.tick % FOLLOW_INTERVAL_TICKS == 0) {
                    this.player.teleportAsync(this.at(this.pose.position().subtract(new Vector(0.0, EYE_HEIGHT, 0.0))));
                }
                if (this.tick >= duration) {
                    this.arrive();
                }
            }
            case STOP -> {
                int end = Math.max(MIN_STOP_TICKS, this.hold + this.stops.get(this.next).minTicks());
                if (!this.hurry && this.dialogueOver()) {
                    this.hurry = true;
                    this.rate = Math.max(1.0, (end - this.clock) / HURRY_TICKS);
                }
                this.clock += this.rate;
                this.pose = this.stopShot.at(Math.max(0.0, this.clock - this.hold));
                if (this.tick % SEND_INTERVAL_TICKS == 0) {
                    this.camera.moveTo(this.stopShot.at(Math.max(0.0, this.clock + INTERPOLATION_TICKS * this.rate - this.hold)));
                }
                if (this.hurry && this.clock >= end) {
                    this.beginHop(this.next + 1);
                }
            }
            case ARRIVING, FINISHING -> {
            }
            case DONE -> this.cancelTask();
        }
    }

    private boolean dialogueOver() {
        if (!this.dialogueStarted) {
            return false;
        }
        if (this.fallbackEndTick >= 0) {
            return this.tick >= this.fallbackEndTick;
        }
        return this.dialogueDone || (this.tick - this.dialogueStartTick > 20 && !HubDialogue.isActive(this.player));
    }

    private void beginHop(int index) {
        this.next = index;
        this.tick = 0;
        if (index >= this.stops.size()) {
            this.arrive();
            return;
        }
        this.stopShot = this.stops.get(index).shot().apply(this.pose.position());
        CameraPose target = this.stopShot.at(0);
        this.cut = index == 0 || this.pose.position().distance(target.position()) > CUT_DISTANCE;
        if (this.cut) {
            this.arrive();
            return;
        }
        this.state = State.HOP;
        this.hop = hop(this.pose, target);
    }

    private void arrive() {
        if (this.next >= this.stops.size()) {
            this.state = State.FINISHING;
            this.player.teleportAsync(this.origin).thenRun(() ->
                    this.player.getScheduler().run(this.plugin, t -> this.manager.finish(this.player, true), null));
            return;
        }
        this.state = State.ARRIVING;
        TourStop stop = this.stops.get(this.next);
        this.player.teleportAsync(this.at(stop.anchor())).thenRun(() ->
                this.player.getScheduler().run(this.plugin, t -> this.startStop(stop), null));
    }

    private void startStop(TourStop stop) {
        if (this.state != State.ARRIVING) {
            return;
        }
        this.state = State.STOP;
        this.tick = 0;
        this.hold = 0;
        this.clock = 0.0;
        this.rate = 1.0;
        this.hurry = false;
        if (this.cut) {
            this.camera.cutTo(this.stopShot.at(0));
            this.hold = CUT_HOLD_TICKS;
        }
        this.pose = this.stopShot.at(0);
        this.dialogueDone = false;
        this.dialogueStarted = true;
        this.dialogueStartTick = 0;
        this.fallbackEndTick = -1;
        if (!HubDialogue.start(this.player, stop.speaker(), stop.portrait(), stop.pages(), stop.manual(),
                p -> this.dialogueDone = true)) {
            for (String page : stop.pages()) {
                this.player.sendMessage(ChatStyle.info("<a>" + stop.speaker() + "</a> <d>»</d> "
                        + page.replace("{", "<v>").replace("}", "</v>")));
            }
            this.fallbackEndTick = stop.pages().size() * FALLBACK_PAGE_TICKS;
        }
    }

    void end() {
        this.state = State.DONE;
        this.cancelTask();
        this.camera.remove();
        this.saved.restore(this.player);
        Reach.restore(this.player, this.reachKey);
        this.manager.equipmentHider().show(this.player);
    }

    private void cancelTask() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    private Location at(Vector feet) {
        Location current = this.player.getLocation();
        return new Location(current.getWorld(), feet.getX(), feet.getY(), feet.getZ(), current.getYaw(), current.getPitch());
    }

    private static Shot hop(CameraPose from, CameraPose to) {
        Vector p0 = from.position();
        Vector p1 = to.position();
        double distance = p0.distance(p1);
        int ticks = (int) Math.max(50, Math.min(240, 30 + distance * 1.6));
        double lift = Math.max(1.5, Math.min(30.0, distance * 0.15));
        Vector control = p0.clone().add(p1).multiply(0.5).add(new Vector(0.0, lift, 0.0));
        return Shot.of(ticks, t -> {
            double e = Shot.ease(t);
            double u = 1.0 - e;
            Vector pos = p0.clone().multiply(u * u)
                    .add(control.clone().multiply(2.0 * u * e))
                    .add(p1.clone().multiply(e * e));
            CameraPose rotation = CameraPose.lerp(from, to, e);
            return new CameraPose(pos.getX(), pos.getY(), pos.getZ(), rotation.yaw(), rotation.pitch());
        });
    }

    private record Saved(boolean allowFlight, boolean flying, float walkSpeed, float flySpeed, boolean collidable) {
        static Saved of(Player player) {
            return new Saved(player.getAllowFlight(), player.isFlying(),
                    player.getWalkSpeed() > 0.0F ? player.getWalkSpeed() : 0.2F,
                    player.getFlySpeed() > 0.0F ? player.getFlySpeed() : 0.1F,
                    player.isCollidable());
        }

        static Saved defaults(Player player) {
            boolean flight = switch (player.getGameMode()) {
                case CREATIVE, SPECTATOR -> true;
                default -> false;
            };
            return new Saved(flight, false, 0.2F, 0.1F, true);
        }

        void restore(Player player) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.setWalkSpeed(this.walkSpeed);
            player.setFlySpeed(this.flySpeed);
            player.setAllowFlight(this.allowFlight);
            player.setFlying(this.allowFlight && this.flying);
            player.setInvulnerable(false);
            player.setCollidable(this.collidable);
        }
    }
}
