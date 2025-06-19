package dev.ripple.mod.modules.impl.misc;

import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.OpenScreenEvent;
import dev.ripple.asm.accessors.IAbstractSignEditScreen;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PriceMarker extends Module {
    public static PriceMarker INSTANCE;
    private final BooleanSetting original = add(new BooleanSetting("OriginalPrice", true));
    public String[] str;

    public PriceMarker() {
        super("PriceMarker", Category.Misc);
        setChinese("村民标价");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        str = null;
        if (!AutoSign.INSTANCE.preferPM.getValue() && AutoSign.INSTANCE.isOn()) disable();
    }

    @Override
    public void onUpdate() {
        if (!AutoSign.INSTANCE.preferPM.getValue() && AutoSign.INSTANCE.isOn()) {
            disable();
            return;
        }
        if (nullCheck()) return;
        if (mc.currentScreen instanceof MerchantScreen merchantScreen) {
            TradeOfferList tradeOffers = merchantScreen.getScreenHandler().getRecipes();
            List<String> info = new ArrayList<>();
            for (TradeOffer tradeOffer : tradeOffers) {
                Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> enchantMap = EnchantmentHelper.getEnchantments(tradeOffer.getSellItem()).getEnchantmentEntries();
                enchantMap.forEach((e) -> {
                    String name = e.getKey().value().description().getString();
                    ItemStack first = original.getValue() ? tradeOffer.getOriginalFirstBuyItem() : tradeOffer.getDisplayedFirstBuyItem();
                    int count = first.getCount();
                    info.add(name + (first.getItem() == Items.EMERALD ? "-" + count : ""));
                });
            }
            if (info.isEmpty()) {
                str = null;
                return;
            }
            if (info.size() < 4) {
                for(int i = info.size(); i < 4; i++) {
                    info.add("");
                }
                str = info.toArray(new String[info.size()]);
                return;
            }
            str = new String[]{info.get(0), info.get(1), info.get(2), info.get(3)};
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (nullCheck()) return;
        if (event.screen instanceof AbstractSignEditScreen && str != null) {
            SignBlockEntity sign = ((IAbstractSignEditScreen) event.screen).getSign();
            mc.getNetworkHandler().sendPacket(new UpdateSignC2SPacket(sign.getPos(), true, str[0], str[1], str[2], str[3]));
            event.cancel();
        }
    }
}