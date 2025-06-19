package dev.ripple.mod.commands.impl;

import dev.ripple.core.impl.CommandManager;
import dev.ripple.mod.commands.Command;
import dev.ripple.mod.modules.impl.client.ClientSetting;
import dev.ripple.mod.modules.impl.combat.AutoLog;
import dev.ripple.mod.modules.impl.misc.AutoEZ;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CoordsCommand extends Command {
    public CoordsCommand() {
        super("coords", "player");
    }

    @Override
    public void runCommand(String[] parameters) {
        if (!ClientSetting.INSTANCE.coordsCommand.getValue()) {
            CommandManager.sendChatMessage("§4For your safety, this command is disabled!");
            CommandManager.sendChatMessage("You can change this in ClientSetting.");
            return;
        }
        if (parameters.length != 1) {
            sendUsage();
            return;
        }
        if (Objects.equals(parameters[0], mc.player.getName().toString())) {
            CommandManager.sendChatMessage("§4The target player can not be yourself!");
            return;
        }
        try {
            if (CoordsCommand.getNames(mc.world.getPlayers()).contains(parameters[0])) {
                mc.getNetworkHandler().sendChatCommand("msg " + parameters[0]
                        + " X: " + mc.player.getBlockX()
                        + " Y: " + mc.player.getBlockY()
                        + " Z: " + mc.player.getBlockZ()
                        + " Dimension: " + AutoLog.INSTANCE.getDimension(mc.world)
                        + (ClientSetting.INSTANCE.randomEnd.getValue() ?
                        "(" + (ClientSetting.INSTANCE.randomAmount.getValueInt() < 1 ?
                                "-" : AutoEZ.INSTANCE.generateRandomString(ClientSetting.INSTANCE.randomAmount.getValueInt())) + ")" : ""));
            } else {
                CommandManager.sendChatMessage("§4Player not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] getAutocorrect(int count, List<String> seperated) {
        return getNames(mc.world.getPlayers()).toArray(new String[]{});
    }

    public static List<String> getNames(List<AbstractClientPlayerEntity> playerEntities) {
        List<String> temp = new ArrayList<>();
        for (AbstractClientPlayerEntity player : playerEntities) {
            temp.add(player.getName().getString());
        }
        return temp;
    }
}
