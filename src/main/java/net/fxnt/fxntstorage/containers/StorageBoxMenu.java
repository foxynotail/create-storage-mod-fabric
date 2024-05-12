package net.fxnt.fxntstorage.containers;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.fxnt.fxntstorage.containers.util.StorageBoxSlot;
import net.fxnt.fxntstorage.init.ModMenuTypes;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class StorageBoxMenu extends AbstractContainerMenu {
    public final Container container;
    private final int slotCount;
    public final Player player;
    private final FilteringBehaviour filtering;

    public StorageBoxMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(buf.readInt()));
    }
    public StorageBoxMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity, ContainerData simpleContainerData) {
        super(ModMenuTypes.STORAGE_BOX_MENU, containerId);
        this.player = playerInventory.player;
        ContainerData containerData = simpleContainerData;
        this.slotCount = containerData.getCount();
        this.container = (Container) blockEntity;
        checkContainerSize(container, this.slotCount);
        this.container.startOpen(player);
        this.initSlots();
        filtering = getFiltering(blockEntity);
    }

    public FilteringBehaviour getFiltering(BlockEntity blockEntity) {
        if (blockEntity instanceof StorageBoxEntity) {
            return ((StorageBoxEntity) blockEntity).filtering;
        }
        return null;
    }

    public void initSlots() {

        Inventory playerInventory = player.getInventory();

        // Add Container Slots
        int index = 0;
        for (int i = 0; i < this.slotCount; i++) {
            this.addSlot(new StorageBoxSlot(container, player.level(), index, index * Util.SLOT_SIZE, 0));
            index++;
        }
        // Add Inventory Slots
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, y * 9 + x + 9, Util.SLOT_SIZE * x, y * Util.SLOT_SIZE));
            }
        }
        // Add Hotbar Slots
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, i * Util.SLOT_SIZE, 2 * Util.SLOT_SIZE));
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
    public int getSlotsSize() {
        return slots.size();
    }
    public int getContainerSize() {
        return this.slotCount;
    }
    public Slot getPlayerSlot(int slotIndex) {
        return slots.get(getSlotsSize() - 36 + slotIndex);
    }
    public Slot getHotbarSlot(int slotIndex) {
        return slots.get(getSlotsSize() - 36 + 27 + slotIndex);
    }

    public boolean filterTest(ItemStack stack) {
        ItemStack filterItem = filtering.getFilter();
        return FilterItemStack.of(filterItem).test(player.level(), stack);
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack newStack = slot.getItem();
            if(!filterTest(newStack)) {
                return originalStack;
            }
            originalStack = newStack.copy();
            if (index < container.getContainerSize()) {
                if (!this.moveItemStackTo(newStack, container.getContainerSize(), container.getContainerSize() + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(newStack, 0, container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }
            if (newStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return originalStack;
    }
}
