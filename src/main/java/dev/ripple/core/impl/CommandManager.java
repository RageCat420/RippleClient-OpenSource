package dev.ripple.core.impl;

import dev.ripple.Ripple;
import dev.ripple.api.interfaces.IChatHudHook;
import dev.ripple.api.utils.Wrapper;
import dev.ripple.mod.commands.Command;
import dev.ripple.mod.commands.impl.*;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.impl.client.ClientSetting;
import net.minecraft.text.Text;

import java.util.HashMap;

public class CommandManager implements Wrapper {
    public static final String syncCode = "§)";
    private final HashMap<String, Command> commands = new HashMap<>();

    public CommandManager() {
        addCommand(new AimCommand());
        addCommand(new BindCommand());
        addCommand(new BindsCommand());
        addCommand(new ClipCommand());
        addCommand(new CoordsCommand());
        addCommand(new FriendCommand());
        addCommand(new XrayCommand());
        addCommand(new GamemodeCommand());
        addCommand(new IRCCommand());
        addCommand(new LoadCommand());
        addCommand(new PrefixCommand());
        addCommand(new RejoinCommand());
        addCommand(new ReloadCommand());
        addCommand(new ReloadAllCommand());
        addCommand(new SaveCommand());
        addCommand(new TeleportCommand());
        addCommand(new TCommand());
        addCommand(new ToggleCommand());
        addCommand(new TradeCommand());
        addCommand(new WatermarkCommand());
    }

    private void addCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public Command getCommandBySyntax(String string) {
        return this.commands.get(string);
    }

    public HashMap<String, Command> getCommands() {
        return this.commands;
    }

    public void command(String[] commandIn) {

        // Get the command from the user's message. (Index 0 is Username)
        Command command = commands.get(commandIn[0].substring(Ripple.PREFIX.length()).toLowerCase());

        // If the command does not exist, throw an error.
        if (command == null)
            sendChatMessage("§cInvalid Command");
        else {
            // Otherwise, create a new parameter list.
            String[] parameterList = new String[commandIn.length - 1];
            System.arraycopy(commandIn, 1, parameterList, 0, commandIn.length - 1);
            if (parameterList.length == 1 && parameterList[0].equals("help")) {
                command.sendUsage();
                return;
            }
            // Runs the command.
            command.runCommand(parameterList);
        }
    }
    public static void sendChatMessage(String message) {
        if (Module.nullCheck()) return;
        if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
            mc.inGameHud.getChatHud().addMessage(Text.of("§f[§b" + ClientSetting.INSTANCE.hackName.getValue() + "§f] " + message));
            return;
        }
        mc.inGameHud.getChatHud().addMessage(Text.of(syncCode + "§r" + ClientSetting.INSTANCE.hackName.getValue() + "§f " + message));
    }

    public static void sendChatMessageWidthId(String message, int id) {
        if (Module.nullCheck()) return;
        if (ClientSetting.INSTANCE.messageStyle.getValue() == ClientSetting.Style.Moon) {
            ((IChatHudHook) mc.inGameHud.getChatHud()).addMessage(Text.of("§f[§b" + ClientSetting.INSTANCE.hackName.getValue() + "§f] " + message), id);
            return;
        }
        ((IChatHudHook) mc.inGameHud.getChatHud()).addMessage(Text.of(syncCode + "§r" + ClientSetting.INSTANCE.hackName.getValue() + "§f " + message), id);
    }

    public static void sendChatMessageWidthIdNoSync(String message, int id) {
        if (Module.nullCheck()) return;
        ((IChatHudHook) mc.inGameHud.getChatHud()).addMessage(Text.of("§f" + message), id);
    }
}
