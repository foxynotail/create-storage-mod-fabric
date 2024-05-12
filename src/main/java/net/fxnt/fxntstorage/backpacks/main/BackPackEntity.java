package net.fxnt.fxntstorage.backpacks.main;

import net.fxnt.fxntstorage.backpacks.upgrades.BackPackAsBlockUpgradeHandler;
import net.fxnt.fxntstorage.backpacks.upgrades.UpgradeItem;
import net.fxnt.fxntstorage.containers.util.ImplementedContainer;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BackPackEntity extends BlockEntity implements ImplementedContainer, Nameable {
    private int slotCount = BackPackBlock.getSlotCount();
    public NonNullList<ItemStack> items;
    private final BlockPos pos;
    public int containerSlotCount;
    private static final int currentTick = 0;
    private static int lastTick = 0;
    private static final int updateEveryXTicks = 30;
    private boolean doTick = false;
    public Container container;
    public int[] SIDED_SLOTS = new int[containerSlotCount];
    public NonNullList<String> upgrades = NonNullList.create();
    private Component customName;
    private final int size;

    private final Block block;

    public int maxStackSize = 64;

    public BackPackEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.BACK_PACK_ENTITY, pos, blockState);
        this.items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        this.size = this.items.size();
        this.pos = pos;
        this.containerSlotCount = this.slotCount - 30; // 24 Tool Slots, 6 Upgrade Slots
        this.block = blockState.getBlock();
        if (this.block instanceof BackPackBlock backPackBlock) {
            this.maxStackSize = backPackBlock.getMaxStackSize();
        }
    }

    public void setCustomName(Component hoverName) {
        this.customName = hoverName;
    }

    public Component getDisplayName() {
        if (this.customName != null) return this.customName;

        Level blockLevel = this.level;
        if (blockLevel != null) {
            return this.block.getCloneItemStack(this.level, this.pos, this.getBlockState()).getHoverName();
        } else {
            return new ItemStack(ModBlocks.BACK_PACK_ITEM).getHoverName();
        }
    }

    @Override
    public Component getName() {
        return getDisplayName();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return getDisplayName();
    }

    public void setData(int slotCount, int maxStackSize) {
        // Called when Block Creates Entity
        this.slotCount = slotCount;
        this.maxStackSize = maxStackSize;
    }

    public NonNullList<String> getUpgrades() {
        return this.upgrades;
    }


    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
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
    }

    // Serialize the BlockEntity
    @Override
    public void saveAdditional(CompoundTag tag) {
        tag = saveEverything(tag);
        super.saveAdditional(tag);
    }

    public CompoundTag saveEverything(CompoundTag tag) {

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

        ListTag upgradesList = new ListTag();
        for (int i = 0; i < this.upgrades.size(); i++) {
            upgradesList.add(i, StringTag.valueOf(this.upgrades.get(i)));
        }
        tag.put("Upgrades", upgradesList);
        tag.putInt("maxStackSize", this.maxStackSize);

        return tag;
    }

    public ItemStack saveToItemStack(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement("BlockEntityTag");
        tag = saveEverything(tag);
        // Save custom display name
        if (this.customName != null) {
            CompoundTag displayTag = stack.getOrCreateTagElement("display");
            displayTag.putString("Name", Component.Serializer.toJson(this.customName));
        }

        return stack;
    }
    public void refreshUpgrades() {
        this.upgrades.clear();
        int UPGRADE_SLOT_START_INDEX = BackPackBlock.containerSlotCount + BackPackBlock.toolSlotCount;
        int UPGRADE_SLOT_END_INDEX = UPGRADE_SLOT_START_INDEX + BackPackBlock.upgradeSlotCount;

        for (int i = UPGRADE_SLOT_START_INDEX; i < UPGRADE_SLOT_END_INDEX; i++) {
            ItemStack itemStack = this.items.get(i);
            if (itemStack.getItem() instanceof UpgradeItem upgradeItem) {
                // ADD TO UPGRADE CACHE
                String upgradeName = upgradeItem.getUpgradeName();
                if (!this.upgrades.contains(upgradeName)) {
                    this.upgrades.add(upgradeName);
                }
            }
        }
    }


    private boolean isBeingPickedUp = false;

    public void setBeingPickedUp(boolean beingPickedUp) {
        isBeingPickedUp = beingPickedUp;
    }

    public boolean isBeingPickedUp() {
        return isBeingPickedUp;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void initializeSlotsForAllDirections() {
        SIDED_SLOTS = new int[containerSlotCount];
        for (int i = 0; i < containerSlotCount; i++) {
            SIDED_SLOTS[i] = i;
        }
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        initializeSlotsForAllDirections();
        // Implement locking system for certain slots if adding filter, or if adding ability to lock / memorize slots
        if (SIDED_SLOTS.length < 1) return new int[]{0};
        return SIDED_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        initializeSlotsForAllDirections();
        if (!filterTest(level, itemStack)) return false;
        //if (!insertItemsTest(itemStack)) return false;
        return SIDED_SLOTS.length >= 1;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        initializeSlotsForAllDirections();
        return SIDED_SLOTS.length >= 1;
    }
    public static boolean filterTest(Level level, ItemStack stack) {
        // Test to see if we're allowing this item into the backpack
        // Use to prevent inception, needs to be called on any backpack interaction (including quickmove / mouse interaction)
        // Add filtering in here too
        // Prevent inception
        return !(stack.getItem() instanceof BackPackItem);
    }
    private boolean initializedBlock = false;
    public <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockEntity blockEntity) {
        if (level != null) {
            if (!level.isClientSide) {
                lastTick++;
                if (lastTick >= updateEveryXTicks) {
                    lastTick = 0;
                    doTick = true;
                }
                if (!doTick) return;

                if (!initializedBlock) {
                    level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), BackPackBlock.UPDATE_ALL);
                    initializedBlock = true;
                }

                if (this.upgrades.contains(Util.MAGNET_UPGRADE)) {
                    BackPackAsBlockUpgradeHandler upgradeHandler = new BackPackAsBlockUpgradeHandler(this);
                    upgradeHandler.applyMagnetUpgrade();
                }
                doTick = false;
            }
        }
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStackSize;
    }

    private boolean inUpgradeSlotRange(int slot) {
        return slot >= Util.UPGRADE_SLOT_START_RANGE && slot < Util.UPGRADE_SLOT_END_RANGE;
    }


    @Override
    public void setChanged() {
        refreshUpgrades();
        super.setChanged();
    }
    /*
        // For hopper, if stack count in slot is less than max slot size, then send through reduced stack count so hopper can fill this slot
        // EG. if stack limit is 100, and slot has 99, send count of 63 (1 remaining)
        if (slot < 0 && slot >= this.items.size()) return ItemStack.EMPTY;

        ItemStack itemStack = this.items.get(slot);

        int remainingSpaceInSlot = this.getMaxStackSize() - itemStack.getCount();
        //                                  100            -        1          = 99;
        int fakeSlotCount = this.items.get(slot).getCount();
        if (remainingSpaceInSlot >= 64) {
            fakeSlotCount = 1;
        } else {
            fakeSlotCount = remainingSpaceInSlot;
        }
        if (fakeSlotCount != itemStack.getCount()) {
            itemStack = new ItemStack(itemStack.getItem(), fakeSlotCount);

            // Set this fake itemstack to the fakeItems list
            fakeItems.set(slot, itemStack);
        }

        return itemStack;
        */

    // If we made a full copy imaginary inventory of all of the items:
    // Update the existing slots with new items added to the imaginary slots or remove items from existing slots if the counts go down.
    // Incoming: Will only work if slots free in inventory, unless we have one extra slot that can be any of the non full stacks
    // Outgoing: no issues

    // How to solve having no space left
    // If always 1 blank slot, how do we know what item it's allowed to be?
    // If a hopper puts an item into the 1 blank slot and it doesn't match any of the existing stacks, then that item will vanish
    // So that slot needs to only allow items in that haven't got a full stack size
    // How?
    // Hook into the mayPlace and say false if the item being checked isn't in the list of items we've actually got

    // REMEMBER
    // Hoppers / chutes don't always use setItem, the often just change the count of the itemstack directly in this inventory


}
