package net.fxnt.fxntstorage.backpacks.upgrades;

import net.fxnt.fxntstorage.backpacks.main.BackPackEntity;
import net.fxnt.fxntstorage.backpacks.main.BackPackItem;
import net.fxnt.fxntstorage.backpacks.util.BackPackHelper;
import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BackPackAsBlockUpgradeHandler {
    private final Container container;
    private final BlockEntity blockEntity;
    private final Level level;
    private final BlockPos pos;
    private static final int magnetUpgradeRange = Config.BACKPACK_MAGNET_RANGE.get();

    public BackPackAsBlockUpgradeHandler(BackPackEntity backPackEntity) {
        this.container = backPackEntity;
        this.blockEntity = backPackEntity;
        this.level = blockEntity.getLevel();
        this.pos = blockEntity.getBlockPos();
    }

    // Magnet Upgrade
    public void applyMagnetUpgrade() {
        if (blockEntity != null && !level.isClientSide) {
            // Define the bounding box around the center position
            AABB boundingBox = new AABB(pos).inflate(magnetUpgradeRange);
            // Retrieve all item entities within the range
            List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class, boundingBox);

            if (!nearbyItems.isEmpty()) {
                for (ItemEntity itemEntity : nearbyItems) {
                    if (itemEntity.getItem().getItem() instanceof BackPackItem) continue;
                    new BackPackHelper().itemEntityToBackPack(this.container, this.blockEntity.getLevel(), itemEntity, Util.ITEM_SLOT_START_RANGE, Util.ITEM_SLOT_END_RANGE);
                }
            }
        }
    }
}
