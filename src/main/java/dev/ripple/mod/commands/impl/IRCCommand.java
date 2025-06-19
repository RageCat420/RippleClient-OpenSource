package dev.ripple.mod.commands.impl;

import dev.ripple.Ripple;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.mod.commands.Command;

import java.util.List;

public class IRCCommand extends Command {
    public IRCCommand() {
        super("irc", "[Message]");
    }

    public void runCommand(String[] args) {
        if (args.length < 1) {
            CommandManager.sendChatMessage("§c用法: §7" + this.getSyntax());
            return;
        }
        String message = String.join(" ", args);
        if (Ripple.IRC.sendMessage(message)) {
            System.out.println("[IRC] [Log] " + message);
        } else {
            CommandManager.sendChatMessage("§c无法发送消息，未连接到IRC服务器！");
            System.out.println("[IRC] [Log] Failed to send msg!");
        }
    }

    public String[] getAutocorrect(int count, List<String> separated) {
        return new String[0];
    }
}
