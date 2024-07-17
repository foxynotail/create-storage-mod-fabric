package net.fxnt.fxntstorage.controller;

import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.containers.util.ContainerSaveContents;
import net.fxnt.fxntstorage.init.ModBlocks;
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

    public int tick = 0;

    public NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private StorageControllerEntity controller = null;

    public StorageInterfaceEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.STORAGE_INTERFACE_ENTITY, pos, blockState);
    }

    public void setController(StorageControllerEntity controller) {
        // Check if already has controller to prevent switching networks constantly
        if (!checkController()) {
            this.controller = controller;
        }
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
        this.controller = null;
    }

    public <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockEntity blockEntity) {

        if (level.isClientSide) return;

        if (this.tick >= Config.SIMPLE_STORAGE_NETWORK_UPDATE_TIME.get()) {
            this.tick = 0;
            if((this.controller != null) && !checkController()) {
                forgetController();
            }
        }
        this.tick++;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        if (this.controller == null) return NonNullList.create();
        return this.controller.storageNetwork.items;
    }

    @Override
    public int getContainerSize() {
        if (this.controller == null) return 0;
        return this.controller.storageNetwork.items.size();
    }

    @Override
    public boolean isEmpty() {
        if (this.controller == null) return true;
        int totalAmount = 0;
        for (ItemStack itemStack : this.controller.storageNetwork.items) {
            totalAmount += itemStack.getCount();
        }
        return totalAmount <= 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        if (this.controller == null) return new int[]{};
        return this.controller.getSlotsForFace(side);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (this.controller == null) return ItemStack.EMPTY;
        return this.controller.storageNetwork.items.get(slot);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        if (this.controller == null) return false;
        return this.controller.canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(slot, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        if (this.controller == null) return false;
        return this.controller.canTakeItemThroughFace(slot, itemStack, direction);
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        if (this.controller == null) return;
        this.controller.storageNetwork.setItem(slot, itemStack);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (this.controller == null) return ItemStack.EMPTY;
        return this.controller.storageNetwork.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (this.controller == null) return ItemStack.EMPTY;
        return this.controller.storageNetwork.removeItemNoUpdate(slot);
    }

    @Override
    public void clearContent() {

    }

}
