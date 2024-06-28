package net.fxnt.fxntstorage.containers.util;

import net.fxnt.fxntstorage.containers.StorageBox;
import net.fxnt.fxntstorage.containers.StorageBoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class StorageBoxEntityHelper<T extends StorageBoxEntity> {

    private final T instance;

    public StorageBoxEntityHelper(T blockEntity) {
        this.instance = blockEntity;
    }

    public void onLoad() {
        if (instance.getLevel() != null && instance.getLevel().isClientSide) {
            instance.getLevel().sendBlockUpdated(instance.getBlockPos(), instance.getBlockState(), instance.getBlockState(), Block.UPDATE_ALL);
            instance.initializeSlotsForAllDirections();
        }
    }

    public void read(CompoundTag tag) {
        instance.loadInventoryFromTag(tag);
        instance.title = tag.getString("title");
        instance.slotCount = tag.getInt("slotCount");
        instance.storedAmount = tag.getInt("storedAmount");
        instance.percentageUsed = tag.getInt("percentageUsed");
        instance.voidUpgrade = tag.getBoolean("voidUpgrade");
    }

    public void write(CompoundTag tag) {
        instance.saveInventoryToTag(tag);
        tag.putString("title", instance.title);
        tag.putInt("slotCount", instance.slotCount);
        tag.putInt("storedAmount", instance.calculateStoredAmount());
        tag.putInt("percentageUsed", instance.calculatePercentageUsed());
        tag.putBoolean("voidUpgrade", instance.voidUpgrade);
    }
    public void writeScreenOpeningData(FriendlyByteBuf buf) {
        buf.writeBlockPos(instance.pos);
        buf.writeInt(instance.slotCount);
    }

    public int calculateStoredAmount() {
        int storedAmount = 0;
        int slots = instance.getContainerSize();
        for (int i = 0; i < slots; i++) {
            storedAmount +=  instance.getItem(i).getCount();
        }
        return storedAmount;
    }
    public int calculatePercentageUsed() {
        double percentageUsed = 0;
        int totalSpace = 0;
        int usedSpace = 0;
        int slots = instance.getContainerSize();
        for (int i = 0; i < slots; i++) {
            int amountInSlot =  instance.getItem(i).getCount();
            int maxItemStackSize = instance.getMaxStackSize();
            if (!instance.getItem(i).isEmpty()) {
                maxItemStackSize = instance.getItem(i).getItem().getMaxStackSize();
            }
            totalSpace += maxItemStackSize;
            usedSpace += amountInSlot;
        }
        if (totalSpace > 0) {
            percentageUsed = ((double) usedSpace / totalSpace) * 100;
        }
        return (int) Math.round(percentageUsed);
    }

    public <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockEntity blockEntity) {
        if (level != null && !level.isClientSide) {
            instance.lastTick++;
            if (instance.voidUpgrade) {
                // If voidupgrade = true, then void off everything in last slot every tick
                instance.items.set(instance.slotCount - 1, ItemStack.EMPTY);
            }

            if (instance.lastTick >= instance.updateEveryXTicks) {
                instance.lastTick = 0;
                instance.doTick = true;
            }
            if (!instance.doTick) return;

            BlockState currentState  = instance.getBlockState();
            instance.storedAmount = calculateStoredAmount();
            level.sendBlockUpdated(instance.getBlockPos(), instance.getBlockState(), instance.getBlockState(), Block.UPDATE_ALL);
            Container container = ((Container)blockEntity);

            int totalSlots = container.getContainerSize();
            boolean allSlotsFull = true;

            int filledSlots = 0;
            for (int i = 0; i < totalSlots; i++) {
                ItemStack slot = container.getItem(i);
                if (!slot.isEmpty()) {
                    filledSlots++;
                    if (slot.getCount() < slot.getItem().getMaxStackSize()) {
                        allSlotsFull = false;
                    }
                } else {
                    allSlotsFull = false;
                }
            }
            int emptySlots = totalSlots - filledSlots;

            EnumProperties.StorageUsed newStorageUsed = EnumProperties.StorageUsed.EMPTY;

            if (allSlotsFull) {
                newStorageUsed = EnumProperties.StorageUsed.FULL;
            } else if (emptySlots == 0){
                newStorageUsed = EnumProperties.StorageUsed.SLOTS_FILLED;

            } else if (filledSlots > 0) {
                newStorageUsed = EnumProperties.StorageUsed.HAS_ITEMS;
            }

            if (currentState.getValue(StorageBox.STORAGE_USED) != newStorageUsed) {
                level.setBlock(blockPos, currentState.setValue(StorageBox.STORAGE_USED, newStorageUsed), 3); // 3 is the update flag
            }
            instance.doTick = false;
        }
    }

    public boolean canPlaceItemThroughFace(Level level, int index, ItemStack itemStack, @Nullable Direction direction) {
        instance.initializeSlotsForAllDirections();
        if (!instance.filterTest(level, itemStack)) return false;
        return instance.SLOTS_FOR_ALL_DIRECTIONS.length >= 1;
    }

    public boolean canTakeItemThroughFace(Level level, int index, ItemStack itemStack, Direction direction) {
        instance.initializeSlotsForAllDirections();
        if (!instance.filterTest(level, itemStack)) return false;
        return instance.SLOTS_FOR_ALL_DIRECTIONS.length >= 1;
    }
    public boolean transferItems(Level level, Container container, int itemSlot, Player player, boolean toPlayer) {
        Inventory inventory = player.getInventory();

        ItemStack itemStack = ItemStack.EMPTY;
        // Receive 1 item when shift clicking
        int amount = 64;
        if (player.isShiftKeyDown()) amount = 1;

        if (toPlayer) {
            itemStack = TransferItems.transferItems(container, itemSlot, inventory, amount, toPlayer);
        } else {
            itemStack = TransferItems.transferItems(inventory, itemSlot, container, amount, toPlayer);
        }
        if (!itemStack.isEmpty() && toPlayer) dropItems(level, itemStack);

        return itemStack.isEmpty();
    }


    public boolean transferItemsToPlayer(Level level, Container container, Player player) {
        ItemStack playerStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack srcStack = container.getItem(i);
            if (srcStack.isEmpty()) continue;
            if (!playerStack.isEmpty() && !ItemStack.isSameItemSameTags(srcStack, playerStack)) continue;
            if (!TransferItems.canTakeItemFromContainer(player.getInventory(), container, srcStack, i)) continue;
            if (!instance.filterTest(level, srcStack)) continue;
            if (transferItems(level, container, i, player, true)) {
                return true;
            }
        }
        return false;
    }

    public boolean transferItemsFromPlayer(Level level, Container container, Player player) {
        int inventorySlot = player.getInventory().selected;
        ItemStack srcStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!instance.filterTest(level, srcStack)) return false;
        return transferItems(level, container, inventorySlot, player, false);
    }


    private void dropItems(Level level, ItemStack itemStack) {
        Direction facing = StorageBox.getDirectionFacing(instance.getBlockState());
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
