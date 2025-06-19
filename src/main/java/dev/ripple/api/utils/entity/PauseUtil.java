package dev.ripple.api.utils.entity;

import dev.ripple.api.utils.Wrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class PauseUtil implements Wrapper {
    public static boolean checkPause(boolean sameHand, PlayerEntity player, Hand useHand) {
        return sameHand ? player.isUsingItem() && player.getActiveHand() == useHand : player.isUsingItem();
    }

    public static boolean checkPause(boolean sameHand, Hand useHand) {
        return checkPause(sameHand, mc.player, useHand);
    }

    public static boolean checkPause(boolean sameHand, PlayerEntity player) {
        return checkPause(sameHand, player, Hand.MAIN_HAND);
    }

    public static boolean checkPause(boolean sameHand) {
        return checkPause(sameHand, mc.player);
    }
}
