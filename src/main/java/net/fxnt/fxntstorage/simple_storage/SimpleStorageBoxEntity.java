package net.fxnt.fxntstorage.simple_storage;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.containers.util.ContainerSaveContents;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.init.ModItems;
import net.fxnt.fxntstorage.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleStorageBoxEntity extends SmartBlockEntity implements WorldlyContainer, ContainerSaveContents, ExtendedScreenHandlerFactory {
    public String title = "Simple Storage Box";
    public BlockPos pos;
    public int baseCapacity = 32;
    public int itemStackSize = 64;
    public int maxCapacity = baseCapacity; // Measured in stacks so max planks = 64 * 8000, max ender pearls = 16 * 8000
    public int maxItemCapacity = itemStackSize * maxCapacity;
    public int slot0MaxCapacity = maxItemCapacity - itemStackSize;
    public int slot1MaxCapacity = itemStackSize;
    public int slot0Amount = 0;
    public int slot1Amount = 0;
    public int storedAmount = 0;
    public int percentageUsed = 0;
    public boolean voidUpgrade = false;
    public int voidUpgradeSlot = 3;
    public int capacityUpgrades = 0;
    public int capacityUpgradeStartSlot = 4;
    public int maxCapacityUpgrades = 9;
    public int capacityUpgradeEndSlot = capacityUpgradeStartSlot + maxCapacityUpgrades;
    public int baseSlotCount = 3;
    public int slotCount = baseSlotCount + 1 + maxCapacityUpgrades; // 3 + Void Upgrade Slot + Capacity Upgrade Slots
    public NonNullList<ItemStack> items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public ItemStack filterItem = ItemStack.EMPTY;
    private final SimpleStorageBoxEntityHelper<SimpleStorageBoxEntity> helper;

    public SimpleStorageBoxEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.SIMPLE_STORAGE_BOX_ENTITY, pos, blockState);
        this.pos = pos;
        this.helper = new SimpleStorageBoxEntityHelper<>(this);
    }

    @Override
    public int getMaxStackSize() {
        // NEED THIS TO PREVENT FABRIC TRANSFER API TAKING ENTIRE CONTENTS IN 1 GO
        return this.getMaxItemCapacity();
    }

    public int getContainerSize() {
        return slotCount;
    }

    public int getCapacityUpgrades() {
        this.capacityUpgrades = 0;
        for (int i = this.capacityUpgradeStartSlot; i < this.capacityUpgradeStartSlot + this.maxCapacityUpgrades; i++) {
            if (this.getItem(i).is(ModItems.STORAGE_BOX_CAPACITY_UPGRADE)) {
                this.capacityUpgrades++;
            }
        }
        return this.capacityUpgrades;
    }

    public boolean hasVoidUpgrade() {
        this.voidUpgrade = this.getItem(this.voidUpgradeSlot).is(ModItems.STORAGE_BOX_VOID_UPGRADE);
        return this.voidUpgrade;
    }

    public int getStoredAmount() {
        this.storedAmount = this.getItem(0).getCount() + this.getItem(1).getCount();
        return this.storedAmount;
    }

    public int getMaxItemCapacity() {
        this.calculateMaxCapacity();
        return this.maxItemCapacity;
    }

    public ItemStack getFilterItem() {
        return this.filterItem;
    }

    public void calculateMaxCapacity() {
        // Set Max Item Capacity & Filter Item
        this.maxCapacity = this.baseCapacity;
        if (this.getCapacityUpgrades() > 0) {
            for (int i = 0; i < this.capacityUpgrades; i++) {
                this.maxCapacity *= 2;
            }
        }
        this.maxItemCapacity = this.maxCapacity * 64;

        if (!filterItem.isEmpty()) {
            this.itemStackSize = filterItem.getMaxStackSize();
            // If has item then get max stack size of item and multiply by maxCapacity
            this.maxItemCapacity = this.maxCapacity * filterItem.getMaxStackSize();
            this.slot0MaxCapacity = this.maxItemCapacity - this.itemStackSize;
            this.slot1MaxCapacity = this.itemStackSize;
        }
    }

    public int getPercentageUsed() {
        calculatePercentageUsed();
        return this.percentageUsed;
    }

    public int calculatePercentageUsed() {
        int totalSpace = getMaxItemCapacity();
        int usedSpace = getStoredAmount();
        double percentageUsed = ((double) usedSpace / totalSpace) * 100;
        this.percentageUsed = (int) Math.round(percentageUsed);
        return this.percentageUsed;
    }

    public void onLoad() {
        if (this.getLevel() != null && this.getLevel().isClientSide) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void saveInventoryToTag(CompoundTag tag) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < slotCount; i++) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putByte("Slot", (byte) i);
            ItemStack itemStack = this.getItem(i);
            itemStack.save(compoundTag);
            compoundTag.putInt("ActualCount", itemStack.getCount());
            listTag.add(compoundTag);
        }
        tag.put("Items", listTag);
    }

    @Override
    public void loadInventoryFromTag(CompoundTag tag) {
        if (tag.contains("Items")) {
            this.items.clear();
            ListTag listTag = tag.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag compoundTag = listTag.getCompound(i);
                int slot = compoundTag.getByte("Slot") & 255;
                ItemStack slotStack = ItemStack.of(compoundTag);
                if (compoundTag.contains("ActualCount", Tag.TAG_INT)) {
                    slotStack.setCount(compoundTag.getInt("ActualCount"));
                }
                this.setItem(slot, slotStack);
            }
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        this.loadInventoryFromTag(tag);
        this.title = tag.getString("title");
        this.slotCount = tag.getInt("slotCount");
        this.maxCapacity = tag.getInt("maxCapacity");
        this.maxItemCapacity = tag.getInt("maxItemCapacity");
        this.storedAmount = tag.getInt("storedAmount");
        this.percentageUsed = tag.getInt("percentageUsed");
        this.voidUpgrade = tag.getBoolean("voidUpgrade");
        this.capacityUpgrades = tag.getInt("capacityUpgrades");
        this.slot0Amount = tag.getInt("slot0Amount");
        this.items.get(0).setCount(this.slot0Amount);
        this.slot1Amount = tag.getInt("slot1Amount");
        this.items.get(1).setCount(this.slot1Amount);
        CompoundTag filterTag = tag.getCompound("filterItem");
        this.filterItem = ItemStack.of(filterTag);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        this.saveInventoryToTag(tag);
        tag.putString("title", this.title);
        tag.putInt("slotCount", this.slotCount);
        tag.putInt("maxCapacity", this.maxCapacity);
        tag.putInt("maxItemCapacity", this.getMaxItemCapacity());
        tag.putInt("storedAmount", this.getStoredAmount());
        tag.putInt("percentageUsed", this.calculatePercentageUsed());
        tag.putBoolean("voidUpgrade", this.hasVoidUpgrade());
        tag.putInt("capacityUpgrades", this.getCapacityUpgrades());
        tag.putInt("slot0Amount", this.items.get(0).getCount());
        tag.putInt("slot1Amount", this.items.get(1).getCount());
        CompoundTag filterTag = new CompoundTag();
        this.filterItem.save(filterTag);
        tag.put("filterItem", filterTag);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.slotCount);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(title);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new SimpleStorageBoxMenu(i, inventory, this);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    public <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockEntity blockEntity) {

        if (level.isClientSide) return;

        ItemStack slot0 = this.getItem(0);
        ItemStack slot1 = this.getItem(1);

        // Get Stored Amount
        this.storedAmount = slot0.getCount() + slot1.getCount();

        // Set filter item to items inside to prevent wrong items being put in
        if (this.storedAmount > 0) {
            ItemStack storedItem = slot0;
            if (storedItem.isEmpty()) storedItem = slot1;
            if (!storedItem.isEmpty() && !ItemStack.isSameItemSameTags(storedItem, this.filterItem)) {
                this.setFilter(storedItem);
            }
        }

        calculateMaxCapacity();
        moveItems();

        // Run standard tick functions
        helper.serverTick(level, blockPos, blockEntity);
    }

    private void moveItems() {

        ItemStack slot0 = this.getItem(0);
        ItemStack slot1 = this.getItem(1);
        //FXNTStorage.LOGGER.warn("Slot0 {} Slot1 {}", slot0, slot1);

        // If full & using void upgrade then items go into slot 2 (delete them all!)
        if (!this.getItem(2).isEmpty()) {
            this.setItem(2, ItemStack.EMPTY);
        }

        // Incoming items are placed into slot 1
        // Move items from slot 1 to slot 0 (slot 0 is bulk storage)
        if (slot1.isEmpty()) return;

        int slot1Amount = slot1.getCount();

        // If no items in slot 0, then add
        if (slot0.isEmpty()) {
            this.setItem(0, slot1.copy());
            this.setItem(1, ItemStack.EMPTY);
            this.setChanged();

        } else {
            int slot0FreeSpace = this.slot0MaxCapacity - slot0.getCount();
            int amountToMove = Math.min(slot1Amount, slot0FreeSpace);
            slot0.grow(amountToMove);
            slot1.shrink(amountToMove);
            this.setChanged();
        }
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        // if full and has void upgrade, then send all items coming in to new slot
        int freeSpace = this.getMaxItemCapacity() - this.getStoredAmount();
        if (freeSpace <= 0 && this.hasVoidUpgrade()) {
            return new int[]{0,1,2};
        }
        return new int[]{0,1};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack itemStack) {

        // Check filter
        if (!this.filterTest(itemStack)) return false;

        // Check against existing items
        //ItemStack slot0 = this.getItem(0);
        //if (!slot0.isEmpty() && !ItemStack.isSameItemSameTags(itemStack, slot0)) return false;

        // Check space in slot 0
        int freeSpace = this.getMaxItemCapacity() - this.getStoredAmount();
        return freeSpace > 0 || this.hasVoidUpgrade();
    }

    public void transferItemsToPlayer(Player player) {
         helper.transferItemsToPlayer(player);
    }

    public void transferItemsFromPlayer(Player player) {
        // Check if upgrade type
        ItemStack handItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (handItem.is(ModTags.STORAGE_BOX_UPGRADE)) {

            if (handItem.is(ModItems.STORAGE_BOX_VOID_UPGRADE)) {
                if (!this.hasVoidUpgrade()) {
                    this.items.set(this.voidUpgradeSlot, handItem.copyWithCount(1));
                    this.setChanged();
                    if (!player.isCreative()) {
                        handItem.shrink(1);
                        player.getInventory().setChanged();
                    }
                } else {
                    ItemStack voidStack = this.getItem(3);
                    int slot = player.getInventory().getSlotWithRemainingSpace(voidStack);
                    if (slot > -1) {
                        player.getInventory().getItem(slot).grow(1);
                        player.getInventory().setChanged();
                    } else {
                        slot = player.getInventory().getFreeSlot();
                        if (slot > -1) {
                            player.getInventory().setItem(slot, voidStack);
                            player.getInventory().setChanged();
                        } else {
                            helper.dropItems(this.getLevel(), voidStack);
                        }
                    }
                    voidStack.setCount(0);
                    this.setChanged();
                }
            } else if (handItem.is(ModItems.STORAGE_BOX_CAPACITY_UPGRADE)) {
                boolean canAddUpgrade = false;
                for (int i = this.capacityUpgradeStartSlot; i < this.capacityUpgradeStartSlot + this.maxCapacityUpgrades; i++) {
                    if (this.items.get(i).isEmpty()) {
                        this.items.set(i, handItem.copyWithCount(1));
                        this.setChanged();
                        canAddUpgrade = true;
                        break;
                    }
                }
                if (!player.isCreative() && canAddUpgrade) {
                    handItem.shrink(1);
                    player.getInventory().setChanged();
                } else if (!canAddUpgrade) {
                    player.displayClientMessage(Component.translatable(FXNTStorage.MOD_ID + ".storage_box_capacity_upgrade_max"), true);
                }
            }
        }

        helper.transferItemsFromPlayer(player);
    }

    public ItemStack insertItems(ItemStack itemStack) {
        if (this.filterTest(itemStack)) {
            int availableSpace = this.getMaxItemCapacity() - this.getStoredAmount();
            int srcAmount = itemStack.getCount();
            int moveAmount = Math.min(srcAmount, availableSpace);

            if (availableSpace <= 0 && hasVoidUpgrade()) {
                itemStack.setCount(0);
                return itemStack;
            }

            if (moveAmount > 0) {

                if (this.getFilterItem().isEmpty()) {
                    setFilter(itemStack);
                }

                if (this.getItem(1).isEmpty()) {
                    this.setItem(1, itemStack.copyWithCount(moveAmount));
                } else {
                    this.getItem(1).grow(moveAmount);
                }
                this.setChanged();
                itemStack.shrink(moveAmount);
            }
        }
        return itemStack;
    }

    private void clearItems() {
        // Don't clear upgrades!
        this.items.set(0, ItemStack.EMPTY);
        this.items.set(1, ItemStack.EMPTY);
        this.items.set(2, ItemStack.EMPTY);
    }

    public void controllerSetItems(ItemStack itemStack) {
        // Resetting the amount stored in this box from a storage controller
        // Wipe anything in slot 0 and 1 and then add as normal
        this.clearItems();
        this.insertItems(itemStack);
        this.setChanged();
    }

    public ItemStack controllerRemoveItems(int amount) {
        if (amount <= this.storedAmount) {
            int newAmount = this.storedAmount - amount;
            ItemStack newStack = this.filterItem.copyWithCount(newAmount);
            this.clearItems();
            this.insertItems(newStack);
            this.setChanged();
            return newStack.copyWithCount(amount);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack controllerRemoveItemsNoUpdate() {
        this.clearItems();
        return ItemStack.EMPTY;
    }

    public void removeFilter() {
        FXNTStorage.LOGGER.info("Remove Filter");
        this.filterItem = ItemStack.EMPTY;
    }

    public void setFilter(ItemStack itemStack) {
        FXNTStorage.LOGGER.info("Set Filter {}", itemStack.copyWithCount(1));
        this.filterItem = itemStack.copyWithCount(1);
    }

    public boolean filterTest(ItemStack stack) {
        // Prevent inception
        if (stack.is(ModTags.STORAGE_BOX_ITEM) || stack.is(ModTags.STORAGE_BOX_UPGRADE)) {
            return false;
        }

        if (!this.filterItem.isEmpty() && !ItemStack.isSameItemSameTags(stack, this.filterItem)) {
            return false;
        }

        return true;
    }
}
