package dev.ripple.mod.modules.impl.misc;

import dev.ripple.Ripple;
import dev.ripple.mod.modules.impl.client.ClientSetting;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.DeathEvent;
import dev.ripple.api.events.impl.TotemEvent;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.mod.modules.Module;
import net.minecraft.entity.player.PlayerEntity;

public class PopCounter extends Module {

    public static PopCounter INSTANCE;
    public final BooleanSetting unPop = add(new BooleanSetting("UnPopMsg", true));
    public final BooleanSetting clear = add(new BooleanSetting("DeathClear", true));

    public PopCounter() {
        super("PopCounter", "Counts players totem pops", Category.Misc);
        setChinese("图腾计数");
        INSTANCE = this;
    }

    @EventHandler
    public void onPlayerDeath(DeathEvent event) {
        PlayerEntity player = event.getPlayer();
        if (Ripple.POP.popContainer.containsKey(player.getName().getString())) {
            int l_Count = Ripple.POP.popContainer.get(player.getName().getString());
            if (l_Count == 1) {
                if (player.equals(mc.player)) {
                    sendMessage("§fYou§r died after popping " + "§f" + l_Count + "§r totem.", player.getId());
                } else {
                    sendMessage("§f" + player.getName().getString() + "§r died after popping " + "§f" + l_Count + "§r totem.", player.getId());
                }
            } else {
                if (player.equals(mc.player)) {
                    sendMessage("§fYou§r died after popping " + "§f" + l_Count + "§r totems.", player.getId());
                } else {
                    sendMessage("§f" + player.getName().getString() + "§r died after popping " + "§f" + l_Count + "§r totems.", player.getId());
                }
            }
        } else if (unPop.getValue()) {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r died.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + "§r died.", player.getId());
            }
        }
        if (clear.getValue()) Ripple.POP.popContainer.put(player.getName().toString(), 0);
    }

    @EventHandler
    public void onTotem(TotemEvent event) {
        PlayerEntity player = event.getPlayer();
        int l_Count = 1;
        if (Ripple.POP.popContainer.containsKey(player.getName().getString())) {
            l_Count = Ripple.POP.popContainer.get(player.getName().getString());
        }
        if (l_Count == 1) {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r popped " + "§f" + l_Count + "§r totem.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + " §rpopped " + "§f" + l_Count + "§r totems.", player.getId());
            }
        } else {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r popped " + "§f" + l_Count + "§r totem.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + " §rhas popped " + "§f" + l_Count + "§r totems.", player.getId());
            }
        }
    }

    public void sendMessage(String message, int id) {
        if (!nullCheck()) {
            if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
                CommandManager.sendChatMessageWidthId("§f[" + "§3" + getName() + "§f] " + message, id);
                return;
            }
            CommandManager.sendChatMessageWidthId(message, id);//"§6[!] " + message, id);
        }
    }
}

