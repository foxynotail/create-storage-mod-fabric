package net.fxnt.fxntstorage.containers.util;

import net.fxnt.fxntstorage.containers.StorageBoxEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StorageBoxSlot extends Slot {
    private final Level level;
    public StorageBoxSlot(Container container, Level level, int slot, int x, int y) {
        super(container, slot, x, y);
        this.level = level;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !(container instanceof StorageBoxEntity storageBoxEntity) || storageBoxEntity.filterTest(this.level, stack);
    }
}
