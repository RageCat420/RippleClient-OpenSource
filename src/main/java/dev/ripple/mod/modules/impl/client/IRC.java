package dev.ripple.mod.modules.impl.client;

import dev.ripple.Ripple;
import dev.ripple.mod.modules.Module;

public class IRC extends Module {
    public static IRC INSTANCE;

    public IRC() {
        super("IRC", Category.Client);
        setChinese("用户聊天");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        Ripple.IRC.init();
    }

    @Override
    public void onDisable() {
        Ripple.IRC.disconnect();
    }
}