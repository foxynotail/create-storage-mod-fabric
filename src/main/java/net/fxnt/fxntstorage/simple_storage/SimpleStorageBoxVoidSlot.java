package net.fxnt.fxntstorage.simple_storage;

import net.fxnt.fxntstorage.init.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SimpleStorageBoxVoidSlot extends Slot {
    public SimpleStorageBoxVoidSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (this.hasItem()) return false;
        return stack.is(ModItems.STORAGE_BOX_VOID_UPGRADE);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
