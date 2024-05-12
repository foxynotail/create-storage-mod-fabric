package net.fxnt.fxntstorage.containers.util;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ImplementedContainer extends WorldlyContainer {
    /**
     * Gets the item list of this inventory.
     * Must return the same instance every time it's called.
     *
     * @return the item list
     */
    NonNullList<ItemStack> getItems();

    /**
     * Creates an inventory from the item list.
     *
     * @param items the item list
     * @return a new inventory
     */
    static ImplementedContainer create(NonNullList<ItemStack> items) {
        return () -> items;
    }

    /**
     * Creates a new inventory with the size.
     *
     * @param size the inventory size
     * @return a new inventory
     */
    static ImplementedContainer createWithCapacity(int size) {
        return create(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    // SidedInventory

    /**
     * Gets the available slots to automation on the side.
     *
     * <p>The default implementation returns an array of all slots.
     *
     * @param side the side
     * @return the available slots
     */
    @Override
    default int[] getSlotsForFace(Direction side) {
        int[] result = new int[getItems().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }

        return result;
    }

    /**
     * Returns true if the stack can be inserted in the slot at the side.
     *
     * <p>The default implementation returns true.
     *
     * @param slot the slot
     * @param stack the stack
     * @param side the side
     * @return true if the stack can be inserted
     */
    @Override
    default boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return true;
    }

    /**
     * Returns true if the stack can be extracted from the slot at the side.
     *
     * <p>The default implementation returns true.
     *
     * @param slot the slot
     * @param stack the stack
     * @param side the side
     * @return true if the stack can be extracted
     */
    @Override
    default boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    // Inventory

    /**
     * Returns the inventory size.
     *
     * <p>The default implementation returns the size of {@link #getItems()}.
     *
     * @return the inventory size
     */
    @Override
    default int getContainerSize() {
        return getItems().size();
    }

    /**
     * @return true if this inventory has only empty stacks, false otherwise
     */
    @Override
    default boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the item in the slot.
     *
     * @param slot the slot
     * @return the item in the slot
     */
    @Override
    default ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    /**
     * Takes a stack of the size from the slot.
     *
     * <p>(default implementation) If there are less items in the slot than what are requested,
     * takes all items in that slot.
     *
     * @param slot the slot
     * @param count the item count
     * @return a stack
     */
    @Override
    default ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(getItems(), slot, count);
        if (!result.isEmpty()) {
            setChanged();
        }

        return result;
    }

    /**
     * Removes the current stack in the {@code slot} and returns it.
     *
     * @param slot the slot
     * @return the removed stack
     */
    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(getItems(), slot);
    }

    /**
     * Replaces the current stack in the {@code slot} with the provided stack.
     *
     * <p>If the stack is too big for this inventory,
     * it gets resized to this inventory's maximum amount.
     *
     * @param slot the slot
     * @param stack the stack
     */
    @Override
    default void setItem(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    /**
     * Clears {@linkplain #getItems() the item list}}.
     */
    @Override
    default void clearContent() {
        getItems().clear();
    }

    @Override
    default void setChanged() {
        // Override if you want behavior.
    }

    @Override
    default boolean stillValid(Player player) {
        return true;
    }
}
