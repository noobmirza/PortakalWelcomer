package org.noobfly.portakalwelcomer.tour;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPlayerInventory;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Turdaki oyuncunun zırh ve elindeki eşyayı diğer oyunculara (ve kendi istemcisine) boş gösterir.
public final class EquipmentHider extends PacketListenerAbstract implements Listener {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HAND, EquipmentSlot.OFF_HAND,
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private final Plugin plugin;
    private static final int HOTBAR_FIRST_SLOT = 36;
    private static final int OFF_HAND_SLOT = 45;
    private static final int INVENTORY_OFF_HAND = 40;

    private final Set<Integer> hidden = ConcurrentHashMap.newKeySet();

    EquipmentHider(Plugin plugin) {
        super(PacketListenerPriority.HIGHEST);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (this.hidden.isEmpty()) {
            return;
        }
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_EQUIPMENT) {
            if (event.getPlayer() instanceof Player self && this.isHidden(self)) {
                this.emptyOwnHands(event);
            }
            return;
        }
        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(event);
        if (!this.hidden.contains(packet.getEntityId())
                || event.getPlayer() instanceof Player viewer && viewer.getEntityId() == packet.getEntityId()) {
            return;
        }
        for (Equipment equipment : packet.getEquipment()) {
            equipment.setItem(ItemStack.EMPTY);
        }
        event.markForReEncode(true);
    }

    private void emptyOwnHands(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
            WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event);
            if (packet.getWindowId() == 0 && packet.getSlot() >= HOTBAR_FIRST_SLOT && packet.getSlot() <= OFF_HAND_SLOT) {
                packet.setItem(ItemStack.EMPTY);
                event.markForReEncode(true);
            }
        } else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
            WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
            List<ItemStack> items = packet.getItems();
            if (packet.getWindowId() == 0 && items.size() > OFF_HAND_SLOT) {
                List<ItemStack> copy = new ArrayList<>(items);
                for (int slot = HOTBAR_FIRST_SLOT; slot <= OFF_HAND_SLOT; slot++) {
                    copy.set(slot, ItemStack.EMPTY);
                }
                packet.setItems(copy);
                event.markForReEncode(true);
            }
        } else if (event.getPacketType() == PacketType.Play.Server.SET_PLAYER_INVENTORY) {
            WrapperPlayServerSetPlayerInventory packet = new WrapperPlayServerSetPlayerInventory(event);
            if (packet.getSlot() <= 8 || packet.getSlot() == INVENTORY_OFF_HAND) {
                packet.setStack(ItemStack.EMPTY);
                event.markForReEncode(true);
            }
        }
    }

    private static void sendEmptyHands(Player player) {
        for (int slot = HOTBAR_FIRST_SLOT; slot <= OFF_HAND_SLOT; slot++) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                    new WrapperPlayServerSetSlot(0, 0, slot, ItemStack.EMPTY));
        }
    }

    private boolean isHidden(Player player) {
        return this.hidden.contains(player.getEntityId());
    }

    public void hide(Player player) {
        this.hidden.add(player.getEntityId());
        this.sendToOthers(player, empty());
        sendEmptyHands(player);
    }

    public void show(Player player) {
        if (!this.hidden.remove(player.getEntityId()) || !player.isOnline()) {
            return;
        }
        EntityEquipment equipment = player.getEquipment();
        Map<EquipmentSlot, org.bukkit.inventory.ItemStack> real = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : SLOTS) {
            real.put(slot, equipment.getItem(slot));
        }
        this.sendToOthers(player, real);
        player.updateInventory();
    }

    private void sendToOthers(Player player, Map<EquipmentSlot, org.bukkit.inventory.ItemStack> items) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer != player && viewer.getWorld().equals(player.getWorld())) {
                viewer.sendEquipmentChange(player, items);
            }
        }
    }

    private static Map<EquipmentSlot, org.bukkit.inventory.ItemStack> empty() {
        Map<EquipmentSlot, org.bukkit.inventory.ItemStack> empty = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : SLOTS) {
            empty.put(slot, new org.bukkit.inventory.ItemStack(Material.AIR));
        }
        return empty;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (event.getEntity() instanceof Player player && this.isHidden(player)) {
            player.getScheduler().runDelayed(this.plugin, t -> {
                if (this.isHidden(player)) {
                    this.sendToOthers(player, empty());
                    sendEmptyHands(player);
                }
            }, null, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTrack(PlayerTrackEntityEvent event) {
        if (event.getEntity() instanceof Player player && this.isHidden(player)) {
            Player viewer = event.getPlayer();
            viewer.getScheduler().runDelayed(this.plugin, t -> {
                if (this.isHidden(player)) {
                    viewer.sendEquipmentChange(player, empty());
                }
            }, null, 1L);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent event) {
        if (this.isHidden(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (this.isHidden(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
