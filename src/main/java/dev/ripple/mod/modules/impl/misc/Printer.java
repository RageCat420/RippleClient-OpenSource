package dev.ripple.mod.modules.impl.misc;

import dev.ripple.api.utils.entity.EntityUtil;
import dev.ripple.api.utils.entity.InventoryUtil;
import dev.ripple.api.utils.entity.MovementUtil;
import dev.ripple.api.utils.entity.PauseUtil;
import dev.ripple.api.utils.world.BlockUtil;
import dev.ripple.mod.modules.Module;
import dev.ripple.mod.modules.settings.impl.BooleanSetting;
import dev.ripple.mod.modules.settings.impl.SliderSetting;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Printer extends Module {
    public static Printer INSTANCE;
    private final SliderSetting placeDelay = add(new SliderSetting("PlaceDelay", 0, 0, 20));
    private final SliderSetting blockPer = add(new SliderSetting("BlockPer", 1, 1, 8));
    private final SliderSetting range = add(new SliderSetting("PlaceRange", 5, 0, 6).setSuffix("m"));
    private final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    private final BooleanSetting packet = add(new BooleanSetting("PacketPlace", false));
    public final BooleanSetting pause = add(new BooleanSetting("PauseOnEat", true).setParent());
    public final BooleanSetting sameHand = add(new BooleanSetting("SameHand", true, pause::isOpen));
    private final BooleanSetting inventory = add(new BooleanSetting("InventorySwap", true));
    private final BooleanSetting movePause = add(new BooleanSetting("PauseOnMove", false));
    private final ArrayList<BlockPos> placePos = new ArrayList<>();
    private final Map<BlockPos, Item> placePosHash = new HashMap<>();
    private int delay = 0;

    public Printer() {
        super("Printer", Category.Misc);
        setChinese("投影打印机");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        delay = 0;
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (pause.getValue() && PauseUtil.checkPause(sameHand.getValue())) return;
        if (!movePause.getValue() || !MovementUtil.isMoving()) {
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
            if (worldSchematic != null) {
                if (delay < placeDelay.getValueInt()) {
                    delay++;
                } else {
                    delay = 0;
                    setPlacePos();
                    if (!placePos.isEmpty()) {
                        int num = Math.min(blockPer.getValueInt(), placePos.size());
                        int oldSlot = mc.player.getInventory().selectedSlot;
                        for(int i = 0; i < num; i++) {
                            BlockPos pos = placePos.get(i);
                            Item item = placePosHash.get(pos);
                            int slot = InventoryUtil.findItemInventorySlot(item);
                            if (slot != -1) {
                                doSwap(slot, oldSlot);
                                BlockUtil.placeBlock(pos, rotate.getValue(), packet.getValue());
                                doSwap(slot, oldSlot);
                            }
                        }
                    }
                }
            }
        }
    }

    private void doSwap(int slot, int selectSlot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, selectSlot);
            EntityUtil.syncInventory();
        } else if (slot < 9) {
            InventoryUtil.switchToSlot(slot);
        }
    }

    public void setPlacePos() {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();
        ArrayList<BlockPos> sphere = BlockUtil.getSphere(range.getValueFloat());
        placePos.clear();
        placePosHash.clear();
        for(BlockPos pos : sphere) {
            if (BlockUtil.canPlace(pos, range.getValueFloat(), true)
                    && worldSchematic.getBlockState(pos).getBlock() != Blocks.AIR
                    && worldSchematic.getBlockState(pos).getBlock() != mc.world.getBlockState(pos).getBlock()
                    && pos.getY() <= DataManager.getRenderLayerRange().getLayerMax()
                    && worldSchematic.getBlockState(pos).getBlock().asItem() != Items.AIR
                    && InventoryUtil.findItemInventorySlot(worldSchematic.getBlockState(pos).getBlock().asItem()) != -1
                    && !placePos.contains(pos)) {
                placePos.add(pos);
                placePosHash.put(pos, worldSchematic.getBlockState(pos).getBlock().asItem());
            }
        }
    }
}