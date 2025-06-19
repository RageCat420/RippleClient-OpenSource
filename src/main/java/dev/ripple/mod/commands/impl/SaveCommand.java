package dev.ripple.mod.commands.impl;

import dev.ripple.core.Manager;
import dev.ripple.Ripple;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.core.impl.ConfigManager;
import dev.ripple.mod.commands.Command;

import java.util.List;

public class SaveCommand extends Command {

	public SaveCommand() {
		super("save", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 1) {
			CommandManager.sendChatMessage("§fSaving config named " + parameters[0]);
			ConfigManager.options = Manager.getFile(parameters[0] + ".txt");
			Ripple.save();
			ConfigManager.options = Manager.getFile("options.txt");
		} else {
			CommandManager.sendChatMessage("§fSaving..");
		}
		Ripple.save();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
