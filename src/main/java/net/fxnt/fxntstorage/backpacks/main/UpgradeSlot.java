package net.fxnt.fxntstorage.backpacks.main;

import net.fxnt.fxntstorage.backpacks.upgrades.UpgradeItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UpgradeSlot extends Slot {
    private final Level level;
    public UpgradeSlot(Container container, Level level, int slot, int x, int y) {
        super(container, slot, x, y);
        this.level = level;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    public static int getMaxStackSizeStatic() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Prevent Inception
        if(!BackPackEntity.filterTest(this.level, stack)) {
            return false;
        }

        // Only allow upgrades through
        return stack.getItem() instanceof UpgradeItem;
    }
}
