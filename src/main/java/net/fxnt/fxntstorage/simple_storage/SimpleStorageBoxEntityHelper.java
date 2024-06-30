package net.fxnt.fxntstorage.simple_storage;

import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.containers.StorageBox;
import net.fxnt.fxntstorage.containers.util.EnumProperties;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SimpleStorageBoxEntityHelper<T extends SimpleStorageBoxEntity> {
    private final T instance;
    public int lastTick = 0;
    public boolean doTick = false;
    public int updateEveryXTicks = Config.STORAGE_BOX_UPDATE_TIME.get();
    public float lastInteractTime = 0;
    public UUID lastInteractPlayer = UUID.randomUUID();
    public byte lastInteractType = -1;
    public int interactWindow = 600;
    public SimpleStorageBoxEntityHelper(T blockEntity) {
        this.instance = blockEntity;
    }

    public <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockEntity blockEntity) {
        if (level != null && !level.isClientSide) {
            this.lastTick++;

            if (this.lastTick >= this.updateEveryXTicks) {
                this.lastTick = 0;
                this.doTick = true;
            }
            if (!this.doTick) return;

            BlockState currentState  = instance.getBlockState();
            level.sendBlockUpdated(instance.getBlockPos(), instance.getBlockState(), instance.getBlockState(), Block.UPDATE_ALL);

            EnumProperties.StorageUsed newStorageUsed = EnumProperties.StorageUsed.EMPTY;

            int storedAmount = instance.getStoredAmount();

            if (storedAmount >= instance.getMaxItemCapacity()) {
                newStorageUsed = EnumProperties.StorageUsed.FULL;
            } else if (!instance.getItem(1).isEmpty()) {
                newStorageUsed = EnumProperties.StorageUsed.SLOTS_FILLED;
            } else if (storedAmount > 0) {
                newStorageUsed = EnumProperties.StorageUsed.HAS_ITEMS;
            }

            if (currentState.getValue(StorageBox.STORAGE_USED) != newStorageUsed) {
                level.setBlock(blockPos, currentState.setValue(StorageBox.STORAGE_USED, newStorageUsed), 3); // 3 is the update flag
            }
            this.doTick = false;
        }
    }

    public void transferItemsToPlayer(Player player) {

        if (instance.getStoredAmount()==0) return;;

        ItemStack srcStack = instance.getItem(0);
        if (srcStack.isEmpty()) {
            srcStack = instance.getItem(1);
        }
        if (srcStack.isEmpty()) return;

        int srcAmount = Math.min(instance.getStoredAmount(), srcStack.getMaxStackSize());

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
            srcAmount = srcStack.getCount();
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
                    dropItems(instance.getLevel(), srcStack.copyWithCount(srcAmount));
                    srcStack.shrink(srcAmount);
                    instance.setChanged();
                }
                return false;
            }
            playerStack = player.getInventory().getItem(playerSlot);
            availableSpace = srcStack.getMaxStackSize() - playerStack.getCount();
        }

        int moveAmount = Math.min(availableSpace, srcAmount);

        if (moveAmount > 0) {
            if (!playerStack.isEmpty()) {
                playerStack.grow(moveAmount);
            } else {
                player.getInventory().setItem(playerSlot, srcStack.copyWithCount(moveAmount));
            }
            player.getInventory().setChanged();
            srcStack.shrink(moveAmount);
            instance.setChanged();
            return true;
        }
        return false;
    }

    public void transferItemsFromPlayer(Player player) {

        ItemStack srcStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        //FXNTStorage.LOGGER.info("Time {}, Player {} {}, Type {} {}, Src {}", Util.getMillis() - (this.lastInteractTime + this.interactWindow), player.getUUID(), this.lastInteractPlayer, 1, this.lastInteractType, srcStack);
        if (Util.getMillis() < this.lastInteractTime + this.interactWindow
                && player.getUUID().equals(this.lastInteractPlayer)
                && this.lastInteractType == 1
                && srcStack.isEmpty()
        ) {
            transferAllItemsFromPlayer(player);
        } else if (!srcStack.isEmpty()) {
            this.lastInteractTime = Util.getMillis();
            this.lastInteractPlayer = player.getUUID();
            this.lastInteractType = 1;
            doTransferItemsFromPlayer(player, srcStack);
        }
    }

    public void transferAllItemsFromPlayer(Player player) {
        for (int i = 0; i <= player.getInventory().getContainerSize(); i++) {
            ItemStack slotStack = player.getInventory().getItem(i);
            if (instance.filterTest(slotStack)) {
                doTransferItemsFromPlayer(player, slotStack);
            }
        }
    }

    private void doTransferItemsFromPlayer(Player player, ItemStack srcStack) {

        if (!instance.filterTest(srcStack)) {
            return;
        }

        int availableSpace = instance.getMaxItemCapacity() - instance.getStoredAmount();
        int srcAmount = srcStack.getCount();
        int moveAmount = Math.min(srcAmount, availableSpace);

        if (moveAmount > 0) {
            if (instance.getItem(1).isEmpty()) {
                instance.setItem(1, srcStack.copyWithCount(moveAmount));
            } else {
                instance.getItem(1).grow(moveAmount);
            }
            instance.setChanged();
            srcStack.shrink(moveAmount);
            player.getInventory().setChanged();
        }
    }


    public void dropItems(Level level, ItemStack itemStack) {
        Direction facing = SimpleStorageBox.getDirectionFacing(instance.getBlockState());
        float xOffset = 0.5f;
        float zOffset = 0.5f;
        if (facing == Direction.NORTH) zOffset = 0.5f -0.8f;
        if (facing == Direction.WEST) xOffset = 0.5f -0.8f;
        if (facing == Direction.EAST) xOffset = 1.3f;
        if (facing == Direction.SOUTH) zOffset = 1.3f;

        float dropX = instance.pos.getX() + xOffset;
        float dropY = instance.pos.getY();
        float dropZ = instance.pos.getZ() + zOffset;
        // Create Item Entities
        ItemStack dropStack = itemStack.split(itemStack.getCount());
        ItemEntity droppedItems = new ItemEntity(level, dropX, dropY, dropZ, dropStack);
        Vec3 motion = droppedItems.getDeltaMovement();
        droppedItems.push(-motion.x, -motion.y, -motion.z);
        level.addFreshEntity(droppedItems);
    }
}
