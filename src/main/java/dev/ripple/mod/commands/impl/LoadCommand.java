package dev.ripple.mod.commands.impl;

import dev.ripple.Ripple;
import dev.ripple.core.Manager;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.core.impl.ConfigManager;
import dev.ripple.mod.commands.Command;

import java.util.List;

public class LoadCommand extends Command {

	public LoadCommand() {
		super("load", "[config]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		CommandManager.sendChatMessage("§fLoading..");
		ConfigManager.options = Manager.getFile(parameters[0] + ".cfg");
		Ripple.CONFIG = new ConfigManager();
		Ripple.PREFIX = Ripple.CONFIG.getString("prefix", Ripple.PREFIX);
		Ripple.CONFIG.loadSettings();
        ConfigManager.options = Manager.getFile("options.txt");
		Ripple.save();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
