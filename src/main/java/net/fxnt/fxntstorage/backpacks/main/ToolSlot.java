package net.fxnt.fxntstorage.backpacks.main;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ToolSlot extends Slot {
    private final Level level;
    public ToolSlot(Container container, Level level, int slot, int x, int y) {
        super(container, slot, x, y);
        this.level = level;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return BackPackEntity.filterTest(this.level, stack);
    }

}
