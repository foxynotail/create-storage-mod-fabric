package net.fxnt.fxntstorage.containers.util;

import net.fxnt.fxntstorage.containers.StorageBoxEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ScrollableSlot extends Slot {
    private boolean active;

    public ScrollableSlot(Container container, int slot, int x, int y, boolean active) {
        super(container, slot, x, y);
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void toggleActive() {
        active = !active;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        Level level = Minecraft.getInstance().level;

        return !(container instanceof StorageBoxEntity storageBoxEntity) || storageBoxEntity.filterTest(level, stack);
    }
}
