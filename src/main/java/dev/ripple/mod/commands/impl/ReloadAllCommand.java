package dev.ripple.mod.commands.impl;

import dev.ripple.Ripple;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.mod.commands.Command;

import java.util.List;

public class ReloadAllCommand extends Command {

	public ReloadAllCommand() {
		super("reloadall", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§fReloading..");
		Ripple.unload();
        try {
            Ripple.load();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
