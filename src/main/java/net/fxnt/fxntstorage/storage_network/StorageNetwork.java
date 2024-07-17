package net.fxnt.fxntstorage.storage_network;

import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.controller.StorageControllerEntity;
import net.fxnt.fxntstorage.controller.StorageInterfaceEntity;
import net.fxnt.fxntstorage.init.ModTags;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class StorageNetwork {

    public final StorageControllerEntity controller;
    public Level level;
    public BlockPos controllerPos;
    private final int searchRange = Config.SIMPLE_STORAGE_NETWORK_RANGE.get();
    public final int baseCapacity = 32;
    public final int itemStackSize = 64;
    public int blankSlot = -1;
    public Set<BlockPos> components = new HashSet<>();
    public NonNullList<StorageNetworkItem> boxes = NonNullList.create();
    public NonNullList<ItemStack> items = NonNullList.create();
    private HashMap<Integer, Integer> boxSlots = new HashMap<>();
    public int networkVersion = 0;
    private int tick = 0;

    public StorageNetwork(StorageControllerEntity controller) {
        this.controller = controller;
        this.level = controller.getLevel();
        this.controllerPos = controller.getBlockPos();
        this.components = getConnectedComponents(this.level, this.controllerPos);
        getBoxes(this.level, this.components);
    }

    public void tick() {
        // Check if boxes removed every tick
        checkBoxes();
        // Move items from blank stack into a matching / empty box
        moveNewItems();
        if (tick >=  Config.SIMPLE_STORAGE_NETWORK_UPDATE_TIME.get()) {
            // Get new components every 20 ticks
            refreshStorageNetwork();
            tick = 0;
        }
        tick++;
    }

    private void moveNewItems() {
        // Move items in empty stack
        if (this.items.isEmpty()) return;
        ItemStack blankStack = this.items.get(this.blankSlot);
        if (!blankStack.isEmpty()) {
            this.insertItems(blankStack);
            blankStack.setCount(0);
        }
    }

    private void refreshStorageNetwork() {
        this.level = this.controller.getLevel();
        this.controllerPos = this.controller.getBlockPos();
        Set<BlockPos> newComponents = getConnectedComponents(this.level, this.controllerPos);
        if (!newComponents.equals(this.components)) {
            this.components = newComponents;
            this.networkVersion++;
            if (this.networkVersion > 999) this.networkVersion = 0;
        }
        getBoxes(this.level, this.components);
    }

    private void checkBoxes() {

        boolean networkChanged = false;
        for (StorageNetworkItem networkItem : this.boxes) {

            BlockEntity blockEntity = this.level.getBlockEntity(networkItem.blockPos);
            if (blockEntity == null) {
                networkChanged = true;
                break;
            } else if (blockEntity instanceof SimpleStorageBoxEntity simpleStorageBoxEntity) {
                if (!simpleStorageBoxEntity.equals(networkItem.simpleStorageBoxEntity)) {
                    networkChanged = true;
                    break;
                }
            } else {
                networkChanged = true;
                break;
            }
        }
        if (networkChanged) {
            refreshStorageNetwork();
        }
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

    private void getBoxes(Level level, Set<BlockPos> components) {
        if (level == null) return;
        this.boxes.clear();
        this.items.clear();
        this.boxSlots.clear();

        int i = 0;
        int b = 0;
        for (BlockPos blockPos : components) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof SimpleStorageBoxEntity boxEntity) {
                StorageNetworkItem networkItem = new StorageNetworkItem(boxEntity);
                this.boxes.add(b, networkItem);
                this.items.add(i, networkItem.item0);
                this.boxSlots.put(i, b);
                i++;
                this.items.add(i, networkItem.item1);
                this.boxSlots.put(i, b);
                i++;
                b++;
            } else if (blockEntity instanceof StorageInterfaceEntity interfaceEntity) {
                interfaceEntity.setController(this.controller);
            }
        }

        // Add Blank Slot to void items
        this.items.add(i, ItemStack.EMPTY);
        this.blankSlot = i;
    }
    private void addBox(List<SimpleStorageBoxEntity> list, SimpleStorageBoxEntity blockEntity) {
        if (!list.contains(blockEntity)) list.add(blockEntity);
    }

    public void insertItems(ItemStack itemStack) {
        // Iterate through all boxes looking for matching boxes with available space or empty boxes
        List<SimpleStorageBoxEntity> emptyBoxes = new ArrayList<>();
        for (StorageNetworkItem networkItem : this.boxes) {
            SimpleStorageBoxEntity blockEntity = networkItem.simpleStorageBoxEntity;
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
    }

    private boolean isNetworkComponent(BlockState blockState) {
        return blockState.is(ModTags.STORAGE_NETWORK_BLOCK);
    }

    public boolean canPlaceItem(int slot, ItemStack itemStack) {

        // If a full box exists that has a void upgrade and an empty box exists, always prefer the void box to prevent empty boxes being filled
        int boxWithVoid = -1;
        int boxWithSpace = -1;
        for (int i = 0; i < this.boxes.size(); i++) {
            StorageNetworkItem networkItem = this.boxes.get(i);
            SimpleStorageBoxEntity blockEntity = networkItem.simpleStorageBoxEntity;
            if (!blockEntity.filterItem.isEmpty() && blockEntity.filterTest(itemStack)) {
                int freeSpace = blockEntity.getMaxItemCapacity() - blockEntity.getStoredAmount();
                if (blockEntity.hasVoidUpgrade() && freeSpace <= 0) {
                    boxWithVoid = i;
                } else if (freeSpace > 0) {
                    // However if another box that matches the item exists that isn't full prefer that one over the void one
                    boxWithSpace = i;
                    boxWithVoid = -1;
                    break;
                }
            }
        }

        // Void Items if Trying to put in Blank Slot
        if (slot == this.blankSlot) {
            return boxWithVoid >= 0;
        }

        // Can place in empty stack
        int boxSlot = this.boxSlots.get(slot);
        if (boxWithSpace >= 0 && boxWithSpace != boxSlot) return false;
        if (boxWithVoid >= 0 && boxWithVoid != boxSlot) return false;

        // Determine if 1 or 2
        int itemSlot = 0;
        if ((slot & 1) != 0) itemSlot = 1;
        return this.boxes.get(boxSlot).simpleStorageBoxEntity.canPlaceItem(itemSlot, itemStack);
    }

    public boolean canTakeItem(int slot, ItemStack itemStack) {
        // Prevent taking from blank slot
        if (slot == this.blankSlot) return false;
        int boxSlot = this.boxSlots.get(slot);
        int itemSlot = 0;
        if ((slot & 1) != 0) itemSlot = 1;
        return this.boxes.get(boxSlot).simpleStorageBoxEntity.canTakeItemThroughFace(itemSlot, itemStack, Direction.NORTH);
    }

    public void setItem(int slot, ItemStack itemStack) {
        // Put void items into blank slot
        if (slot == this.blankSlot) this.items.set(slot, itemStack);
        // Put existing items into existing box
        else {
            int boxSlot = this.boxSlots.get(slot);
            // Determine if 1 or 2
            int itemSlot = 0;
            if ((slot & 1) != 0) itemSlot = 1;
            SimpleStorageBoxEntity blockEntity = this.boxes.get(boxSlot).simpleStorageBoxEntity;
            blockEntity.setItem(itemSlot, itemStack);
            // Update this items as well as the storage box entity items
            this.items.set(slot, blockEntity.items.get(itemSlot));
        }
    }

    public ItemStack removeItem(int slot, int amount) {
        ItemStack slotStack = this.items.get(slot);
        slotStack.shrink(amount);
        return slotStack;
    }

    public ItemStack removeItemNoUpdate(int slot) {
        this.items.set(slot, ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }
    public class StorageNetworkItem {
        public SimpleStorageBoxEntity simpleStorageBoxEntity;
        public String id;

        public ItemStack item0;
        public ItemStack item1;
        public BlockPos blockPos;
        public Level level;

        public StorageNetworkItem(SimpleStorageBoxEntity simpleStorageBoxEntity) {
            this.simpleStorageBoxEntity = simpleStorageBoxEntity;
            this.level = simpleStorageBoxEntity.getLevel();
            this.blockPos = simpleStorageBoxEntity.getBlockPos();
            String dimension = "null";
            if (level != null) dimension = level.toString();
            this.id =  dimension + simpleStorageBoxEntity.pos.toShortString();
            this.item0 = simpleStorageBoxEntity.items.get(0);
            this.item1 = simpleStorageBoxEntity.items.get(1);
        }
    }
}
