package net.fxnt.fxntstorage.simple_storage;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.containers.util.EnumProperties;
import net.fxnt.fxntstorage.containers.util.ImplementedContainer;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.init.ModItems;
import net.fxnt.fxntstorage.init.ModTags;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SimpleStorageBoxEntity extends BlockEntity implements ImplementedContainer, ExtendedScreenHandlerFactory {
    public String title = "Simple Storage Box";
    public BlockPos pos;
    public int tick = 0;
    public long lastInteractTime = 0;
    public UUID lastInteractPlayer = UUID.randomUUID();
    public byte lastInteractType = -1;
    public int interactWindow = 600;
    public int baseCapacity = 32;
    public int itemStackSize = 64;
    public int maxCapacity = baseCapacity; // Measured in stacks so max planks = 64 * 8000, max ender pearls = 16 * 8000
    public int maxItemCapacity = itemStackSize * maxCapacity;
    public int slot0MaxCapacity = maxItemCapacity - itemStackSize;
    public int slot0Amount = 0;
    public int slot1Amount = 0;
    public int storedAmount = 0;
    public boolean voidUpgrade = false;
    public int voidUpgradeSlot = 3;
    public int capacityUpgrades = 0;
    public int capacityUpgradeStartSlot = 4;
    public int maxCapacityUpgrades = 9;
    public int baseSlotCount = 3;
    public int slotCount = baseSlotCount + 1 + maxCapacityUpgrades; // 2 + RemainderSlot + Void Upgrade Slot + Capacity Upgrade Slots
    public NonNullList<ItemStack> items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public ItemStack filterItem = ItemStack.EMPTY;

    public SimpleStorageBoxEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.SIMPLE_STORAGE_BOX_ENTITY, pos, blockState);
        this.pos = pos;
    }

    @Override
    public int getMaxStackSize() {
        // NEED THIS TO PREVENT FABRIC TRANSFER API TAKING ENTIRE CONTENTS IN 1 GO
        return this.getMaxItemCapacity();
    }

    @Override
    public int getContainerSize() {
        // Return a higher count than actually have to enable Fabric Transfer API (particularly threshold switches) to determine capacity
        return this.getMaxItemCapacity() / this.filterItem.getMaxStackSize();
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
        // Take into account items in slot 1 as this affects items being inserted
        this.storedAmount = this.items.get(0).getCount() + this.items.get(1).getCount();
        return this.storedAmount;
    }

    public int getMaxItemCapacity() {
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
            this.slot0MaxCapacity = this.maxItemCapacity - filterItem.getMaxStackSize();
        }
        return this.maxItemCapacity;
    }

    public ItemStack getFilterItem() {
        return this.filterItem;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        // Prevent errors accessing slots that are within the FakeSlotCount range
        if (slot > this.slotCount - 1) {
            // Pass empty slot if slot out of bounds
            return ItemStack.EMPTY;
        }
        return getItems().get(slot);
    }

    public void onLoad() {
        if (this.getLevel() != null && this.getLevel().isClientSide) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void saveInventoryToTag(CompoundTag tag) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.slotCount; i++) {
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
    protected void saveAdditional(CompoundTag tag) {
        this.saveInventoryToTag(tag);
        tag.putString("title", this.title);
        tag.putInt("slotCount", this.slotCount);
        tag.putInt("maxCapacity", this.maxCapacity);
        tag.putInt("maxItemCapacity", this.getMaxItemCapacity());
        tag.putInt("storedAmount", this.getStoredAmount());
        tag.putBoolean("voidUpgrade", this.hasVoidUpgrade());
        tag.putInt("capacityUpgrades", this.getCapacityUpgrades());
        tag.putInt("slot0Amount", this.items.get(0).getCount());
        tag.putInt("slot1Amount", this.items.get(1).getCount());
        CompoundTag filterTag = new CompoundTag();
        this.filterItem.save(filterTag);
        tag.put("filterItem", filterTag);
        super.saveAdditional(tag);
    }

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
    public void load(CompoundTag tag) {
        super.load(tag);
        this.loadInventoryFromTag(tag);
        this.title = tag.getString("title");
        this.slotCount = tag.getInt("slotCount");
        this.maxCapacity = tag.getInt("maxCapacity");
        this.maxItemCapacity = tag.getInt("maxItemCapacity");
        this.storedAmount = tag.getInt("storedAmount");
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
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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

    public <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockEntity blockEntity) {

        if (level.isClientSide) return;

        ItemStack slot0 = this.items.get(0);

        // Get Stored Amount
        this.storedAmount = this.getStoredAmount();

        // Set filter item to items inside to prevent wrong items being put in
        if (!slot0.isEmpty() && !ItemStack.isSameItemSameTags(slot0, this.filterItem)) {
            this.setFilter(slot0);
        }

        getMaxItemCapacity();
        moveItems();

        if (this.tick >= Config.STORAGE_BOX_UPDATE_TIME.get()) {

            BlockState currentState = this.getBlockState();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);

            EnumProperties.StorageUsed newStorageUsed = EnumProperties.StorageUsed.EMPTY;

            int storedAmount = this.getStoredAmount();

            if (storedAmount >= this.getMaxItemCapacity()) {
                newStorageUsed = EnumProperties.StorageUsed.FULL;
            } else if (storedAmount > 0) {
                newStorageUsed = EnumProperties.StorageUsed.HAS_ITEMS;
            }

            if (currentState.getValue(SimpleStorageBox.STORAGE_USED) != newStorageUsed) {
                level.setBlock(blockPos, currentState.setValue(SimpleStorageBox.STORAGE_USED, newStorageUsed), 3); // 3 is the update flag
            }
        }
        this.tick++;
    }

    private void moveItems() {

        ItemStack slot0 = this.items.get(0);
        ItemStack slot1 = this.items.get(1);

        // If full & using void upgrade then items go into slot 2 (delete them all!)
        if (!this.items.get(2).isEmpty()) {
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

        } else {
            // Always move items from slot 1 to 0 if space available
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
    public boolean canPlaceItem(int index, @NotNull ItemStack itemStack) {
        // Check filter
        if (!this.filterTest(itemStack)) return false;

        // Check space in slot 0
        int freeSpace = this.getMaxItemCapacity() - this.getStoredAmount();

        if (this.hasVoidUpgrade()) return true;

        int amountToPlace = itemStack.getCount();
        return freeSpace > amountToPlace;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return true;
    }

    public void transferItemsToPlayer(Player player) {
        if (getStoredAmount()==0) return;;

        ItemStack srcStack = this.items.get(0);
        if (srcStack.isEmpty()) return;

        if (Util.getMillis() < this.lastInteractTime + this.interactWindow && player.getUUID().equals(this.lastInteractPlayer) && this.lastInteractType == 0) {
            transferAllItemsToPlayer(player, srcStack);
        } else {
            this.lastInteractTime = Util.getMillis();
            this.lastInteractPlayer = player.getUUID();
            this.lastInteractType = 0;
            int amountToMove = Math.min(this.getStoredAmount(), srcStack.getMaxStackSize());
            if (player.isShiftKeyDown()) amountToMove = 1;
            doTransferToPlayer(player, srcStack, amountToMove, false);
        }
    }

    public void transferAllItemsToPlayer(Player player, ItemStack srcStack) {
        int srcAmount = srcStack.getCount();
        while(srcAmount > 0 && playerHasSpace(player, srcStack)) {
            if (!doTransferToPlayer(player, srcStack, srcAmount, true)) break;
            srcAmount = srcStack.getCount();
        }
    }

    private Boolean playerHasSpace(Player player, ItemStack srcStack) {
        int playerSlot = player.getInventory().getSlotWithRemainingSpace(srcStack);
        if (playerSlot < 0) {
            playerSlot = player.getInventory().getFreeSlot();
        }
        return playerSlot >= 0;
    }

    public boolean doTransferToPlayer(Player player, ItemStack srcStack, int amountToMove, boolean transferAll) {

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
                    dropItems(this.getLevel(), srcStack.copyWithCount(amountToMove));
                    srcStack.shrink(amountToMove);
                    this.setChanged();
                }
                return false;
            }
            playerStack = player.getInventory().getItem(playerSlot);
            availableSpace = srcStack.getMaxStackSize() - playerStack.getCount();
        }

        int moveAmount = Math.min(availableSpace, amountToMove);

        if (moveAmount > 0) {
            if (!playerStack.isEmpty()) {
                playerStack.grow(moveAmount);
            } else {
                player.getInventory().setItem(playerSlot, srcStack.copyWithCount(moveAmount));
            }
            player.getInventory().setChanged();
            srcStack.shrink(moveAmount);
            this.setChanged();
            return true;
        }
        return false;
    }

    public void transferItemsFromPlayer(Player player) {
        // Check if upgrade type
        ItemStack handItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (handItem.is(ModTags.STORAGE_BOX_UPGRADE)) {

            if (handItem.is(ModItems.STORAGE_BOX_VOID_UPGRADE)) {
                if (!this.hasVoidUpgrade()) {
                    this.setItem(this.voidUpgradeSlot, handItem.copyWithCount(1));
                    if (!player.isCreative()) {
                        handItem.shrink(1);
                        player.getInventory().setChanged();
                    }
                } else {
                    ItemStack voidStack = this.items.get(this.voidUpgradeSlot);
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
                            dropItems(this.getLevel(), voidStack);
                        }
                    }
                    this.setItem(this.voidUpgradeSlot, ItemStack.EMPTY);
                }
            } else if (handItem.is(ModItems.STORAGE_BOX_CAPACITY_UPGRADE)) {
                boolean canAddUpgrade = false;
                for (int i = this.capacityUpgradeStartSlot; i < this.capacityUpgradeStartSlot + this.maxCapacityUpgrades; i++) {
                    if (this.items.get(i).isEmpty()) {
                        this.setItem(i, handItem.copyWithCount(1));
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
            if (this.filterTest(slotStack)) {
                doTransferItemsFromPlayer(player, slotStack);
            }
        }
    }

    private void doTransferItemsFromPlayer(Player player, ItemStack srcStack) {
        insertItems(srcStack);
        player.getInventory().setChanged();
    }

    public ItemStack insertItems(ItemStack srcStack) {
        if (this.filterTest(srcStack)) {
            int availableSpace = this.getMaxItemCapacity() - this.getStoredAmount();
            int srcAmount = srcStack.getCount();

            if (availableSpace <= 0 && hasVoidUpgrade()) {
                srcStack.shrink(srcAmount);
                return srcStack;
            }
            int moveAmount = Math.min(srcAmount, availableSpace);

            if (moveAmount > 0) {

                if (this.getFilterItem().isEmpty()) {
                    setFilter(srcStack);
                }

                if (!this.items.get(1).isEmpty()) {
                    this.items.get(1).grow(moveAmount);
                } else {
                    this.setItem(1, srcStack.copyWithCount(moveAmount));
                }
                srcStack.shrink(moveAmount);
            }
        }
        return srcStack;
    }


    public void dropItems(Level level, ItemStack itemStack) {
        Direction facing = SimpleStorageBox.getDirectionFacing(getBlockState());
        float xOffset = 0.5f;
        float zOffset = 0.5f;
        if (facing == Direction.NORTH) zOffset = 0.5f -0.8f;
        if (facing == Direction.WEST) xOffset = 0.5f -0.8f;
        if (facing == Direction.EAST) xOffset = 1.3f;
        if (facing == Direction.SOUTH) zOffset = 1.3f;

        float dropX = this.pos.getX() + xOffset;
        float dropY = this.pos.getY();
        float dropZ = this.pos.getZ() + zOffset;
        // Create Item Entities
        ItemStack dropStack = itemStack.split(itemStack.getCount());
        ItemEntity droppedItems = new ItemEntity(level, dropX, dropY, dropZ, dropStack);
        Vec3 motion = droppedItems.getDeltaMovement();
        droppedItems.push(-motion.x, -motion.y, -motion.z);
        level.addFreshEntity(droppedItems);
    }

    public void removeFilter() {
        this.filterItem = ItemStack.EMPTY;
    }

    public void setFilter(ItemStack itemStack) {
        this.filterItem = itemStack.copyWithCount(1);
    }

    public boolean filterTest(ItemStack stack) {
        // Prevent inception
        if (stack.is(ModTags.STORAGE_BOX_ITEM) || stack.is(ModTags.STORAGE_BOX_UPGRADE)) {
            return false;
        }

        return this.filterItem.isEmpty() || ItemStack.isSameItemSameTags(stack, this.filterItem);
    }

    @Override
    public CompoundTag serializeNBT() {
        return super.serializeNBT();
    }
}
