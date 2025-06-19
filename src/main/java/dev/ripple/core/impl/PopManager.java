package dev.ripple.core.impl;

import dev.ripple.Ripple;
import dev.ripple.mod.modules.Module;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.DeathEvent;
import dev.ripple.api.events.impl.PacketEvent;
import dev.ripple.api.events.impl.TotemEvent;
import dev.ripple.api.utils.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;

public class PopManager implements Wrapper {
    public PopManager() {
        Ripple.EVENT_BUS.subscribe(this);
    }

    public final HashMap<String, Integer> popContainer = new HashMap<>();
    public final ArrayList<PlayerEntity> deadPlayer = new ArrayList<>();

    public Integer getPop(String s) {
        return popContainer.getOrDefault(s, 0);
    }
    public void onUpdate() {
        if (Module.nullCheck()) return;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || !player.isDead()) {
                deadPlayer.remove(player);
                continue;
            }
            if (deadPlayer.contains(player)) {
                continue;
            }
            Ripple.EVENT_BUS.post(new DeathEvent(player));
            onDeath(player);
            deadPlayer.add(player);
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (Module.nullCheck()) return;
        if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
                Entity entity = packet.getEntity(mc.world);
                if(entity instanceof PlayerEntity player) {
                    onTotemPop(player);
                }
            }
        }
    }

    public void onDeath(PlayerEntity player) {
        popContainer.remove(player.getName().getString());
    }

    public void onTotemPop(PlayerEntity player) {
        int l_Count = 1;
        if (popContainer.containsKey(player.getName().getString())) {
            l_Count = popContainer.get(player.getName().getString());
            popContainer.put(player.getName().getString(), ++l_Count);
        } else {
            popContainer.put(player.getName().getString(), l_Count);
        }
        Ripple.EVENT_BUS.post(new TotemEvent(player));
    }

    public Formatting getFormat(String name) {
        if (getPop(name) > 6) {
            return Formatting.DARK_RED;
        } else if (getPop(name) > 3) {
            return null;
        } else if (getPop(name) == 0) {
            return null;
        } else {
            return Formatting.YELLOW;
        }
    }
}
