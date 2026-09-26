package org.noobfly.portakalwelcomer.camera;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.noobfly.portakalwelcomer.tour.TourManager;
import org.noobfly.portakalwelcomer.util.ChatStyle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class DeathCamManager implements Listener {
    private static final String CONFIG = "olum-kamerasi";
    private static final int INTERPOLATION_TICKS = 3;
    private static final double FOCUS_HEIGHT = 0.6;
    private static final double RADIUS = 4.5;
    private static final double MIN_RADIUS = 0.8;
    private static final double WALL_MARGIN = 0.4;
    private static final double PULL_OUT = 0.12;
    private static final float MIN_PITCH = 5.0F;
    private static final float MAX_PITCH = 80.0F;
    private static final int FLAGS_INDEX = 0;
    private static final byte INVISIBLE = 0x20;
    private static final int HEALTH_INDEX = 9;
    private static final float CLIENT_HEALTH = 1.0F;

    private final JavaPlugin plugin;
    private final TourManager tourManager;
    private final NamespacedKey reachKey;
    private final Method getHandle;
    private final Field deathTime;
    private final Map<UUID, Scene> scenes = new ConcurrentHashMap<>();
    private final Set<UUID> screenOff = ConcurrentHashMap.newKeySet();
    private final SelfInteractGuard interactGuard = new SelfInteractGuard(this::isActive);
    private final PacketListenerAbstract input = new PacketListenerAbstract(PacketListenerPriority.LOWEST) {
        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            DeathCamManager.this.onClientPacket(event);
        }

        @Override
        public void onPacketSend(PacketSendEvent event) {
            if (DeathCamManager.this.scenes.isEmpty()) {
                return;
            }
            if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
                DeathCamManager.this.rewriteOwnMetadata(event);
            } else if (event.getPacketType() == PacketType.Play.Server.UPDATE_HEALTH) {
                DeathCamManager.this.rewriteOwnHealth(event);
            }
        }
    };

    public DeathCamManager(JavaPlugin plugin, TourManager tourManager) {
        this.plugin = plugin;
        this.tourManager = tourManager;
        this.reachKey = new NamespacedKey(plugin, "olum_menzil");
        Method handle = null;
        Field field = null;
        try {
            handle = Bukkit.getServer().getClass().getClassLoader()
                    .loadClass("org.bukkit.craftbukkit.entity.CraftPlayer").getMethod("getHandle");
            for (Class<?> type = handle.getReturnType(); type != null && field == null; type = type.getSuperclass()) {
                try {
                    field = type.getDeclaredField("deathTime");
                    field.setAccessible(true);
                } catch (NoSuchFieldException ignored) {
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            plugin.getLogger().log(Level.WARNING, "Ölüm kamerası kapalı: CraftPlayer.getHandle bulunamadı", e);
        }
        if (field == null) {
            plugin.getLogger().warning("Ölüm kamerası kapalı: LivingEntity.deathTime bulunamadı");
        }
        this.getHandle = field != null ? handle : null;
        this.deathTime = field;
        PacketEvents.getAPI().getEventManager().registerListener(this.interactGuard);
        PacketEvents.getAPI().getEventManager().registerListener(this.input);
    }

    public boolean isEnabled() {
        return this.plugin.getConfig().getBoolean(CONFIG + ".acik", true);
    }

    public boolean toggle() {
        boolean on = !this.isEnabled();
        this.plugin.getConfig().set(CONFIG + ".acik", on);
        this.plugin.saveConfig();
        return on;
    }

    public boolean isActive(Player player) {
        return this.scenes.containsKey(player.getUniqueId());
    }

    private int durationTicks() {
        return Math.max(1, this.plugin.getConfig().getInt(CONFIG + ".sure-saniye", 7)) * 20;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (this.deathTime == null || !this.isEnabled() || isBedrock(player) || this.tourManager.isOnTour(player)
                || this.isActive(player)) {
            return;
        }
        Component message = event.deathMessage();
        Scene scene = new Scene(message, player.getLocation());
        scene.survivedTicks = player.getStatistic(Statistic.TIME_SINCE_DEATH);
        scene.items = event.getKeepInventory() ? "Korundu." : event.getDrops().isEmpty() ? "Yoktu." : "Yere düştü!";
        this.scenes.put(player.getUniqueId(), scene);
        send(player, new WrapperPlayServerChangeGameState(WrapperPlayServerChangeGameState.Reason.ENABLE_RESPAWN_SCREEN, 1.0F));
        this.screenOff.add(player.getUniqueId());
        player.getScheduler().run(this.plugin, t -> this.start(player, scene), () -> this.scenes.remove(player.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRespawn(PlayerRespawnEvent event) {
        this.finish(event.getPlayer(), false);
    }

    @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        if (this.screenOff.remove(player.getUniqueId())) {
            boolean immediate = Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRules.IMMEDIATE_RESPAWN));
            send(player, new WrapperPlayServerChangeGameState(WrapperPlayServerChangeGameState.Reason.ENABLE_RESPAWN_SCREEN, immediate ? 1.0F : 0.0F));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.screenOff.remove(event.getPlayer().getUniqueId());
        this.finish(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (this.isActive(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && this.isActive(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player && this.isActive(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (this.isActive(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (this.isActive(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private void onClientPacket(PacketReceiveEvent event) {
        if (this.scenes.isEmpty() || !(event.getPlayer() instanceof Player player)) {
            return;
        }
        Scene scene = this.scenes.get(player.getUniqueId());
        if (scene == null) {
            return;
        }
        if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
            if (new WrapperPlayClientClientStatus(event).getAction() == WrapperPlayClientClientStatus.Action.PERFORM_RESPAWN) {
                event.setCancelled(true);
            }
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION
                || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);
            if (packet.hasRotationChanged()) {
                scene.yaw = packet.getLocation().getYaw();
                scene.pitch = packet.getLocation().getPitch();
            }
        }
    }

    private void start(Player player, Scene scene) {
        if (this.scenes.get(player.getUniqueId()) != scene || !player.isOnline()) {
            return;
        }
        try {
            this.show(player, scene);
        } catch (RuntimeException e) {
            this.plugin.getLogger().log(Level.WARNING, "Ölüm kamerası başlatılamadı, " + player.getName() + " hemen doğuyor", e);
            this.finish(player, true);
        }
    }

    private void show(Player player, Scene scene) {
        Location body = scene.body;
        scene.focus = body.toVector().add(new Vector(0.0, FOCUS_HEIGHT, 0.0));
        this.keepBody(player);
        if (this.scenes.get(player.getUniqueId()) != scene) {
            return;
        }
        this.sendOwnFlags(player, true);
        this.tourManager.equipmentHider().hide(player);
        Reach.disable(player, this.reachKey);

        scene.camera = new FakeCamera(player, CameraPose.of(player.getEyeLocation()), INTERPOLATION_TICKS);
        scene.vehicle = Bukkit.getUnsafe().nextEntityId(player.getWorld());
        send(player, new WrapperPlayServerSpawnEntity(scene.vehicle, Optional.of(UUID.randomUUID()), EntityTypes.ITEM_DISPLAY,
                new Vector3d(body.getX(), body.getY(), body.getZ()), 0.0F, 0.0F, 0.0F, 0, Optional.empty()));
        send(player, new WrapperPlayServerSetPassengers(scene.vehicle, new int[] {player.getEntityId()}));

        int duration = this.durationTicks();
        String sound = this.plugin.getConfig().getString(CONFIG + ".ses", "portakalsounds:olumcani");
        if (sound != null && !sound.isBlank()) {
            player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 1.0F, 1.0F);
        }
        if (this.plugin.getConfig().getBoolean(CONFIG + ".ozel-ekran", true)) {
            Location at = scene.body;
            List<String[]> details = List.of(
                    new String[] {"Hayatta kaldığın süre", formatTicks(scene.survivedTicks)},
                    new String[] {"Ölüm konumu", at.getBlockX() + ", " + at.getBlockY() + ", " + at.getBlockZ()},
                    new String[] {"Eşyaların", scene.items},
                    new String[] {"Toplam ölüm", String.valueOf(player.getStatistic(Statistic.DEATHS))});
            scene.screen = DeathScreen.data(scene.message, details);
            player.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ZERO, Duration.ofMillis(duration * 50L + 10_000L), Duration.ZERO));
            player.sendTitlePart(TitlePart.SUBTITLE, Component.empty());
            player.sendTitlePart(TitlePart.TITLE, DeathScreen.render(scene.screen, 0, duration));
        } else {
            player.showTitle(Title.title(ChatStyle.title(ChatStyle.Kind.ERROR, "Öldün"), scene.message != null ? scene.message : Component.empty(),
                    Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(Math.max(0, duration * 50L - 750)), Duration.ofMillis(500))));
        }
        scene.task = player.getScheduler().runAtFixedRate(this.plugin, t -> this.tick(player, scene, duration), null, 1L, 1L);
    }

    private void tick(Player player, Scene scene, int duration) {
        try {
            this.step(player, scene, duration);
        } catch (RuntimeException e) {
            this.plugin.getLogger().log(Level.WARNING, "Ölüm kamerası hata verdi, " + player.getName() + " hemen doğuyor", e);
            this.finish(player, true);
        }
    }

    private void step(Player player, Scene scene, int duration) {
        scene.tick++;
        if (scene.tick >= duration || !player.isDead()) {
            this.finish(player, true);
            return;
        }
        this.keepBody(player);
        if (this.scenes.get(player.getUniqueId()) != scene) {
            return;
        }
        if (scene.screen != null) {
            player.sendTitlePart(TitlePart.TITLE, DeathScreen.render(scene.screen, scene.tick, duration));
        } else if (scene.tick % 20 == 1) {
            int seconds = (duration - scene.tick + 19) / 20;
            player.sendActionBar(ChatStyle.bar(ChatStyle.Kind.INFO, "Yeniden doğmana <v>{}</v> saniye", seconds));
        }

        float yaw = scene.yaw;
        float pitch = Math.max(MIN_PITCH, Math.min(MAX_PITCH, scene.pitch));
        Vector back = new CameraPose(0.0, 0.0, 0.0, yaw, pitch).direction().multiply(-1.0);

        double wanted = RADIUS;
        RayTraceResult hit = scene.body.getWorld().rayTraceBlocks(scene.focus.toLocation(scene.body.getWorld()), back,
                RADIUS + WALL_MARGIN, FluidCollisionMode.NEVER, true);
        if (hit != null) {
            wanted = Math.max(MIN_RADIUS, hit.getHitPosition().distance(scene.focus) - WALL_MARGIN);
        }
        scene.distance = wanted < scene.distance ? wanted : scene.distance + (wanted - scene.distance) * PULL_OUT;

        Vector position = scene.focus.clone().add(back.multiply(scene.distance));
        scene.camera.moveTo(new CameraPose(position.getX(), position.getY(), position.getZ(), yaw, pitch));
    }

    private void keepBody(Player player) {
        try {
            this.deathTime.setInt(this.getHandle.invoke(player), 0);
        } catch (ReflectiveOperationException | RuntimeException e) {
            this.plugin.getLogger().log(Level.WARNING, "deathTime sıfırlanamadı, " + player.getName() + " hemen doğuyor", e);
            this.finish(player, true);
        }
    }

    private void finish(Player player, boolean respawn) {
        Scene scene = this.scenes.remove(player.getUniqueId());
        if (scene == null) {
            return;
        }
        try {
            if (scene.task != null) {
                scene.task.cancel();
            }
            if (scene.camera != null) {
                scene.camera.remove();
                if (player.isOnline()) {
                    send(player, new WrapperPlayServerDestroyEntities(scene.vehicle));
                    player.resetTitle();
                    this.sendOwnFlags(player, player.isInvisible());
                }
            }
            this.tourManager.equipmentHider().show(player);
        } catch (RuntimeException e) {
            this.plugin.getLogger().log(Level.WARNING, "Ölüm kamerası kapatılırken hata: " + player.getName(), e);
        } finally {
            Reach.restore(player, this.reachKey);
            if (respawn && player.isOnline() && player.isDead()) {
                player.spigot().respawn();
            }
        }
    }

    public boolean release(Player player) {
        if (!this.isActive(player)) {
            return false;
        }
        this.finish(player, true);
        return true;
    }

    public void shutdown() {
        boolean stopping = Bukkit.isStopping();
        for (UUID id : List.copyOf(this.scenes.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                this.finish(player, !stopping);
            } else {
                Scene scene = this.scenes.remove(id);
                if (scene != null && scene.task != null) {
                    scene.task.cancel();
                }
            }
        }
        PacketEvents.getAPI().getEventManager().unregisterListener(this.interactGuard);
        PacketEvents.getAPI().getEventManager().unregisterListener(this.input);
    }

    private void rewriteOwnMetadata(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) {
            return;
        }
        Scene scene = this.scenes.get(viewer.getUniqueId());
        if (scene == null) {
            return;
        }
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
        if (packet.getEntityId() != viewer.getEntityId()) {
            return;
        }
        List<EntityData<?>> data = new ArrayList<>(packet.getEntityMetadata());
        boolean changed = false;
        for (int i = 0; i < data.size(); i++) {
            EntityData<?> entry = data.get(i);
            if (entry.getIndex() == FLAGS_INDEX && scene.camera != null && entry.getValue() instanceof Byte flags) {
                data.set(i, new EntityData<>(FLAGS_INDEX, EntityDataTypes.BYTE, (byte) (flags | INVISIBLE)));
                changed = true;
            } else if (entry.getIndex() == HEALTH_INDEX && entry.getValue() instanceof Float health && health <= 0.0F) {
                data.set(i, new EntityData<>(HEALTH_INDEX, EntityDataTypes.FLOAT, CLIENT_HEALTH));
                changed = true;
            }
        }
        if (changed) {
            packet.setEntityMetadata(data);
            event.markForReEncode(true);
        }
    }

    private void rewriteOwnHealth(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player viewer) || !this.scenes.containsKey(viewer.getUniqueId())) {
            return;
        }
        WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth(event);
        if (packet.getHealth() <= 0.0F) {
            packet.setHealth(CLIENT_HEALTH);
            event.markForReEncode(true);
        }
    }

    private static void sendOwnFlags(Player player, boolean invisible) {
        byte flags = 0;
        if (player.getFireTicks() > 0) {
            flags |= 0x01;
        }
        if (player.isSneaking()) {
            flags |= 0x02;
        }
        if (player.isSprinting()) {
            flags |= 0x08;
        }
        if (player.isSwimming()) {
            flags |= 0x10;
        }
        if (invisible) {
            flags |= INVISIBLE;
        }
        if (player.isGlowing()) {
            flags |= 0x40;
        }
        if (player.isGliding()) {
            flags |= (byte) 0x80;
        }
        send(player, new WrapperPlayServerEntityMetadata(player.getEntityId(), List.of(new EntityData<>(FLAGS_INDEX, EntityDataTypes.BYTE, flags))));
    }

    private static String formatTicks(int ticks) {
        int seconds = Math.max(0, ticks / 20);
        if (seconds < 60) {
            return seconds + " sn";
        }
        if (seconds < 3600) {
            return seconds / 60 + " dk " + seconds % 60 + " sn";
        }
        return seconds / 3600 + " sa " + seconds / 60 % 60 + " dk";
    }

    private static boolean isBedrock(Player player) {
        return player.getUniqueId().getMostSignificantBits() == 0L;
    }

    private static void send(Player player, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    private static final class Scene {
        final Component message;
        final Location body;
        int survivedTicks;
        String items;
        DeathScreen.Data screen;
        Vector focus;
        FakeCamera camera;
        int vehicle;
        ScheduledTask task;
        int tick;
        double distance = 0.3;
        volatile float yaw;
        volatile float pitch;

        Scene(Component message, Location body) {
            this.message = message;
            this.body = body.clone();
            this.yaw = body.getYaw();
            this.pitch = body.getPitch();
        }
    }
}
