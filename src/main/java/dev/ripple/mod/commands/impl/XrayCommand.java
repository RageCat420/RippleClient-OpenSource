package dev.ripple.mod.commands.impl;

import dev.ripple.Ripple;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.mod.commands.Command;

import java.util.ArrayList;
import java.util.List;

public class XrayCommand extends Command {

	public XrayCommand() {
		super("xray", "[name/reset/list] | [add/addkey/remove/removekey] [name]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
        switch (parameters[0]) {
            case "reset" -> {
                Ripple.XRAY.list.clear();
                CommandManager.sendChatMessage("§fBlocks list got reset");
                return;
            }
            case "list" -> {
                if (Ripple.XRAY.list.isEmpty()) {
                    CommandManager.sendChatMessage("§fBlocks list is empty");
                    return;
                }

                for (String name : Ripple.XRAY.list) {
                    CommandManager.sendChatMessage("§a" + name);
                }
                return;
            }
            case "addkey" -> {
                if (parameters.length == 2) {
                    Ripple.XRAY.add(parameters[1]);
                    CommandManager.sendChatMessage("§f" + parameters[1] + (Ripple.XRAY.inWhitelist(parameters[1]) ? " §ahas been added" : " §chas been removed"));
                    return;
                }
                sendUsage();
                return;
            }
            case "add" -> {
                if (parameters.length == 2) {
                    Ripple.XRAY.add("block.minecraft." + parameters[1]);
                    CommandManager.sendChatMessage("§f" + parameters[1] + (Ripple.XRAY.inWhitelist("block.minecraft." +parameters[1]) ? " §ahas been added" : " §chas been removed"));
                    return;
                }
                sendUsage();
                return;
            }
            case "remove" -> {
                if (parameters.length == 2) {
                    Ripple.XRAY.remove("block.minecraft." + parameters[1]);
                    CommandManager.sendChatMessage("§f" + parameters[1] + (Ripple.XRAY.inWhitelist("block.minecraft." +parameters[1]) ? " §ahas been added" : " §chas been removed"));
                    return;
                }
                sendUsage();
                return;
            }
            case "removekey" -> {
                if (parameters.length == 2) {
                    Ripple.XRAY.remove(parameters[1]);
                    CommandManager.sendChatMessage("§f" + parameters[1] + (Ripple.XRAY.inWhitelist(parameters[1]) ? " §ahas been added" : " §chas been removed"));
                    return;
                }
                sendUsage();
                return;
            }
        }

        if (parameters.length == 1) {
			CommandManager.sendChatMessage("§f" + parameters[0] + (Ripple.XRAY.inWhitelist(parameters[0]) ? " §ais in whitelist" : " §cisn't in whitelist"));
			return;
		}

		sendUsage();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		if (count == 1) {
			String input = seperated.get(seperated.size() - 1).toLowerCase();
			List<String> correct = new ArrayList<>();
			List<String> list = List.of("add", "addkey", "remove", "removekey", "list", "reset");
			for (String x : list) {
				if (input.equalsIgnoreCase(Ripple.PREFIX + "xray") || x.toLowerCase().startsWith(input)) {
					correct.add(x);
				}
			}
			int numCmds = correct.size();
			String[] commands = new String[numCmds];

			int i = 0;
			for (String x : correct) {
				commands[i++] = x;
			}

			return commands;
		}
		return null;
	}
}
