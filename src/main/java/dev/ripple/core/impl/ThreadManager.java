package dev.ripple.core.impl;

import dev.ripple.api.utils.world.BlockUtil;
import dev.ripple.Ripple;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.eventbus.EventPriority;
import dev.ripple.api.events.impl.TickEvent;
import dev.ripple.mod.modules.impl.render.PlaceRender;

public class ThreadManager {
    public static ClientService clientService;

    public ThreadManager() {
        Ripple.EVENT_BUS.subscribe(this);
        clientService = new ClientService();
        clientService.setName("RippleClientService");
        clientService.setDaemon(true);
        clientService.start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(TickEvent event) {
        if (event.isPre()) {
            if (!clientService.isAlive()) {
                clientService = new ClientService();
                clientService.setName("RippleClientService");
                clientService.setDaemon(true);
                clientService.start();
            }
            BlockUtil.placedPos.forEach(pos -> PlaceRender.renderMap.put(pos, PlaceRender.INSTANCE.create(pos)));
            BlockUtil.placedPos.clear();
            Ripple.SERVER.onUpdate();
            Ripple.PLAYER.onUpdate();
            Ripple.MODULE.onUpdate();
            Ripple.GUI.onUpdate();
            Ripple.POP.onUpdate();
        }
    }

    public static class ClientService extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (Ripple.MODULE != null) {
                        Ripple.MODULE.onThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
