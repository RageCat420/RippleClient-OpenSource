package dev.ripple.mod.modules.impl.misc;

import dev.ripple.Ripple;
import dev.ripple.mod.modules.settings.impl.StringSetting;
import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.SendMessageEvent;
import dev.ripple.mod.modules.Module;

public class ChatAppend extends Module {
	public static ChatAppend INSTANCE;
	private final StringSetting message = add(new StringSetting("append", Ripple.NAME));
	public ChatAppend() {
		super("ChatAppend", Category.Misc);
		setChinese("消息后缀");
		INSTANCE = this;
	}

	@EventHandler
	public void onSendMessage(SendMessageEvent event) {
		if (nullCheck() || event.isCancelled() || AutoQueue.inQueue) return;
		String message = event.message;

		if (message.startsWith("/") || message.startsWith("!") || message.endsWith(this.message.getValue())) {
			return;
		}
		String suffix = this.message.getValue();
		message = message + " " + suffix;
		event.message = message;
	}
}