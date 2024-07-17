package net.fxnt.fxntstorage.controller;

import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.containers.util.ContainerSaveContents;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.storage_network.StorageNetwork;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class StorageControllerEntity extends BlockEntity implements WorldlyContainer, ContainerSaveContents {

    public int tick = 0;
    public long lastInteractTime = 0;
    public UUID lastInteractPlayer = UUID.randomUUID();
    public byte lastInteractType = -1;
    public int interactWindow = 600;
    public StorageNetwork storageNetwork;
    public NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);

    public StorageControllerEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.STORAGE_CONTROLLER_ENTITY, pos, blockState);
        getStorageNetwork();
    }

    public void getStorageNetwork() {
        this.storageNetwork = new StorageNetwork(this);
        this.items = this.storageNetwork.items;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.storageNetwork.items;
    }

    @Override
    public int getContainerSize() {
        return this.storageNetwork.items.size();
    }

    @Override
    public boolean isEmpty() {
        int totalAmount = 0;
        for (ItemStack itemStack : this.storageNetwork.items) {
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
        if (this.storageNetwork != null) {
            this.storageNetwork.tick();
        }

        if (this.tick >= Config.SIMPLE_STORAGE_NETWORK_UPDATE_TIME.get()) {
            this.tick = 0;
        }
        this.tick++;

    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        int[] slots = new int[this.storageNetwork.items.size()];
        for (int i = 0; i < this.storageNetwork.items.size(); i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.storageNetwork.items.get(slot);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        return this.storageNetwork.canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return this.storageNetwork.canTakeItem(slot, itemStack);
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        this.storageNetwork.setItem(slot, itemStack);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return this.storageNetwork.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.storageNetwork.removeItemNoUpdate(slot);
    }

    @Override
    public void clearContent() {
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
    }
}
