package net.fxnt.fxntstorage.containers.util;

import net.fxnt.fxntstorage.FXNTStorage;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.stream.IntStream;

public class TransferItems {

    public static ItemStack transferItems(Container srcContainer, int index, Container dstContainer, int amount, boolean toPlayer) {

        FXNTStorage.LOGGER.info("Transfer Items");
        ItemStack remainder = ItemStack.EMPTY;

        if (isFullContainer(dstContainer)) {
            return remainder;
        } else {
            if (!srcContainer.getItem(index).isEmpty()) {
                ItemStack srcStack = srcContainer.getItem(index).copy();
                if (!canTakeItemFromContainer(dstContainer, srcContainer, srcStack, index)) return remainder;
                FXNTStorage.LOGGER.info("Can Take Item");

                ItemStack moveStack = addItem(dstContainer, srcContainer.removeItem(index, amount), toPlayer);
                if (moveStack.isEmpty()) {
                    dstContainer.setChanged();
                    return remainder;
                }

                if (toPlayer) {
                    return moveStack;
                } else {
                    srcContainer.setItem(index, moveStack);
                }
            }
            return remainder;
        }
    }

    public static boolean canTakeItemFromContainer(Container srcContainer, Container dstContainer, ItemStack stack, int slot) {
        return dstContainer.canTakeItem(srcContainer, slot, stack);
    }
    public static boolean canPlaceItemInContainer(Container dstContainer, ItemStack stack, int slot) {
        return dstContainer.canPlaceItem(slot, stack);
    }

    public static boolean isFullContainer(Container container) {
        return getSlots(container).allMatch((slot) -> {
            ItemStack itemStack = container.getItem(slot);
            return itemStack.getCount() >= itemStack.getMaxStackSize();
        });
    }

    public static IntStream getSlots(Container container) {
        return IntStream.range(0, container.getContainerSize());
    }

    public static ItemStack addItem(Container dstContainer, ItemStack stack, boolean toPlayer) {

        int i;
        int size = dstContainer.getContainerSize();
        if (!toPlayer) {
            for(i = 0; i < size && !stack.isEmpty(); ++i) {
                stack = tryMoveInItem(dstContainer, stack, i);
            }
        } else {
            // Prefer to pass to main hand
            if (dstContainer instanceof Inventory) {
                int selectedSlot = ((Inventory) dstContainer).selected;
                stack = tryMoveInItem(dstContainer, stack, selectedSlot);
            }
            for(i = 0; i < 36 && !stack.isEmpty(); ++i) {
                stack = tryMoveInItem(dstContainer, stack, i);
            }
        }

        return stack;
    }

    public static ItemStack tryMoveInItem(Container dstContainer, ItemStack stack, int slot) {
        ItemStack itemStack = dstContainer.getItem(slot);
        if (canPlaceItemInContainer(dstContainer, stack, slot)) {
            boolean success = false;
            if (itemStack.isEmpty()) {
                dstContainer.setItem(slot, stack);
                stack = ItemStack.EMPTY;
                success = true;
            } else if (canMergeItems(itemStack, stack)) {
                int i = stack.getMaxStackSize() - itemStack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.shrink(j);
                itemStack.grow(j);
                success = j > 0;
            }

            if (success) {
                dstContainer.setChanged();
            }
        }
        return stack;
    }

    public static boolean canMergeItems(ItemStack stack1, ItemStack stack2) {
        return stack1.getCount() <= stack1.getMaxStackSize() && ItemStack.isSameItemSameTags(stack1, stack2);
    }


}
