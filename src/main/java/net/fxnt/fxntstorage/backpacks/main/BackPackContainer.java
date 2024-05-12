package net.fxnt.fxntstorage.backpacks.main;

import net.fxnt.fxntstorage.backpacks.upgrades.UpgradeItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class BackPackContainer extends SimpleContainer implements Container, StackedContentsCompatible {

    public NonNullList<ItemStack> items = NonNullList.withSize(BackPackBlock.getSlotCount()+1, ItemStack.EMPTY);
    public NonNullList<String> upgrades = NonNullList.create();
    private int size = BackPackBlock.getSlotCount();
    @Nullable
    private List<ContainerListener> listeners;
    private ItemStack backPackItemStack = null;
    public int maxStackSize;

    public Block block;

    public BackPackContainer(ItemStack itemStack) {
        if (itemStack == null) return;
        this.backPackItemStack = itemStack;

        if (itemStack.getItem() instanceof BackPackItem backPackItem) {
            this.block = backPackItem.getBlock();
            if (this.block instanceof BackPackBlock backPackBlock) {
                this.maxStackSize = backPackBlock.getMaxStackSize();
            }
        }
        loadItemsFromStack(itemStack);
        this.size = this.items.size();
    }

    public Component getDisplayName() {
        return this.backPackItemStack.getHoverName();
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    public NonNullList<String> getUpgrades() {
        return this.upgrades;
    }

    public void loadItemsFromStack(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTagElement("BlockEntityTag");
        if (tag != null) {
            if (tag.contains("Items")) {
                this.items.clear();
                ListTag listTag = tag.getList("Items", Tag.TAG_COMPOUND);
                for (int i = 0; i < listTag.size(); ++i) {
                    CompoundTag compoundTag = listTag.getCompound(i);
                    int slot = compoundTag.getByte("Slot") & 255;
                    ItemStack slotStack = ItemStack.of(compoundTag);
                    if (compoundTag.contains("ActualCount", Tag.TAG_INT)) {
                        slotStack.setCount(compoundTag.getInt("ActualCount"));
                    }
                    if (slot < this.items.size()) {
                        this.items.set(slot, slotStack);
                    }
                }
            }
            if (tag.contains("Upgrades")) {
                this.upgrades.clear();
                ListTag upgradesList = tag.getList("Upgrades", Tag.TAG_STRING);
                for (int i = 0; i < upgradesList.size(); i++) {
                    this.upgrades.add(i, upgradesList.getString(i));
                }
            }
            if (tag.contains("maxStackSize")) {
                this.maxStackSize = tag.getInt("maxStackSize");
            }
        } else {
            // If tag is null, itemstack not initialized
            // Initialize stack with default settins
            saveItemsToStack();
        }
    }

    public void saveItemsToStack() {
        CompoundTag tag = new CompoundTag();

        // Save items
        ListTag itemsList = new ListTag();
        for(int i = 0; i < this.items.size(); ++i) {
            ItemStack tagStack = this.items.get(i);
            if (!tagStack.isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte)i);
                tagStack.save(compoundTag);
                compoundTag.putInt("ActualCount", tagStack.getCount());
                itemsList.add(compoundTag);
            }
        }
        tag.put("Items", itemsList);

        // Save upgrades
        ListTag upgradesList = new ListTag();
        for (int i = 0; i < this.upgrades.size(); i++) {
            upgradesList.add(i, StringTag.valueOf(this.upgrades.get(i)));
        }
        tag.put("Upgrades", upgradesList);

        tag.putInt("maxStackSize", this.maxStackSize);
        this.backPackItemStack.addTagElement("BlockEntityTag", tag);
    }

    @Override
    public void startOpen(Player player) {
    }

    @Override
    public void stopOpen(Player player) {
        saveItemsToStack();
    }
    public void refreshUpgrades() {
        this.upgrades.clear();
        int UPGRADE_SLOT_START_INDEX = BackPackBlock.containerSlotCount + BackPackBlock.toolSlotCount;
        int UPGRADE_SLOT_END_INDEX = UPGRADE_SLOT_START_INDEX + BackPackBlock.upgradeSlotCount;

        for (int i = UPGRADE_SLOT_START_INDEX; i < UPGRADE_SLOT_END_INDEX; i++) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.getItem() instanceof UpgradeItem upgradeItem) {
                String upgradeName = upgradeItem.getUpgradeName();
                if (!this.upgrades.contains(upgradeName)) {
                    this.upgrades.add(upgradeName);
                }
            }
        }
    }

    @Override
    public void setChanged() {
        if (this.listeners != null) {
            for(ContainerListener containerListener : this.listeners) {
                containerListener.containerChanged(this);
            }
        }
        refreshUpgrades();
        saveItemsToStack();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return super.canPlaceItem(index, stack);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < this.items.size() ? this.items.get(slot) : ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = this.items.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    public ItemStack addItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemStack = stack.copy();
            this.moveItemToOccupiedSlotsWithSameType(itemStack);
            if (itemStack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                this.moveItemToEmptySlots(itemStack);
                return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
            }
        }
    }
    public ItemStack removeItemType(Item item, int amount) {
        ItemStack itemStack = new ItemStack(item, 0);

        for(int i = this.size - 1; i >= 0; --i) {
            ItemStack itemStack2 = this.getItem(i);
            if (itemStack2.getItem().equals(item)) {
                int j = amount - itemStack.getCount();
                ItemStack itemStack3 = itemStack2.split(j);
                itemStack.grow(itemStack3.getCount());
                if (itemStack.getCount() == amount) {
                    break;
                }
            }
        }

        if (!itemStack.isEmpty()) {
            this.setChanged();
        }

        return itemStack;
    }
    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }
        return itemStack;
    }
    public boolean canAddItem(ItemStack stack) {
        boolean bl = false;
        for(ItemStack itemStack : this.items) {
            if (itemStack.isEmpty() || ItemStack.isSameItemSameTags(itemStack, stack) && itemStack.getCount() < itemStack.getMaxStackSize()) {
                bl = true;
                break;
            }
        }
        return bl;
    }
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack itemStack = this.items.get(slot);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.items.set(slot, ItemStack.EMPTY);
            return itemStack;
        }
    }
    @Override
    public int getContainerSize() {
        return this.size;
    }
    @Override
    public boolean isEmpty() {
        for(ItemStack itemStack : this.items) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        return true;
    }
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    @Override
    public void clearContent() {
        this.items.clear();
        refreshUpgrades();
        this.setChanged();
    }
    @Override
    public void fillStackedContents(StackedContents contents) {
        for(ItemStack itemStack : this.items) {
            contents.accountStack(itemStack);
        }
    }
    public String toString() {
        return this.items.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList()).toString();
    }
    private void moveItemToEmptySlots(ItemStack stack) {
        for(int i = 0; i < this.size; ++i) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty()) {
                this.setItem(i, stack.copyAndClear());
                return;
            }
        }
    }
    private void moveItemToOccupiedSlotsWithSameType(ItemStack stack) {
        for(int i = 0; i < this.size; ++i) {
            ItemStack itemStack = this.getItem(i);
            if (ItemStack.isSameItemSameTags(itemStack, stack)) {
                this.moveItemsBetweenStacks(stack, itemStack);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void moveItemsBetweenStacks(ItemStack stack, ItemStack other) {
        int i = Math.min(this.getMaxStackSize(), other.getMaxStackSize());
        int j = Math.min(stack.getCount(), i - other.getCount());
        if (j > 0) {
            other.grow(j);
            stack.shrink(j);
            this.setChanged();
        }
    }
}
