package dev.ripple.mod.commands.impl;

import dev.ripple.Ripple;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.core.impl.ConfigManager;
import dev.ripple.mod.commands.Command;

import java.util.List;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super("reload", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§fReloading..");
		Ripple.CONFIG = new ConfigManager();
		Ripple.PREFIX = Ripple.CONFIG.getString("prefix", Ripple.PREFIX);
		Ripple.CONFIG.loadSettings();
		Ripple.XRAY.read();
		Ripple.TRADE.read();
		Ripple.FRIEND.read();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
