package dev.ripple.mod.modules.impl.combat;

import dev.ripple.api.events.eventbus.EventHandler;
import dev.ripple.api.events.impl.UpdateWalkingPlayerEvent;
import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.api.utils.math.Timer;
import dev.ripple.mod.gui.clickgui.UIScreen;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.List;

public class AutoTotem extends Module {
    public List<Block> blocks = List.of(Blocks.CRAFTER, Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER, Blocks.BREWING_STAND, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST, Blocks.HOPPER, Blocks.DISPENSER, Blocks.DROPPER, Blocks.ENCHANTING_TABLE, Blocks.GRINDSTONE, Blocks.SMITHING_TABLE, Blocks.STONECUTTER, Blocks.LOOM, Blocks.CARTOGRAPHY_TABLE, Blocks.BARREL, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL, Blocks.LECTERN, Blocks.SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.CHERRY_TRAPDOOR, Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.ACACIA_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.CRIMSON_TRAPDOOR, Blocks.WARPED_TRAPDOOR, Blocks.BAMBOO_TRAPDOOR, Blocks.CHERRY_DOOR, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR, Blocks.CRIMSON_DOOR, Blocks.WARPED_DOOR, Blocks.BAMBOO_DOOR, Blocks.STONE_BUTTON, Blocks.CHERRY_BUTTON, Blocks.OAK_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.BIRCH_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.ACACIA_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.CRIMSON_BUTTON, Blocks.WARPED_BUTTON, Blocks.BAMBOO_BUTTON, Blocks.POLISHED_BLACKSTONE_BUTTON, Blocks.CHERRY_FENCE_GATE, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.CRIMSON_FENCE_GATE, Blocks.WARPED_FENCE_GATE, Blocks.BAMBOO_FENCE_GATE, Blocks.WHITE_BED, Blocks.ORANGE_BED, Blocks.MAGENTA_BED, Blocks.LIGHT_BLUE_BED, Blocks.YELLOW_BED, Blocks.LIME_BED, Blocks.PINK_BED, Blocks.GRAY_BED, Blocks.LIGHT_GRAY_BED, Blocks.CYAN_BED, Blocks.PURPLE_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.GREEN_BED, Blocks.RED_BED, Blocks.BLACK_BED, Blocks.CHERRY_SIGN, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.JUNGLE_SIGN, Blocks.ACACIA_SIGN, Blocks.DARK_OAK_SIGN, Blocks.CRIMSON_SIGN, Blocks.WARPED_SIGN, Blocks.BAMBOO_SIGN, Blocks.CHERRY_WALL_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_WALL_SIGN, Blocks.BAMBOO_WALL_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.OAK_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.CRIMSON_HANGING_SIGN, Blocks.WARPED_HANGING_SIGN, Blocks.BAMBOO_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN, Blocks.NOTE_BLOCK, Blocks.JUKEBOX, Blocks.LEVER, Blocks.RESPAWN_ANCHOR, Blocks.CHISELED_BOOKSHELF);
    private final BooleanSetting onlyTick = add(new BooleanSetting("OnlyTick", false));
    private final BooleanSetting mainHand = add(new BooleanSetting("MainHand", false));
    private final BooleanSetting crystal = add(new BooleanSetting("Crystal", false, () -> !mainHand.getValue()));
    private final BooleanSetting gapple = add(new BooleanSetting("Gapple", false, () -> !mainHand.getValue()));
    private final SliderSetting health = add(new SliderSetting("Health", 16.0f, 0.0f, 36.0f, 0.1));
    private final SliderSetting gHealth = add(new SliderSetting("GappleHealth", 4f, 0f, 36f, 0.1));
    private final SliderSetting switchDelay = add(new SliderSetting("SwitchDelay", 200, 0, 1000, 1).setSuffix("ms"));
    
    public AutoTotem() {
        super("AutoTotem", Category.Combat);
        setChinese("自动图腾");
    }

    int totems = 0;
    private final Timer timer = new Timer();

    private boolean preCrashing = true;

    @Override
    public String getInfo() {
        return String.valueOf(totems);
    }

