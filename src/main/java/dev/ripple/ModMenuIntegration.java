package dev.ripple;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.ripple.core.impl.GuiManager;
import dev.ripple.mod.modules.impl.client.UI;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (UI.INSTANCE != null) UI.INSTANCE.enable();
        return screen -> GuiManager.uiScreen;
    }
}
