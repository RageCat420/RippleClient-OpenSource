package dev.ripple.mod.commands.impl;

import dev.ripple.mod.commands.Command;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

import java.util.List;

public class RejoinCommand extends Command {

	public RejoinCommand() {
		super("rejoin", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		mc.executeTask(() -> {
			if (mc.world != null && mc.getCurrentServerEntry() != null) {
				ServerInfo lastestServerEntry = mc.getCurrentServerEntry();
				new DisconnectS2CPacket(Text.of("Self kick")).apply(mc.getNetworkHandler());
				ConnectScreen.connect(new TitleScreen(), mc, ServerAddress.parse(lastestServerEntry.address), lastestServerEntry, false, null);
			}
		});
	}


	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
