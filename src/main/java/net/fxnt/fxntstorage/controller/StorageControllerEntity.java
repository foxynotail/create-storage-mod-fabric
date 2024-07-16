package net.fxnt.fxntstorage.controller;

import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.containers.util.ContainerSaveContents;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntity;
import net.fxnt.fxntstorage.storage_network.StorageNetwork;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class StorageControllerEntity extends BlockEntity implements WorldlyContainer, ContainerSaveContents {

    public int lastTick = 0;
    public int updateEveryXTicks = Config.SIMPLE_STORAGE_NETWORK_UPDATE_TIME.get();
    public boolean doTick = false;
    public long lastInteractTime = 0;
    public UUID lastInteractPlayer = UUID.randomUUID();
    public byte lastInteractType = -1;
    public int interactWindow = 600;
    public StorageNetwork storageNetwork;
    public int networkVersion;
    public int lastNetworkVersion;
    boolean networkUpdating = false;
    boolean networkChanged = false;
    public int slotCount = 0;
    public NonNullList<ItemStack> items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    private int blankSlot = 0;
    public int maxItemCapacity = 0; // Get maximum capacity of connected box with largest capacity

    public StorageControllerEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.STORAGE_CONTROLLER_ENTITY, pos, blockState);
        updateStorageNetwork();
    }

    public void updateStorageNetwork() {
        this.networkUpdating = true;
        // Save copy of items before updating (might be an issue if slots change order on update)
        if (this.storageNetwork == null) {
            this.storageNetwork = new StorageNetwork(this);
        } else {
            this.lastNetworkVersion = this.storageNetwork.networkVersion;
            this.storageNetwork.updateStorageNetwork();
        }
        this.networkVersion = this.storageNetwork.networkVersion;
        this.slotCount = this.storageNetwork.items.size()+1;
        this.items = this.storageNetwork.items;
        this.items.add(ItemStack.EMPTY);
        // Blank Slot used for inserting new items
        this.blankSlot = this.items.size() - 1;
        this.maxItemCapacity = this.storageNetwork.maxItemCapacity;

        if (this.networkVersion != this.lastNetworkVersion) {
            this.networkChanged = true;
        }
        this.networkUpdating = false;
    }

    public boolean networkBusy() {
        return this.networkUpdating || this.networkChanged;
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

    public <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockEntity blockEntity) {

        if (level.isClientSide) return;
        moveItems();

        this.lastTick++;

        if (this.lastTick >= this.updateEveryXTicks) {
            this.lastTick = 0;
            this.doTick = true;
        }
        if (!this.doTick) return;

        updateStorageNetwork();
        // If the network has changed, reset it here
        if (this.networkChanged) this.networkChanged = false;
        this.doTick = false;

    }

    private void moveItems() {
        if (networkBusy()) return;
        // Move any items in slot 0 to proper slot
        if (this.items.size() <= 0) return;
        if (this.blankSlot > this.items.size() - 1) return;
        ItemStack insertSlot = this.items.get(this.blankSlot);
        if (insertSlot.isEmpty()) return;
        this.storageNetwork.insertItems(insertSlot);
        this.items.set(this.blankSlot, ItemStack.EMPTY);
        updateStorageNetwork();
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        int[] slots = new int[this.slotCount];
        for (int i = 0; i < this.slotCount; i++) {
            slots[i] = i;
        }

        if (networkBusy()) return new int[]{};
        return slots;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot > this.items.size() - 1) return ItemStack.EMPTY;
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
        //FXNTStorage.LOGGER.info("Can Take Items: {} {}", index, itemStack);
        return index != this.blankSlot && index >= 0 && index <= this.items.size();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

        if (slot == this.blankSlot) {
            this.items.set(this.blankSlot, stack);
            this.setChanged();
            return;
        }

        if (networkBusy()) {
            return;
        }

        StorageNetwork.StorageNetworkItem networkItem = this.storageNetwork.boxes.get(slot);
        SimpleStorageBoxEntity simpleStorageBoxEntity = networkItem.simpleStorageBoxEntity;
        if (simpleStorageBoxEntity == null) return;

        simpleStorageBoxEntity.controllerSetItems(stack);

        // Refresh the network to reflect the items here
        updateStorageNetwork();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {

        if (networkBusy()) {
            return ItemStack.EMPTY;
        }

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

        if (networkBusy()) {
            return ItemStack.EMPTY;
        }

        StorageNetwork.StorageNetworkItem networkItem = this.storageNetwork.boxes.get(slot);
        SimpleStorageBoxEntity simpleStorageBoxEntity = networkItem.simpleStorageBoxEntity;
        if (simpleStorageBoxEntity != null) {
            return simpleStorageBoxEntity.controllerRemoveItemsNoUpdate();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void clearContent() {
        updateStorageNetwork();
    }

    //private SimpleStorageBoxEntity getSimpleStorageBoxBySlot(int slot) {
    //    return this.storageNetwork.boxes.get(slot);
    //}

    public void transferItemsToPlayer(Player player) {

        ItemStack srcStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (srcStack.isEmpty()) {
            srcStack = this.storageNetwork.getFirstItemStack();
            if (srcStack.isEmpty()) return;
        }
        int srcAmount = this.storageNetwork.getAmountOfItem(srcStack);

        if (Util.getMillis() < this.lastInteractTime + this.interactWindow && player.getUUID().equals(this.lastInteractPlayer) && this.lastInteractType == 0) {
            transferAllItemsToPlayer(player, srcStack, srcAmount);
        } else {
            this.lastInteractTime = Util.getMillis();
            this.lastInteractPlayer = player.getUUID();
            this.lastInteractType = 0;
            if (player.isShiftKeyDown()) srcAmount = 1;
            doTransferToPlayer(player, srcStack, srcAmount, false);
        }
    }
    public void transferAllItemsToPlayer(Player player, ItemStack srcStack, int srcAmount) {

        while(srcAmount > 0) {
            if (!doTransferToPlayer(player, srcStack, srcAmount, true)) break;
            srcAmount = this.storageNetwork.getAmountOfItem(srcStack);
        }
    }
    public boolean doTransferToPlayer(Player player, ItemStack srcStack, int srcAmount, boolean transferAll) {

        int playerSlot = player.getInventory().selected;
        ItemStack playerStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        int availableSpace = srcStack.getMaxStackSize() - playerStack.getCount();

        if ((!playerStack.isEmpty() && !ItemStack.isSameItemSameTags(srcStack, playerStack)) || availableSpace <= 0) {
            playerSlot = player.getInventory().getSlotWithRemainingSpace(srcStack);
            if (playerSlot < 0) {
                playerSlot = player.getInventory().getFreeSlot();
            }
            if (playerSlot < 0) {
                if (!transferAll) {
                    int amountTaken = this.storageNetwork.takeItem(playerStack, srcAmount);
                    dropItems(this.getLevel(), srcStack.copyWithCount(amountTaken));
                    updateStorageNetwork();
                }
                return false;
            }
            playerStack = player.getInventory().getItem(playerSlot);
            availableSpace = srcStack.getMaxStackSize() - playerStack.getCount();
        }

        int amountToTake = Math.min(availableSpace, srcAmount);

        if (amountToTake > 0) {
            int amountTaken = this.storageNetwork.takeItem(playerStack, amountToTake);
            if (!playerStack.isEmpty()) {
                playerStack.grow(amountTaken);
            } else {
                player.getInventory().setItem(playerSlot, srcStack.copyWithCount(amountTaken));
            }
            player.getInventory().setChanged();
            updateStorageNetwork();
            return true;
        }
        return false;
    }
    public void dropItems(Level level, ItemStack itemStack) {
        Direction facing = StorageControllerBlock.getDirectionFacing(this.getBlockState());
        float xOffset = 0.5f;
        float zOffset = 0.5f;
        if (facing == Direction.NORTH) zOffset = 0.5f -0.8f;
        if (facing == Direction.WEST) xOffset = 0.5f -0.8f;
        if (facing == Direction.EAST) xOffset = 1.3f;
        if (facing == Direction.SOUTH) zOffset = 1.3f;

        float dropX = this.getBlockPos().getX() + xOffset;
        float dropY = this.getBlockPos().getY();
        float dropZ = this.getBlockPos().getZ() + zOffset;
        // Create Item Entities
        ItemStack dropStack = itemStack.split(itemStack.getCount());
        ItemEntity droppedItems = new ItemEntity(level, dropX, dropY, dropZ, dropStack);
        Vec3 motion = droppedItems.getDeltaMovement();
        droppedItems.push(-motion.x, -motion.y, -motion.z);
        level.addFreshEntity(droppedItems);
    }
    public void transferItemsFromPlayer(Player player) {
        ItemStack handItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (Util.getMillis() < this.lastInteractTime + this.interactWindow
                && player.getUUID().equals(this.lastInteractPlayer)
                && this.lastInteractType == 1
                && handItem.isEmpty()
        ) {
            transferAllItemsFromPlayer(player);
        } else if (!handItem.isEmpty()) {
            this.lastInteractTime = Util.getMillis();
            this.lastInteractPlayer = player.getUUID();
            this.lastInteractType = 1;
            doTransferItemsFromPlayer(player, handItem);
        }

    }
    public void transferAllItemsFromPlayer(Player player) {
        for (int i = 0; i <= player.getInventory().getContainerSize(); i++) {
            ItemStack slotStack = player.getInventory().getItem(i);
            doTransferItemsFromPlayer(player, slotStack);
        }
    }
    private void doTransferItemsFromPlayer(Player player, ItemStack srcStack) {
        this.storageNetwork.insertItems(srcStack);
        player.getInventory().setChanged();
        updateStorageNetwork();
    }
}
