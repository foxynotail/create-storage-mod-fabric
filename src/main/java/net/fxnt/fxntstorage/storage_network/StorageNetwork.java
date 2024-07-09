package net.fxnt.fxntstorage.storage_network;

import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.controller.StorageControllerEntity;
import net.fxnt.fxntstorage.controller.StorageInterfaceEntity;
import net.fxnt.fxntstorage.init.ModTags;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StorageNetwork {

    public final StorageControllerEntity controller;
    public final Level level;
    private final int searchRange = Config.SIMPLE_STORAGE_NETWORK_RANGE.get();
    public final int baseCapacity = 32;
    public final int itemStackSize = 64;
    public Set<BlockPos> components = new HashSet<>();
    public NonNullList<SimpleStorageBoxEntity> boxes = NonNullList.create();
    public NonNullList<ItemStack> items = NonNullList.create();
    public int maxItemCapacity = itemStackSize * baseCapacity; // Get maximum capacity of connected box with largest capacity

    public StorageNetwork(StorageControllerEntity controller) {
        this.controller = controller;
        this.level = controller.getLevel();
        this.components = getConnectedComponents(this.level, this.controller.getBlockPos());
        getBoxes(this.level, this.components);
        getItems(this.boxes);
        //FXNTStorage.LOGGER.info("Components: {} Boxes: {} Items: {}", this.components.size(), this.boxes.size(), this.items);
    }

    private Set<BlockPos> getConnectedComponents(Level level, BlockPos origin) {

        if (level == null) return new HashSet<>();
        List<BlockPos> positions = new ArrayList<>();
        positions.add(origin);
        int lastCheckedPos = 0;
        int distanceToController = 0;

        while (distanceToController < this.searchRange && lastCheckedPos < positions.size()) {

            for (int i = lastCheckedPos; i < positions.size(); i++) {

                BlockPos checkPos = positions.get(i);

                BlockPos pos = checkPos.above();
                if (isNetworkComponent(level.getBlockState(pos))) addPosition(positions, pos);

                pos = checkPos.below();
                if (isNetworkComponent(level.getBlockState(pos))) addPosition(positions, pos);

                pos = checkPos.north();
                if (isNetworkComponent(level.getBlockState(pos))) addPosition(positions, pos);

                pos = checkPos.south();
                if (isNetworkComponent(level.getBlockState(pos))) addPosition(positions, pos);

                pos = checkPos.east();
                if (isNetworkComponent(level.getBlockState(pos))) addPosition(positions, pos);

                pos = checkPos.west();
                if (isNetworkComponent(level.getBlockState(pos))) addPosition(positions, pos);

                lastCheckedPos = i;

            }
            lastCheckedPos++;

            distanceToController++;

        }

        return new HashSet<>(positions);
    }

    private void addPosition(List<BlockPos> list, BlockPos pos) {
        if (!list.contains(pos)) list.add(pos);
    }

    public void getBoxes(Level level, Set<BlockPos> components) {
        if (level == null) return;

        for (BlockPos blockPos : components) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof SimpleStorageBoxEntity boxEntity) {
                this.boxes.add(boxEntity);
            } else if (blockEntity instanceof StorageInterfaceEntity interfaceEntity) {
                interfaceEntity.setController(this.controller);
            }
        }
        this.boxes.sort((o1, o2) -> Integer.compare(o2.hashCode(), o1.hashCode()));
    }
    private void addBox(List<SimpleStorageBoxEntity> list, SimpleStorageBoxEntity blockEntity) {
        if (!list.contains(blockEntity)) list.add(blockEntity);
    }

    private void getItems(NonNullList<SimpleStorageBoxEntity> boxes) {

        for(SimpleStorageBoxEntity blockEntity : boxes) {
            ItemStack filterItem = blockEntity.getFilterItem();
            int storedAmount = blockEntity.getStoredAmount();
            ItemStack itemToAdd = ItemStack.EMPTY;
            if (!filterItem.isEmpty()) {
                itemToAdd = filterItem.copyWithCount(storedAmount);
            }
            this.items.add(itemToAdd.copyWithCount(storedAmount));
            // Update maxItemCapacity to the maximum found
            if (blockEntity.getMaxItemCapacity() > this.maxItemCapacity) {
                this.maxItemCapacity = blockEntity.getMaxItemCapacity();
            }
        }
    }

    public ItemStack insertItems(ItemStack itemStack) {
        // Iterate through all boxes looking for matching boxes with available space or empty boxes
        List<SimpleStorageBoxEntity> emptyBoxes = new ArrayList<>();
        for (SimpleStorageBoxEntity blockEntity : this.boxes) {
            ItemStack filterItem = blockEntity.getFilterItem();
            if (filterItem.isEmpty()) {
                // Prefer adding into boxes that already contain item so do empty boxes last
                addBox(emptyBoxes, blockEntity);
            } else if (blockEntity.filterTest(itemStack)) {
                itemStack = blockEntity.insertItems(itemStack);
            }
        }

        if (itemStack.getCount() > 0) {
            // Check empty boxes
            for (SimpleStorageBoxEntity blockEntity : emptyBoxes) {
                itemStack = blockEntity.insertItems(itemStack);
            }
        }

        return itemStack;

    }

    public boolean canInsertItems(ItemStack itemStack) {
        ItemStack testStack = itemStack.copy();
        int amountCanInsert = 0;
        boolean canVoid = false;
        for (SimpleStorageBoxEntity blockEntity : this.boxes) {
            ItemStack filterItem = blockEntity.getFilterItem();
            if (filterItem.isEmpty() || blockEntity.filterTest(testStack)) {
                int availableSpace = blockEntity.getMaxItemCapacity() - blockEntity.getStoredAmount();
                if (blockEntity.hasVoidUpgrade()) {
                    canVoid = true;
                    break;
                }
                amountCanInsert += availableSpace;
            }
        }

        if (canVoid) amountCanInsert = Integer.MAX_VALUE;
        return amountCanInsert >= itemStack.getCount();
    }

    public ItemStack getFirstItemStack() {
        ItemStack itemStack = ItemStack.EMPTY;
        int i = 0;
        while(itemStack.isEmpty() && i < this.items.size()) {
            itemStack = this.items.get(i);
            i++;
        }
        return itemStack;
    }

    public int takeItem(ItemStack itemStack, int amount) {

        // Iterate through all boxes looking for matching boxes
        int amountTaken = 0;
        int i = 0;
        // If player stack is empty, set first non-empty item as item to remove
        while(itemStack.isEmpty()) {
            itemStack = this.items.get(i);
            i++;
        }

        for (SimpleStorageBoxEntity blockEntity : this.boxes) {
            if (blockEntity.filterTest(itemStack)) {
                int availableItems = blockEntity.getStoredAmount();
                int amountToTake = Math.min(availableItems, amount);
                blockEntity.controllerRemoveItems(amountToTake);
                amount -= amountToTake;
                amountTaken += amountToTake;
            }
        }
        return amountTaken;
    }

    public int getAmountOfItem(ItemStack itemStack) {
        int amount = 0;
        for (SimpleStorageBoxEntity blockEntity : this.boxes) {
            if (blockEntity.filterTest(itemStack)) {
                amount += blockEntity.getStoredAmount();
            }
        }
        return amount;
    }

    private boolean isNetworkComponent(BlockState blockState) {
        return blockState.is(ModTags.STORAGE_NETWORK_BLOCK);
    }


}
