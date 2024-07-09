package net.fxnt.fxntstorage.controller;

import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.containers.util.ContainerSaveContents;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntity;
import net.fxnt.fxntstorage.storage_network.StorageNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StorageInterfaceEntity extends BlockEntity implements WorldlyContainer, ContainerSaveContents {

    public int lastTick = 0;
    public int updateEveryXTicks = Config.SIMPLE_STORAGE_NETWORK_UPDATE_TIME.get();
    public boolean doTick = false;
    public int slotCount = 0;
    public NonNullList<ItemStack> items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    private int blankSlot = 0;
    public int maxItemCapacity = 0;
    private StorageControllerEntity controller = null;
    public StorageNetwork storageNetwork = null;

    public StorageInterfaceEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.STORAGE_INTERFACE_ENTITY, pos, blockState);
    }

    public void setController(StorageControllerEntity controller) {
        // Check if already has controller to prevent switching networks constantly
        if (!checkController()) {
            FXNTStorage.LOGGER.info("Set Controller: {}", controller.getBlockPos());
            this.controller = controller;
            getStorageNetwork();
        }
    }

    public void getStorageNetwork() {
        FXNTStorage.LOGGER.info("Get Network");
        this.storageNetwork = this.controller.storageNetwork;
        this.slotCount = this.controller.items.size();
        this.items = this.controller.items;
        this.blankSlot = this.items.size() - 1;
        this.maxItemCapacity = this.controller.maxItemCapacity;
    }

    private boolean checkController() {
        // Check controller still exists
        if (this.controller != null) {
            BlockEntity controllerCheck = this.getLevel().getBlockEntity(this.controller.getBlockPos());
            if (controllerCheck != null) {
                return controllerCheck.getBlockState().equals(this.controller.getBlockState());
            }
        }
        return false;
    }

    private void forgetController() {
        FXNTStorage.LOGGER.info("Forget Controller");
        this.controller = null;
        this.storageNetwork = null;
        this.slotCount = 0;
        this.items.clear();
        this.items = NonNullList.create();
        this.blankSlot = 0;
        this.maxItemCapacity = 0;
    }

    private void updateStorageNetwork() {
        this.controller.updateStorageNetwork();
        getStorageNetwork();
    }

    public <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockEntity blockEntity) {
        if (level.isClientSide) return;

        this.lastTick++;

        if (this.lastTick >= this.updateEveryXTicks) {
            this.lastTick = 0;
            this.doTick = true;
        }
        if (!this.doTick) return;

        if((this.controller != null || this.storageNetwork != null) && !checkController()) {
            forgetController();
        } else if (this.controller != null) {
            getStorageNetwork();
        }
        this.doTick = false;



    }

    @Override
    public int getMaxStackSize() {
        // NEED THIS TO PREVENT FABRIC TRANSFER API TAKING ENTIRE CONTENTS IN 1 GO
        return this.maxItemCapacity;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public int getContainerSize() {
        return this.slotCount;
    }

    @Override
    public boolean isEmpty() {
        int totalAmount = 0;
        for (ItemStack itemStack : this.items) {
            totalAmount += itemStack.getCount();
        }
        return totalAmount <= 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    private void moveItems() {
        // Move any items in slot 0 to proper slot
        ItemStack insertSlot = this.items.get(this.blankSlot);
        if (insertSlot.isEmpty()) return;
        this.storageNetwork.insertItems(insertSlot);
        this.items.set(this.blankSlot, ItemStack.EMPTY);
        updateStorageNetwork();
    }

    @Override
    public void setChanged() {
        moveItems();
        super.setChanged();
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        int[] slots = new int[this.slotCount];
        for (int i = 0; i < this.slotCount; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack itemStack) {
        return index == this.blankSlot && this.storageNetwork.canInsertItems(itemStack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack itemStack, Direction direction) {
        return index != this.blankSlot && index >= 0 && index <= this.items.size();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

        if (slot == this.blankSlot) {
            this.items.set(this.blankSlot, stack);
            this.setChanged();
            return;
        }

        SimpleStorageBoxEntity simpleStorageBoxEntity = getSimpleStorageBoxBySlot(slot);
        simpleStorageBoxEntity.controllerSetItems(stack);

        // Refresh the network to reflect the items here
        updateStorageNetwork();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {

        ItemStack slotItem = this.items.get(slot);
        int amountAvailable = slotItem.getCount();

        if (slot == this.blankSlot) {
            slotItem.shrink(amount);
            this.setChanged();
            return slotItem;
        }

        int amountTaken = this.storageNetwork.takeItem(slotItem, amount);
        ItemStack remainingItems = slotItem.copyWithCount(amountAvailable - amountTaken);
        updateStorageNetwork();
        return remainingItems;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot == this.blankSlot) {
            this.items.set(this.blankSlot, ItemStack.EMPTY);
            this.setChanged();
            return ItemStack.EMPTY;
        }
        return getSimpleStorageBoxBySlot(slot).controllerRemoveItemsNoUpdate();
    }

    @Override
    public void clearContent() {
        updateStorageNetwork();
    }

    private SimpleStorageBoxEntity getSimpleStorageBoxBySlot(int slot) {
        return this.storageNetwork.boxes.get(slot);
    }

}