    public void check() {
        HitResult target = mc.crosshairTarget;
        if (target instanceof BlockHitResult bhr) {
            Block block = mc.world.getBlockState(bhr.getBlockPos()).getBlock();
            if (blocks.contains(block) && (!(blocks.contains(Blocks.RESPAWN_ANCHOR)) || block != Blocks.RESPAWN_ANCHOR)) {
                if (!mc.world.isAir(bhr.getBlockPos())) {
                    preCrashing = true;
                }
            } else if (blocks.contains(Blocks.RESPAWN_ANCHOR) && block == Blocks.RESPAWN_ANCHOR) {
                preCrashing = mc.world.getBlockState(bhr.getBlockPos()).get(RespawnAnchorBlock.CHARGES) != 0;
            } else {
                preCrashing = false;
            }
        }
        if (target instanceof EntityHitResult ehr) {
            Entity ety = ehr.getEntity();
            if (ety instanceof WanderingTraderEntity) {
                preCrashing = true;
            } else if (ety instanceof VillagerEntity) {
                preCrashing = true;
            } else if (ety instanceof ItemFrameEntity || ety instanceof GlowItemFrameEntity) {
                preCrashing = true;
            } else if (ety instanceof BoatEntity || ety instanceof MinecartEntity || ety instanceof HorseEntity || ety instanceof DonkeyEntity || ety instanceof MuleEntity || ety instanceof LlamaEntity || ety instanceof TraderLlamaEntity || ety instanceof SkeletonHorseEntity || ety instanceof ZombieHorseEntity) {
                preCrashing = true;
            } else if (ety instanceof PigEntity pig) {
                preCrashing = pig.isSaddled();
            } else if (ety instanceof StriderEntity strider) {
                preCrashing = strider.isSaddled();
            } else {
                preCrashing = false;
            }
        }
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        if (onlyTick.getValue()) return;
        check();
        update();
    }

    @Override
    public void onUpdate() {
        check();
        update();
    }

    private void update() {
        if (nullCheck()) return;
        totems = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof InventoryScreen) && !(mc.currentScreen instanceof UIScreen) && !(mc.currentScreen instanceof GameMenuScreen)) {
            return;
        }
        if (!timer.passedMs(switchDelay.getValueInt())) {
            return;
        }
        if (gapple.getValue() && !mainHand.getValue()
                && (mc.player.getMainHandStack().getItem() instanceof SwordItem
                || mc.player.getMainHandStack().getItem() instanceof PickaxeItem
                || mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING
                || ((mc.player.getOffHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE
                || mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE)
                && mc.player.getActiveItem() == mc.player.getOffHandStack()))
                && mc.options.useKey.isPressed() && !preCrashing
                && mc.player.getHealth() + mc.player.getAbsorptionAmount() > gHealth.getValue()) {
            if (mc.player.getOffHandStack().getItem() != Items.ENCHANTED_GOLDEN_APPLE && mc.player.getOffHandStack().getItem() != Items.GOLDEN_APPLE) {
                int itemSlot = findItemInventorySlot(Items.ENCHANTED_GOLDEN_APPLE);
                if (itemSlot == -1) {
                    itemSlot = findItemInventorySlot(Items.GOLDEN_APPLE);
                }
                if (itemSlot != -1) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                    EntityUtil.syncInventory();
                    timer.reset();
                }
            }
            return;
        }
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.getValue()) {
            if (!mainHand.getValue() && crystal.getValue() && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
                int itemSlot = findItemInventorySlot(Items.END_CRYSTAL);
                if (itemSlot != -1) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                    EntityUtil.syncInventory();
                    timer.reset();
                }
            }
            return;
        }
        if (mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING || mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }
        int itemSlot = findItemInventorySlot(Items.TOTEM_OF_UNDYING);
        if (itemSlot != -1) {
            if (mainHand.getValue()) {
                InventoryUtil.switchToSlot(0);
                if (mc.player.getInventory().getStack(0).getItem() != Items.TOTEM_OF_UNDYING) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36, 0, SlotActionType.PICKUP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                    EntityUtil.syncInventory();
                }
            } else {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                EntityUtil.syncInventory();
            }
            timer.reset();
        }
    }

    public int findItemInventorySlot(Item item) {
        for (int i = 0; i <= 44; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i < 9 ? i + 36 : i;
        }
        return -1;
    }

}
