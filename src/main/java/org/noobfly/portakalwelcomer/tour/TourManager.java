package org.noobfly.portakalwelcomer.tour;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.noobfly.portakalwelcomer.camera.SelfInteractGuard;
import org.noobfly.portakalwelcomer.util.ChatStyle;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class TourManager implements Listener {
    private static final long JOIN_DELAY_TICKS = 40L;
    private static final long JOIN_START_TICKS = 60L;
    private static final long PACK_TIMEOUT_TICKS = 20L * 60L;

    private final JavaPlugin plugin;
    private final Map<UUID, TourSession> sessions = new ConcurrentHashMap<>();
    private final Set<UUID> pending = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Location> returnPoints = new ConcurrentHashMap<>();
    private final Set<UUID> waitingForPack = ConcurrentHashMap.newKeySet();
    private final Set<UUID> packLoading = ConcurrentHashMap.newKeySet();
    private final File pendingFile;
    private final TourSoundListener soundListener = new TourSoundListener(this);
    private final SelfInteractGuard interactGuard = new SelfInteractGuard(player -> this.session(player) != null);
    private final EquipmentHider equipmentHider;

    public TourManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pendingFile = new File(plugin.getDataFolder(), "bekleyen-tanitim.yml");
        PacketEvents.getAPI().getEventManager().registerListener(this.soundListener);
        PacketEvents.getAPI().getEventManager().registerListener(this.interactGuard);
        this.equipmentHider = new EquipmentHider(plugin);
        PacketEvents.getAPI().getEventManager().registerListener(this.equipmentHider);
        plugin.getServer().getPluginManager().registerEvents(this.equipmentHider, plugin);
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.pendingFile);
        for (String id : yaml.getStringList("oyuncular")) {
            try {
                UUID uuid = UUID.fromString(id);
                this.pending.add(uuid);
                Location home = yaml.getLocation("donus." + id);
                if (home != null && home.getWorld() != null) {
                    this.returnPoints.put(uuid, home);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    TourSession session(Player player) {
        return this.sessions.get(player.getUniqueId());
    }

    public EquipmentHider equipmentHider() {
        return this.equipmentHider;
    }

    public boolean isOnTour(Player player) {
        return this.sessions.containsKey(player.getUniqueId());
    }

    public void start(Player player) {
        World world = Bukkit.getWorld(TourRoute.WORLD);
        if (world == null) {
            this.plugin.getLogger().warning("Tanıtım başlatılamadı: '" + TourRoute.WORLD + "' dünyası yok.");
            return;
        }
        UUID id = player.getUniqueId();
        TourSession running = this.sessions.get(id);
        boolean resume = this.pending.contains(id) && running == null;
        Location home = running != null ? running.origin() : this.returnPoints.get(id);
        this.stop(player);
        if (home == null || !world.equals(home.getWorld())) {
            home = player.getWorld().equals(world) && !resume ? player.getLocation() : lobbySpawn(world);
        }
        this.pending.add(id);
        this.returnPoints.put(id, home);
        this.savePending();

        Location origin = home;
        if (player.getWorld().equals(world) && !resume) {
            this.begin(player, origin, resume);
            return;
        }
        player.teleportAsync(origin).thenRun(() ->
                player.getScheduler().run(this.plugin, t -> this.begin(player, origin, resume), null));
    }

    private void begin(Player player, Location origin, boolean resume) {
        if (!player.isOnline() || this.sessions.containsKey(player.getUniqueId())) {
            return;
        }
        this.sessions.put(player.getUniqueId(),
                new TourSession(this.plugin, this, player, TourRoute.stops(), origin, resume));
    }

    private static Location lobbySpawn(World world) {
        Location spawn = hubLobbySpawn();
        return spawn != null && world.equals(spawn.getWorld()) ? spawn : world.getSpawnLocation();
    }

    public static Location hubLobbySpawn() {
        Plugin hub = Bukkit.getPluginManager().getPlugin("portakalhub");
        if (hub != null && hub.isEnabled()) {
            try {
                if (hub.getClass().getMethod("getLobbySpawn").invoke(hub) instanceof Location spawn && spawn.getWorld() != null) {
                    return spawn;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    void finish(Player player, boolean completed) {
        TourSession session = this.sessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }
        session.end();
        if (completed) {
            this.pending.remove(player.getUniqueId());
            this.returnPoints.remove(player.getUniqueId());
            this.savePending();
            player.sendMessage(ChatStyle.success("Tanıtım bitti. İyi oyunlar!"));
        }
    }

    public boolean stop(Player player) {
        TourSession session = this.sessions.remove(player.getUniqueId());
        if (session == null) {
            return false;
        }
        session.end();
        player.teleportAsync(session.origin());
        this.pending.remove(player.getUniqueId());
        this.returnPoints.remove(player.getUniqueId());
        this.savePending();
        return true;
    }

    public void shutdown() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this.soundListener);
        PacketEvents.getAPI().getEventManager().unregisterListener(this.interactGuard);
        for (UUID id : List.copyOf(this.sessions.keySet())) {
            Player player = Bukkit.getPlayer(id);
            TourSession session = this.sessions.remove(id);
            if (session != null && player != null) {
                session.end();
                player.teleport(session.origin());
            }
        }
        PacketEvents.getAPI().getEventManager().unregisterListener(this.equipmentHider);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (isBedrock(player)) {
            return;
        }
        boolean firstJoin = !player.hasPlayedBefore() && this.plugin.getConfig().getBoolean("tanitim.ilk-giriste", false);
        if (!firstJoin && !this.pending.contains(player.getUniqueId())) {
            return;
        }
        if (this.pending.add(player.getUniqueId())) {
            this.savePending();
        }
        UUID id = player.getUniqueId();
        this.waitingForPack.add(id);
        player.getScheduler().runDelayed(this.plugin, t -> {
            if (!this.packLoading.contains(id) && this.waitingForPack.remove(id)) {
                this.start(player);
            }
        }, null, JOIN_START_TICKS);
        player.getScheduler().runDelayed(this.plugin, t -> {
            this.packLoading.remove(id);
            if (this.waitingForPack.remove(id)) {
                this.start(player);
            }
        }, null, PACK_TIMEOUT_TICKS);
    }

    @EventHandler
    public void onPackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        switch (event.getStatus()) {
            case ACCEPTED, DOWNLOADED -> {
                if (this.waitingForPack.contains(player.getUniqueId())) {
                    this.packLoading.add(player.getUniqueId());
                }
                return;
            }
            default -> {
            }
        }
        this.packLoading.remove(player.getUniqueId());
        if (this.waitingForPack.remove(player.getUniqueId())) {
            player.getScheduler().runDelayed(this.plugin, t -> this.start(player), null, JOIN_DELAY_TICKS);
        }
    }

    private static boolean isBedrock(Player player) {
        return player.getUniqueId().getMostSignificantBits() == 0L;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.waitingForPack.remove(player.getUniqueId());
        this.packLoading.remove(player.getUniqueId());
        TourSession session = this.sessions.remove(player.getUniqueId());
        if (session != null) {
            session.end();
            player.teleport(session.origin());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!this.isOnTour(player)) {
            return;
        }
        String label = event.getMessage().substring(1).split(" ", 2)[0].toLowerCase();
        if (player.hasPermission("portakalwelcomer.admin")
                && (label.equals("pwc") || label.equals("portakalwelcomer") || label.endsWith(":portakalwelcomer"))) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(ChatStyle.error("Tanıtım sırasında komut kullanamazsın."));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!this.isOnTour(event.getPlayer())) {
            return;
        }
        World to = event.getTo().getWorld();
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN && to != null && to.getName().equals(TourRoute.WORLD)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (this.isOnTour(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (this.isOnTour(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (this.isOnTour(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && this.isOnTour(player)) {
            event.setCancelled(true);
        }
    }

    private void savePending() {
        YamlConfiguration yaml = new YamlConfiguration();
        Set<String> ids = new LinkedHashSet<>();
        this.pending.forEach(id -> ids.add(id.toString()));
        yaml.set("oyuncular", List.copyOf(ids));
        this.returnPoints.forEach((id, home) -> {
            if (this.pending.contains(id)) {
                yaml.set("donus." + id, home);
            }
        });
        try {
            yaml.save(this.pendingFile);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING, "bekleyen-tanitim.yml kaydedilemedi", e);
        }
    }
}
