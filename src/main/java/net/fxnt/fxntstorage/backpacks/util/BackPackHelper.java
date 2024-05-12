package net.fxnt.fxntstorage.backpacks.util;

import com.simibubi.create.compat.Mods;
import net.fxnt.fxntstorage.backpacks.main.BackPackEntity;
import net.fxnt.fxntstorage.backpacks.main.BackPackItem;
import net.fxnt.fxntstorage.backpacks.main.BackPackSlot;
import net.fxnt.fxntstorage.compat.trinkets.Trinkets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BackPackHelper {

    public boolean isWearingBackPack(Player player) {
        ItemStack itemStack = getWornBackPackStack(player);
        return !itemStack.isEmpty();
    }

    public ItemStack getWornBackPackStack(Player player) {
        ItemStack backPack = ItemStack.EMPTY;
        boolean backPackFound = false;
        if (player != null) {
            if (Mods.TRINKETS.isLoaded()) {
                ItemStack backPackTrinket = Trinkets.getBackPackTrinket(player);
                if (!backPackTrinket.isEmpty()) {
                    backPack = backPackTrinket;
                    backPackFound = true;
                }
            }
            if (!backPackFound) {
                ItemStack chestSlotItem = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestSlotItem.getItem() instanceof BackPackItem) {
                    backPack = chestSlotItem;
                }
            }
        }
        return backPack;
    }
    public Container getBackPackContainerFromBlockPos(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BackPackEntity backPackEntity) {
            return backPackEntity;
        }
        return null;
    }

    public int getItemSlotFromContainer(Container container, Item itemToFind) {
        for(int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slotItem = container.getItem(i);
            if (slotItem.is(itemToFind)) {
                return i;
            }
        }
        return -1;
    }
    public NonNullList<BackPackSlot> getSlotsFromContainer(Container container, Level level) {
        NonNullList<BackPackSlot> slots = NonNullList.create();
        for (int i = 0; i < container.getContainerSize(); i++) {
            slots.add(i, new BackPackSlot(container, level, i, 0, 0));
        }
        return slots;
    }
    public boolean itemEntityToBackPack(Container container, Level level, ItemEntity itemEntity, int startIndex, int endIndex) {

        ItemStack newStack = itemEntity.getItem();
        NonNullList<BackPackSlot> slots = getSlotsFromContainer(container, level);

        if (endIndex == -1) endIndex = slots.size();

        boolean success = false;
        int i = startIndex;
        if (!newStack.isDamageableItem() && !newStack.hasTag() && !newStack.hasCustomHoverName() && !newStack.isBarVisible() && !newStack.isBarVisible()) {
            // If matching slot stack exists
            while (!newStack.isEmpty() && i < endIndex) {
                Slot slot = slots.get(i);
                ItemStack itemStack = slot.getItem();
                if (!itemStack.isEmpty() && ItemStack.isSameItemSameTags(newStack, itemStack)) {
                    int totalCount = itemStack.getCount() + newStack.getCount();
                    int maxPutSize = Math.max(newStack.getMaxStackSize(), slot.getMaxStackSize());
                    int availableSpace = maxPutSize - itemStack.getCount();

                    if (totalCount <= maxPutSize) {
                        newStack.setCount(0);
                        itemStack.setCount(totalCount);
                        slot.setChanged();
                        success = true;
                    } else if (availableSpace < newStack.getMaxStackSize()) {
                        newStack.shrink(availableSpace);
                        itemStack.setCount(maxPutSize);
                        slot.setChanged();
                        success = true;
                    }
                }
                ++i;
            }
        }
        if (!newStack.isEmpty()) {
            i = startIndex;
            // If matching slot doesn't exist
            while (i < endIndex) {
                BackPackSlot slot = slots.get(i);
                ItemStack itemStack = slot.getItem();
                if (itemStack.isEmpty() && slot.mayPlace(newStack)) {

                   int maxPutSize = Math.max(newStack.getMaxStackSize(), slot.getMaxStackSize());
                   int availableSpace = maxPutSize - itemStack.getCount();

                    if (newStack.getCount() > availableSpace) {
                        ItemStack inputStack = newStack.split(slot.getMaxStackSize());
                        slot.setByPlayer(inputStack);
                    } else {
                        ItemStack inputStack = newStack.split(newStack.getCount());
                        slot.setByPlayer(inputStack);
                    }
                    slot.setChanged();
                    success = true;
                    break;
                }
                ++i;
            }
        }
        return success;
    }
}