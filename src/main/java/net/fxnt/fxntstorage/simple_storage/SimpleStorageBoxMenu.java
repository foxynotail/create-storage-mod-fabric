package net.fxnt.fxntstorage.simple_storage;

import net.fxnt.fxntstorage.init.ModItems;
import net.fxnt.fxntstorage.init.ModMenuTypes;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class SimpleStorageBoxMenu extends AbstractContainerMenu {
    public final Container container;
    public final SimpleStorageBoxEntity simpleStorageBoxEntity;
    public final Player player;

    public SimpleStorageBoxMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }
    public SimpleStorageBoxMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenuTypes.SIMPLE_STORAGE_BOX_MENU, containerId);
        this.player = playerInventory.player;
        this.container = (Container) blockEntity;
        this.container.startOpen(player);
        this.simpleStorageBoxEntity = (SimpleStorageBoxEntity) blockEntity;
        this.initSlots();
    }

    public void initSlots() {

        // Add Fake Main slot (Non intractable)
        // Just render. Don't add slot

        // Add Void slot
        this.addSlot(new SimpleStorageBoxVoidSlot(this.container, simpleStorageBoxEntity.voidUpgradeSlot, 8, 20));

        // Add Capacity Slots
        for (int i = 0; i < simpleStorageBoxEntity.maxCapacityUpgrades; i++) {
            int slot = i + simpleStorageBoxEntity.capacityUpgradeStartSlot;
            int y = 58;
            int x = 8;
            this.addSlot(new SimpleStorageBoxUpgradeSlot(this.container, slot, x + (Util.SLOT_SIZE * i), y));
        }

        Inventory playerInventory = player.getInventory();
        // Add Inventory Slots
        int xOffset = 8;
        int yOffset = 94;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, y * 9 + x + 9, xOffset + Util.SLOT_SIZE * x, yOffset + y * Util.SLOT_SIZE));
            }
        }
        // Add Hotbar Slots
        yOffset = yOffset + (Util.SLOT_SIZE * 3) + 4;
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, xOffset + i * Util.SLOT_SIZE, yOffset));
        }
    }

    @Override
    public boolean stillValid(Player player)  {
        return this.container.stillValid(player);
    }
    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
    public Container getInventory() {
        return this.container;
    }
    public boolean filterTest(ItemStack stack) {
        return this.simpleStorageBoxEntity.filterTest(stack);
    }


    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        int playerStartSlot = 1 + this.simpleStorageBoxEntity.maxCapacityUpgrades;
        if (slotId >= 0 && slotId < playerStartSlot) {
            ItemStack itemStack = this.slots.get(slotId).getItem();
            if (itemStack.is(ModItems.STORAGE_BOX_CAPACITY_UPGRADE)) {
                // Calculate new capacity
                int upgrades = this.simpleStorageBoxEntity.getCapacityUpgrades();
                if (upgrades > 0) {
                    int storedAmount = this.simpleStorageBoxEntity.getStoredAmount();
                    int stackSize = this.simpleStorageBoxEntity.itemStackSize;
                    if (!this.simpleStorageBoxEntity.filterItem.isEmpty()) {
                        stackSize = this.simpleStorageBoxEntity.filterItem.getMaxStackSize();
                    }
                    int capacityCheck = this.simpleStorageBoxEntity.baseCapacity;
                    for (int i = 0; i < upgrades - 1; i++) {
                        capacityCheck *= 2;
                    }
                    capacityCheck = capacityCheck * stackSize;
                    if (capacityCheck < storedAmount) {
                        return;
                    }
                }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        ItemStack slotStack = this.slots.get(index).getItem();

        // As not adding container slots, void upgrade (container slot 3) is actually index 0 as it's the first added
        // So upgrade slot 1 (container slot 4) is index 1;
        // First player slot is maxUpgradeSlots (9) + voidSlot = 10
        int playerStartSlot = 1 + this.simpleStorageBoxEntity.maxCapacityUpgrades;

        // If click player slot, if upgrade then move to upgrade slot, otherwise, don't allow inserting items
        if (index < playerStartSlot) {
            // Clicked on upgrade slot
            int playerSlot = player.getInventory().getSlotWithRemainingSpace(slotStack);
            if (playerSlot == -1) {
                playerSlot = player.getInventory().getFreeSlot();
            }
            if (playerSlot > -1) {
                ItemStack playerStack = player.getInventory().getItem(playerSlot);
                if (playerStack.isEmpty()) {
                    player.getInventory().setItem(playerSlot, slotStack.copyWithCount(1));
                } else {
                    playerStack.grow(1);
                }
                slotStack.shrink(1);
                this.container.setChanged();
                player.getInventory().setChanged();
                return ItemStack.EMPTY;
            }

        } else {
            // Clicked Player Slot
            if (slotStack.is(ModItems.STORAGE_BOX_VOID_UPGRADE)) {
                // Move to void slot
                if (!this.slots.get(0).hasItem()) {
                    this.slots.get(0).set(slotStack.copyWithCount(1));
                    slotStack.shrink(1);
                    this.container.setChanged();
                    player.getInventory().setChanged();
                    return slotStack;
                }
            } else if (slotStack.is(ModItems.STORAGE_BOX_CAPACITY_UPGRADE)) {
                // Move to upgrade slot
                for (int i = 1; i <= this.simpleStorageBoxEntity.maxCapacityUpgrades; i++) {
                    if (!this.slots.get(i).hasItem()) {
                        this.slots.get(i).set(slotStack.copyWithCount(1));
                        slotStack.shrink(1);
                        this.container.setChanged();
                        player.getInventory().setChanged();
                        return slotStack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(@NotNull ItemStack stack, Slot slot) {
        int playerStartSlot = 1 + this.simpleStorageBoxEntity.maxCapacityUpgrades;
        if (slot.index < playerStartSlot) {
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }
}
