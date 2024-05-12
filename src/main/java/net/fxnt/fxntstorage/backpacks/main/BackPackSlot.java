package net.fxnt.fxntstorage.backpacks.main;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class BackPackSlot extends Slot {

    private final Container container;
    private final Level level;
    public BackPackSlot(Container container, Level level, int slot, int x, int y) {
        super(container, slot, x, y);
        this.container = container;
        this.level = level;
    }


    @Override
    public int getMaxStackSize() {
        return this.container.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return this.container.getMaxStackSize();
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public void set(ItemStack stack) {
        super.set(stack);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return BackPackEntity.filterTest(this.level, stack);
    }

    @Override
    public ItemStack safeInsert(ItemStack stack) {
        return super.safeInsert(stack);
    }

    @Override
    public ItemStack safeInsert(ItemStack newStack, int increment) {
        if (!newStack.isEmpty() && this.mayPlace(newStack)) {
            ItemStack currentStack = this.getItem();
            int i = Math.min(Math.min(increment, newStack.getCount()), this.getMaxStackSize(newStack) - currentStack.getCount());
            if (currentStack.isEmpty()) {
                this.setByPlayer(newStack.split(i));
            } else if (ItemStack.isSameItemSameTags(currentStack, newStack)) {
                newStack.shrink(i);
                currentStack.grow(i);
                this.setByPlayer(currentStack);
            }
            return newStack;
        } else {
            return newStack;
        }
    }

    @Override
    public Optional<ItemStack> tryRemove(int count, int decrement, Player player) {
        if (!this.mayPickup(player)) {
            return Optional.empty();
        } else if (!this.allowModification(player) && decrement < this.getItem().getCount()) {
            return Optional.empty();
        } else {
            count = Math.min(count, decrement);
            ItemStack itemStack = this.remove(count);
            if (itemStack.isEmpty()) {
                return Optional.empty();
            } else {
                if (this.getItem().isEmpty()) {
                    this.setByPlayer(ItemStack.EMPTY);
                }

                return Optional.of(itemStack);
            }
        }
    }
}
