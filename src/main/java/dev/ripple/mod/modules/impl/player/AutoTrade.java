package dev.ripple.mod.modules.impl.player;

import dev.ripple.Ripple;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

public class AutoTrade extends Module {
    public AutoTrade() {
        super("AutoTrade", Category.Player);
    }
    public final SliderSetting repeat = add(new SliderSetting("Repeat", 2, 1, 15, 1));
    public final BooleanSetting autoClose = add(new BooleanSetting("AutoClose", true));
    @Override
    public void onUpdate() {
        if (mc.player.currentScreenHandler instanceof MerchantScreenHandler handler) {
            int i = 0;
            boolean flag = true;

                TradeOfferList list = handler.getRecipes();
                for (int size = 0; size < list.size(); ++size) {
                    if (i >= repeat.getValue()) return;
                    TradeOffer tradeOffer = list.get(size);
                    if (!tradeOffer.isDisabled()) {
                        if (Ripple.TRADE.inWhitelist(tradeOffer.getSellItem().getItem().getTranslationKey())) {
                            while (i < repeat.getValue() && flag) {
                                flag = false;
                                if (!tradeOffer.getDisplayedFirstBuyItem().isEmpty()) {
                                    int count = InventoryUtil.getItemCount(tradeOffer.getDisplayedFirstBuyItem().getItem());
                                    if (handler.getSlot(0).getStack().getItem() == tradeOffer.getDisplayedFirstBuyItem().getItem()) {
                                        count += handler.getSlot(0).getStack().getCount();
                                    }
                                    if (count < tradeOffer.getDisplayedFirstBuyItem().getCount()) {
                                        continue;
                                    }
                                }
                                if (!tradeOffer.getSecondBuyItem().isEmpty()) {
                                    int count = InventoryUtil.getItemCount(tradeOffer.getSecondBuyItem().get().itemStack().getItem());
                                    if (handler.getSlot(1).getStack().getItem() == tradeOffer.getSecondBuyItem().get().itemStack().getItem()) {
                                        count += handler.getSlot(1).getStack().getCount();
                                    }
                                    if (count < tradeOffer.getSecondBuyItem().get().itemStack().getCount()) {
                                        continue;
                                    }
                                }
                                mc.getNetworkHandler().sendPacket(new SelectMerchantTradeC2SPacket(size));
                                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 2, 1, SlotActionType.QUICK_MOVE, mc.player);
                                flag = true;
                                i++;
                                //mc.player.currentScreenHandler.onSlotClick(3, 1, SlotActionType.QUICK_MOVE, mc.player);
                            }
                        }
                    }
                }
                //CommandManager.sendChatMessage(handler.getSlot(0).getStack().getItem().getName().getString());
            if (autoClose.getValue() && i < repeat.getValue()) {
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                mc.currentScreen.close();
            }
        }
    }
}
