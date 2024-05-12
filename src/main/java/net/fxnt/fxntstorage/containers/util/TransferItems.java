package net.fxnt.fxntstorage.containers.util;

import net.fxnt.fxntstorage.util.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TransferItems {

    public static NonNullList<Slot> getSlots(Container container, Inventory inventory) {
        NonNullList<Slot> slots = NonNullList.create();
        for (int i = 0; i < container.getContainerSize(); i++) {
            slots.add(new Slot(container, i, i * Util.SLOT_SIZE, 0));
        }
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                slots.add(new Slot(inventory, y * 9 + x + 9, Util.SLOT_SIZE * x, y * Util.SLOT_SIZE));
            }
        }
        for (int i = 0; i < 9; i++) {
            slots.add(new Slot(inventory, i, i * Util.SLOT_SIZE, 2 * Util.SLOT_SIZE));
        }
        return slots;
    }

    public static ItemStack quickMoveStack(boolean toPlayer, ItemStack itemStack, int itemSlot, int containerRows, Container container, Inventory inventory) {

        NonNullList<Slot> slots = getSlots(container, inventory);

        ItemStack itemStack2 = ItemStack.EMPTY;

        if (toPlayer) {

            // TODO
            // if player is sneaking only get 1 item instead of a full stack
            // Not sure how to handle this for now

            quickMoveStack(itemSlot, containerRows, slots);
            while(!itemStack2.isEmpty() && ItemStack.isSameItem(itemStack2, itemStack)) {
                itemStack2 = quickMoveStack(itemSlot, containerRows, slots);
            }
        } else {
            // Modify player slot to match new slot index
            // If player hotbar selected = 2, then need to add all container slots + all player inventory slots first
            itemSlot += container.getContainerSize() + 27; // Player inventory size
            quickMoveStack(itemSlot, containerRows, slots);
            while(!itemStack2.isEmpty() && ItemStack.isSameItem(itemStack2, itemStack)) {
                itemStack2 = quickMoveStack(itemSlot, containerRows, slots);
            }
        }
        // Send back remainder of the item stack
        ItemStack remainder = container.getItem(itemSlot);

        return remainder;

    }

    public static ItemStack quickMoveStack(int index, int containerRows, NonNullList<Slot> slots) {

        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < containerRows * 9) { // Container Inventory
                if (!moveItemStackTo(itemStack2, containerRows * 9, slots.size(), true, slots)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemStack2, 0, containerRows * 9, false, slots)) { // Player Inventory
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    public static boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, NonNullList<Slot> slots) {
        boolean bl = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }
        if (stack.isStackable()) {
            while(!stack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex)) {
                Slot slot = slots.get(i);
                ItemStack itemStack = slot.getItem();
                if (!itemStack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemStack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= stack.getMaxStackSize()) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot.setChanged();
                        bl = true;
                    } else if (itemStack.getCount() < stack.getMaxStackSize()) {
                        stack.shrink(stack.getMaxStackSize() - itemStack.getCount());
                        itemStack.setCount(stack.getMaxStackSize());
                        slot.setChanged();
                        bl = true;
                    }
                }
                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(reverseDirection ? i >= startIndex : i < endIndex) {
                Slot slot = slots.get(i);
                ItemStack itemStack = slot.getItem();
                if (itemStack.isEmpty() && slot.mayPlace(stack)) {
                    if (stack.getCount() > slot.getMaxStackSize()) {
                        slot.setByPlayer(stack.split(slot.getMaxStackSize()));
                    } else {
                        slot.setByPlayer(stack.split(stack.getCount()));
                    }
                    slot.setChanged();
                    bl = true;
                    break;
                }
                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        return bl;
    }

}
