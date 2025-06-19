package dev.ripple.mod.modules;

import dev.ripple.Ripple;
import dev.ripple.core.impl.CommandManager;
import dev.ripple.core.impl.ModuleManager;
import dev.ripple.mod.Mod;
import dev.ripple.mod.modules.impl.client.*;
import dev.ripple.mod.modules.settings.Setting;
import dev.ripple.mod.modules.settings.impl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public abstract class Module extends Mod {

	public Module(String name, Category category) {
		this(name, "", category);
	}

	public Module(String name, String description, Category category) {
		super(name);
		this.category = category;
		this.description = description;
		ModuleManager.lastLoadMod = this;
		bindSetting = new BindSetting("Key", isGui() ? GLFW.GLFW_KEY_RIGHT_SHIFT : -1);
		drawnSetting = add(new BooleanSetting("Drawn", !listHide()));
	}
	private boolean isGui() {
		return this instanceof UI;
	}

	private boolean listHide() {
		return this instanceof Colors || this instanceof BaritoneModule || this instanceof AntiCheat || this instanceof ClientSetting || this instanceof HUD || this instanceof ModuleList;
	}
	private String description;
	private final Category category;
	private final BindSetting bindSetting;
	public final BooleanSetting drawnSetting;
	public boolean state;

	public String chinese;

	public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
		if (mc.getNetworkHandler() == null || mc.world == null) return;
		try (PendingUpdateManager pendingUpdateManager = mc.world.getPendingUpdateManager().incrementSequence()) {
			int i = pendingUpdateManager.getSequence();
			mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
		}
	}


	public void setChinese(String chinese) {
		this.chinese = chinese;
	}

	public String getDisplayName() {
		if (UI.INSTANCE.chinese.getValue() && chinese != null) {
			return chinese;
		}
		return getName();
	}


	private final List<Setting> settings = new ArrayList<>();

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Module.Category getCategory() {
		return this.category;
	}

	public BindSetting getBind() {
		return this.bindSetting;
	}
	public boolean isOn() {
		return this.state;
	}

	public boolean isOff() {
		return !isOn();
	}

	public void toggle() {
		if (this.isOn()) {
			disable();
		} else {
			enable();
		}
	}

	public void enable() {
		if (this.state) return;
		if (!nullCheck() && drawnSetting.getValue() && ClientSetting.INSTANCE.toggle.getValue()) {
			int id = ClientSetting.INSTANCE.onlyOne.getValue() ? -1 : hashCode();
			switch (ClientSetting.INSTANCE.messageStyle.getValue()) {
				case Mio -> CommandManager.sendChatMessageWidthId("§2[+] §f" + getDisplayName(), id);
				case Debug -> CommandManager.sendChatMessageWidthId(getCategory().name().toLowerCase() + "." + getDisplayName().toLowerCase() + ".§aenable", id);
				case Lowercase -> CommandManager.sendChatMessageWidthId(getDisplayName().toLowerCase() + " §aenabled", id);
				case Melon -> CommandManager.sendChatMessageWidthId("§b" + getDisplayName() + " §aEnabled.", id);
				case Normal -> CommandManager.sendChatMessageWidthId("§f" + getDisplayName() + " §aEnabled", id);
				case Future -> CommandManager.sendChatMessageWidthId("§7" + getDisplayName() + " toggled §aon", id);
				case Chinese -> CommandManager.sendChatMessageWidthId(getDisplayName() + " §a开启", id);
				case Moon -> CommandManager.sendChatMessageWidthIdNoSync("§f[§b" + ClientSetting.INSTANCE.hackName.getValue() + "§f] [" + "§3" + getDisplayName() + "§f]" + " §7toggled §aon", id);
				case Earth -> CommandManager.sendChatMessageWidthIdNoSync("§l" + getDisplayName() + " §aenabled.", id);
			}
		}
		this.state = true;
		Ripple.EVENT_BUS.subscribe(this);
		this.onToggle();
		try {
			this.onEnable();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disable() {
		if (!this.state) return;
		if (!nullCheck() && drawnSetting.getValue() && ClientSetting.INSTANCE.toggle.getValue()) {
			int id = ClientSetting.INSTANCE.onlyOne.getValue() ? -1 : hashCode();
			switch (ClientSetting.INSTANCE.messageStyle.getValue()) {
				case Mio -> CommandManager.sendChatMessageWidthId("§4[-] §f" + getDisplayName(), id);
				case Debug -> CommandManager.sendChatMessageWidthId(getCategory().name().toLowerCase() + "." + getDisplayName().toLowerCase() + ".§cdisable", id);
				case Lowercase -> CommandManager.sendChatMessageWidthId(getDisplayName().toLowerCase() + " §cdisabled", id);
				case Normal -> CommandManager.sendChatMessageWidthId("§f" + getDisplayName() + " §cDisabled", id);
				case Melon -> CommandManager.sendChatMessageWidthId("§b" + getDisplayName() + " §cDisabled.", id);
				case Future -> CommandManager.sendChatMessageWidthId("§7" + getDisplayName() + " toggled §coff", id);
				case Earth -> CommandManager.sendChatMessageWidthIdNoSync("§l" + getDisplayName() + " §cdisabled.", id);
				case Chinese -> CommandManager.sendChatMessageWidthId(getDisplayName().toLowerCase() + " §c关闭", id);
				case Moon -> CommandManager.sendChatMessageWidthIdNoSync("§f[§b" + ClientSetting.INSTANCE.hackName.getValue() + "§f] [" + "§3" + getDisplayName() + "§f]" + " §7toggled §coff", id);
			}
		}
		this.state = false;
		Ripple.EVENT_BUS.unsubscribe(this);
		this.onToggle();
		this.onDisable();
	}
	public void setState(boolean state) {
		if (this.state == state) return;
		if (state) {
			enable();
		} else {
			disable();
		}
	}

	public boolean setBind(String rkey) {
		if (rkey.equalsIgnoreCase("none")) {
			this.bindSetting.setKey(-1);
			return true;
		}
		int key;
		try {
			key = InputUtil.fromTranslationKey("key.keyboard." + rkey.toLowerCase()).getCode();
		} catch (NumberFormatException e) {
			if (!nullCheck()) CommandManager.sendChatMessage("§cBad key!");
			return false;
		}
		if (rkey.equalsIgnoreCase("none")) {
			key = -1;
		}
		if (key == 0) {
			return false;
		}
		this.bindSetting.setKey(key);
		return true;
	}

	public void addSetting(Setting setting) {
		this.settings.add(setting);
	}

	public StringSetting add(StringSetting setting) {
		addSetting(setting);
		return setting;
	}

	public ColorSetting add(ColorSetting setting) {
		addSetting(setting);
		return setting;
	}

	public SliderSetting add(SliderSetting setting) {
		addSetting(setting);
		return setting;
	}

	public BooleanSetting add(BooleanSetting setting) {
		addSetting(setting);
		return setting;
	}

	public <T extends Enum<T>> EnumSetting<T> add(EnumSetting<T> setting) {
		addSetting(setting);
		return setting;
	}

	public BindSetting add(BindSetting setting) {
		addSetting(setting);
		return setting;
	}

	public List<Setting> getSettings() {
		return this.settings;
	}

	public boolean hasSettings() {
		return !this.settings.isEmpty();
	}

	public static boolean nullCheck() {
		return mc.player == null || mc.world == null;
	}

	public void onDisable() {

	}

	public void onEnable() {

	}

	public void onToggle() {

	}

	public void onUpdate() {

	}

	public void onThread() {

	}

	public void onLogin() {

	}

	public void onLogout() {

	}
	public void onRender2D(DrawContext drawContext, float tickDelta) {

	}

	public void onRender3D(MatrixStack matrixStack) {

	}

	public final boolean isCategory(Module.Category category) {
		return category == this.category;
	}

	public String getArrayName() {
		return getDisplayName() + getArrayInfo();
	}
	public String getArrayInfo() {
		return (getInfo() == null ? "" : " §7[" + getInfo() + "§7]");
	}
	public String getInfo() {
		return null;
	}

	public enum Category {
		Combat, Misc, Render, Movement, Player, Exploit, Client
	}
}
