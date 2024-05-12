package net.fxnt.fxntstorage.containers.util;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ContainerSaveContents extends Container {
    NonNullList<ItemStack> getItems();

    default void loadInventoryFromTag(CompoundTag tag) {
        ContainerHelper.loadAllItems(tag, getItems());
    }

    default int replaceInventoryWith(NonNullList<ItemStack> items) {
        NonNullList<ItemStack> inventory = getItems();
        inventory.clear();
        //noinspection ConstantValue,DataFlowIssue
        int size = Math.min(inventory.size(), items.size());
        //noinspection ConstantValue
        for (int i = 0; i < size; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                inventory.set(i, stack);
            }
        }

        return size;
    }

    default void saveInventoryToTag(CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, getItems());
    }

    @Override
    default int getContainerSize() {
        return getItems().size();
    }

    @Override
    default boolean isEmpty() {
        for (ItemStack stack : getItems()) {
            if (stack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @NotNull
    @Override
    default ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    @NotNull
    @Override
    default ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(getItems(), slot, amount);
        if (!stack.isEmpty()) this.setChanged();
        return stack;
    }

    @NotNull
    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(getItems(), slot);
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        if (stack.getCount() > this.getMaxStackSize()) stack.setCount(this.getMaxStackSize());
        getItems().set(slot, stack);
        this.setChanged();
    }

    @Override
    boolean stillValid(Player player);

    @Override
    default void clearContent() {
        getItems().clear();
    }
}
