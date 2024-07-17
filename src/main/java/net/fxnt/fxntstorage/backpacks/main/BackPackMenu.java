package net.fxnt.fxntstorage.backpacks.main;

import com.google.common.collect.Sets;
import com.simibubi.create.AllTags;
import io.github.fabricators_of_create.porting_lib.tags.Tags;
import io.github.fabricators_of_create.porting_lib.tool.ToolActions;
import net.fxnt.fxntstorage.backpacks.upgrades.UpgradeItem;
import net.fxnt.fxntstorage.init.ModItems;
import net.fxnt.fxntstorage.init.ModTags;
import net.fxnt.fxntstorage.network.HighStackCountSync;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class BackPackMenu extends AbstractContainerMenu {
    public Container container;
    private final int slotCount;
    public final Player player;
    public byte backPackType; // 1 = On Back, 2 = From Hand, 3 = BlockEntity
    public BlockPos blockPos;
    private final MenuType<?> menuType;
    private int quickcraftType = -1;
    private int quickcraftStatus;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();
    private final int containerSlotCount = BackPackBlock.getContainerSlotCount();
    private final int toolSlotCount = BackPackBlock.getToolSlotCount();
    private final int upgradeSlotCount = BackPackBlock.getUpgradeSlotCount();
    public boolean ctrlKeyDown = false;

    public BackPackMenu(MenuType<?> type, int containerId, Inventory playerInventory, Container container, byte backPackType) {
        super(type, containerId);
        this.player = playerInventory.player;
        this.backPackType = backPackType;
        this.container = container;
        checkContainerSize(container, BackPackBlock.getSlotCount());
        container.startOpen(player);
        slotCount = BackPackBlock.getSlotCount();
        initSlots();
        this.menuType = type;
        if (backPackType == Util.BACKPACK_AS_BLOCK) {
            this.blockPos = ((BackPackEntity) container).getBlockPos();
        } else {
            this.blockPos = new BlockPos(0,0,0);
        }
    }

    @Override
    public void setSynchronizer(ContainerSynchronizer synchronizer) {
        if (player instanceof ServerPlayer serverPlayer) {
            super.setSynchronizer(new HighStackCountSync(serverPlayer));
            return;
        }
        super.setSynchronizer(synchronizer);
    }

    public void initSlots() {

        Inventory playerInventory = player.getInventory();

        // Add Container Slots
        int index = 0;
        for (int i = 0; i < containerSlotCount; i++) {
            this.addSlot(new BackPackSlot(container, player.level(), index, index * Util.SLOT_SIZE, 0));
            index++;
        }
        // Add Tool Slots
        for (int i = 0; i < toolSlotCount; i++) {
            this.addSlot(new ToolSlot(container, player.level(), index, index * Util.SLOT_SIZE, 0));
            index++;
        }
        // Add Upgrade Slots
        for (int i = 0; i < upgradeSlotCount; i++) {
            this.addSlot(new UpgradeSlot(container, player.level(), index, index * Util.SLOT_SIZE, 0));
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
    public boolean stillValid(@NotNull Player player)  {
        if (backPackType == Util.BACKPACK_IN_HAND) {
            ItemStack selectedStack = player.getInventory().getSelected();
            if (!(selectedStack.getItem() instanceof BackPackItem)) return false;
        }
        return this.container.stillValid(player);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    public Container getInventory() {
        return this.container;
    }

    public int getSlotsSize() {
        return slots.size();
    }

    public Slot getSlot(int slotIndex) {
        return slots.get(slotIndex);
    }
    public Slot getPlayerSlot(int slotIndex) {
        return slots.get(getSlotsSize() - 36 + slotIndex);
    }
    public Slot getHotbarSlot(int slotIndex) {
        return slots.get(getSlotsSize() - 36 + 27 + slotIndex);
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, Player player) {

        // Prevent moving backpack while it is open
        if (slotId >= 0 && backPackType == Util.BACKPACK_IN_HAND) {
            int selectedHotBarSlot = player.getInventory().selected;
            ItemStack selectedStack = player.getInventory().getSelected();
            if (slotId == getSlotsSize() - 36 + 27 + selectedHotBarSlot && selectedStack.getItem() instanceof BackPackItem)
                return;
        }
        if (!this.player.level().isClientSide) {
            if (toggleUpgrade(slotId, ctrlKeyDown)) return;
        }

        try {
            this.doThisClick(slotId, button, clickType, player);
        } catch (Exception var8) {
            CrashReport crashReport = CrashReport.forThrowable(var8, "Container click");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Click info");
            crashReportCategory.setDetail(
                    "Menu Type", () -> this.menuType != null ? BuiltInRegistries.MENU.getKey(this.menuType).toString() : "<no type>"
            );
            crashReportCategory.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
            crashReportCategory.setDetail("Slot Count", this.slots.size());
            crashReportCategory.setDetail("Slot", slotId);
            crashReportCategory.setDetail("Button", button);
            crashReportCategory.setDetail("Type", clickType);
            throw new ReportedException(crashReport);
        }
    }

    private boolean isUpgradeItem(ItemStack itemStack) {
        return itemStack.is(ModTags.BACK_PACK_UPGRADE);
    }

    private boolean isToolItem(ItemStack itemStack) {
        return itemStack.is(ItemTags.TOOLS)
                || itemStack.canPerformAction(ToolActions.SWORD_SWEEP)
                || itemStack.canPerformAction(ToolActions.SHEARS_HARVEST)
                || itemStack.is(Tags.Items.TOOLS_BOWS)
                || itemStack.is(Tags.Items.TOOLS_FISHING_RODS)
                || itemStack.is(Tags.Items.TOOLS_CROSSBOWS)
                || itemStack.is(Tags.Items.TOOLS_SHIELDS)
                || itemStack.is(AllTags.AllItemTags.WRENCH.tag);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        // if item is upgrade item, put into upgrade slot (Only from player inventory)
        if (index > slotCount - 1) {
            Slot slot = this.slots.get(index);
            ItemStack slotItem = slot.getItem();
            if (isUpgradeItem(slotItem)) {
                // Are there any free upgrade slots
                for (int i = Util.UPGRADE_SLOT_START_RANGE; i < Util.UPGRADE_SLOT_END_RANGE; i++) {
                    if(this.slots.get(i).getItem().isEmpty()) {
                        this.slots.get(i).safeInsert(slotItem);
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        // if item is tool item, put into tool slot (Only from player inventory)
        if (index > slotCount - 1) {
            Slot slot = this.slots.get(index);
            ItemStack slotItem = slot.getItem();
            // SHEARS / BOWS / FISHING RODS / SHIELDS
            if (isToolItem(slotItem)) {
                // Are there any free tool ade slots
                for (int i = Util.TOOL_SLOT_START_RANGE; i < Util.TOOL_SLOT_END_RANGE; i++) {
                    if(this.slots.get(i).getItem().isEmpty()) {
                        this.slots.get(i).safeInsert(slotItem);
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        // General Quick Move for all other items
        ItemStack tempStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();

            // Check filter against items before proceeding
            if(!BackPackEntity.filterTest(player.level(), slotStack)) {
                return tempStack;
            }
            tempStack = slotStack.copy();


            if (index < this.slotCount) {
                //FXNTStorage.LOGGER.info("Sending from Container {}", slotStack.getCount());
                if (!this.moveItemStack(slotStack, Util.UPGRADE_SLOT_END_RANGE, this.slots.size(), true, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStack(slotStack, 0, Util.ITEM_SLOT_END_RANGE, false, false)) {
                //FXNTStorage.LOGGER.info("Sending from Inventory {}", slotStack.getCount());
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }


        return tempStack;
    }

    public boolean moveItemStack(ItemStack newStack, int startIndex, int endIndex, boolean reverseDirection, boolean fromContainer) {
        boolean bl = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        if ((!fromContainer && !newStack.isDamageableItem() && !newStack.hasTag() && !newStack.hasCustomHoverName() && !newStack.isBarVisible() && !newStack.isBarVisible()) || (fromContainer && newStack.isStackable())) {
            while(!newStack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex)) {
                Slot slot = this.slots.get(i);
                ItemStack slotStack = slot.getItem();
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(newStack, slotStack)) {
                    // Check if the slot is an UpgradeSlot and adjust the logic accordingly
                    if (slot instanceof UpgradeSlot && slotStack.getCount() >= 1) {
                        // Prevent any further stacking if it's an UpgradeSlot with an item already
                        break; // Break the loop to prevent combining items
                    }

                    int totalCount = slotStack.getCount() + newStack.getCount();
                    int maxPutSize = Math.max(newStack.getMaxStackSize(), slot.getMaxStackSize());
                    int availableSpace = maxPutSize - slotStack.getCount();

                    if (totalCount <= maxPutSize) {
                        newStack.setCount(0);
                        slotStack.setCount(totalCount);
                        slot.setChanged();
                        bl = true;
                    } else if (availableSpace < newStack.getMaxStackSize()) {
                        newStack.shrink(availableSpace);
                        slotStack.setCount(maxPutSize);
                        slot.setChanged();
                        bl = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!newStack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(reverseDirection ? i >= startIndex : i < endIndex) {
                Slot slot = this.slots.get(i);
                ItemStack slotStack = slot.getItem();

                if (slotStack.isEmpty() && slot.mayPlace(newStack)) {

                    int maxPutSize = Math.max(newStack.getMaxStackSize(), slot.getMaxStackSize());
                    int availableSpace = maxPutSize - slotStack.getCount();
                    //if (newStack.getCount() > availableSpace) {

                    if (fromContainer && !newStack.isStackable()) {
                        // If item is non-stackable only put 1 in
                        if (1 > availableSpace) {
                            ItemStack inputStack = newStack.split(1);
                            slot.setByPlayer(inputStack);
                        } else {
                            ItemStack inputStack = newStack.split(1);
                            slot.setByPlayer(inputStack);
                        }
                    } else {
                        // From Player (Stack Can Be Any Size)
                        if (newStack.getCount() > availableSpace) {
                            ItemStack inputStack = newStack.split(slot.getMaxStackSize());
                            slot.setByPlayer(inputStack);
                        } else {
                            ItemStack inputStack = newStack.split(newStack.getCount());
                            slot.setByPlayer(inputStack);
                        }
                    }

                    slot.setChanged();
                    bl = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return bl;
    }

    private void doThisClick(int slotId, int button, ClickType clickType, Player player) {
        //FXNTStorage.LOGGER.info("Do This Click {} {} {} {}", slotId, button, clickType.toString(), getCarried().getItem().toString());
        Inventory inventory = player.getInventory();

        if (clickType == ClickType.QUICK_CRAFT) {
            //FXNTStorage.LOGGER.info("Quick Craft");
            int i = this.quickcraftStatus;
            /**
             * Args : clickedButton, Returns (0 : start drag, 1 : add slot, 2 : end drag)
             */
            this.quickcraftStatus = getQuickcraftHeader(button);
            if ((i != 1 || this.quickcraftStatus != 2) && i != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                /**
                 * Extracts the drag mode. Args : eventButton. Return (0 : evenly split, 1 : one item by slot, 2 : not used ?)
                 */
                this.quickcraftType = getQuickcraftType(button);
                if (isValidQuickcraftType(this.quickcraftType, player)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                Slot slot = this.slots.get(slotId);
                ItemStack carriedItemStack = this.getCarried();
                // Adjusted Method
                if (fxnt$canItemQuickReplace(slot, carriedItemStack, true)
                        && slot.mayPlace(carriedItemStack)
                        && (this.quickcraftType == 2 || carriedItemStack.getCount() > this.quickcraftSlots.size())
                        && this.canDragTo(slot)) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (this.quickcraftSlots.size() == 1) {
                        int j = this.quickcraftSlots.iterator().next().index;
                        this.resetQuickCraft();
                        this.doThisClick(j, this.quickcraftType, ClickType.PICKUP, player);
                        return;
                    }

                    ItemStack carriedItemStackCopy = this.getCarried().copy();
                    if (carriedItemStackCopy.isEmpty()) {
                        this.resetQuickCraft();
                        return;
                    }

                    int k = this.getCarried().getCount();

                    for(Slot slot2 : this.quickcraftSlots) {
                        ItemStack newCarriedItemStack = this.getCarried();
                        if (slot2 != null
                                && fxnt$canItemQuickReplace(slot2, newCarriedItemStack, true)
                                && slot2.mayPlace(newCarriedItemStack)
                                && (this.quickcraftType == 2 || newCarriedItemStack.getCount() >= this.quickcraftSlots.size())
                                && this.canDragTo(slot2)) {
                            int l = slot2.hasItem() ? slot2.getItem().getCount() : 0;

                            // Get Max Stack Size
                            int m = Math.min(carriedItemStackCopy.getMaxStackSize(), slot2.getMaxStackSize(carriedItemStackCopy));
                            //FXNTStorage.LOGGER.info("Quick Craft Math Min {} {} {}",m, carriedItemStackCopy.getMaxStackSize(), slot2.getMaxStackSize(carriedItemStackCopy));
                            int n = Math.min(getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, carriedItemStackCopy) + l, m);
                            k -= n - l;
                            slot2.setByPlayer(carriedItemStackCopy.copyWithCount(n));
                        }
                    }

                    carriedItemStackCopy.setCount(k);
                    this.setCarried(carriedItemStackCopy);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
            ClickAction clickAction = button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
            if (slotId == -999) {
                // Clicked Outside
                if (!this.getCarried().isEmpty()) {
                    if (clickAction == ClickAction.PRIMARY) {
                        player.drop(this.getCarried(), true);
                        //FXNTStorage.LOGGER.info("Set Carried -1");
                        this.setCarried(ItemStack.EMPTY);
                    } else {
                        player.drop(this.getCarried().split(1), true);
                    }
                }
            } else if (clickType == ClickType.QUICK_MOVE) {
                //FXNTStorage.LOGGER.info("QUICK MOVE TYPE");
                // Quick Move Stack
                if (slotId < 0) {
                    return;
                }
                Slot slot = this.slots.get(slotId);
                if (!slot.mayPickup(player)) {
                    return;
                }

                ItemStack itemStack = this.quickMoveStack(player, slotId);
                // Not sure how to deal with double click so instead use right click to move entire stack from container
                if (slotId >= Util.ITEM_SLOT_END_RANGE || clickAction == ClickAction.SECONDARY) {

                    int freeContainerSlot = -1;

                    // Free slot (need to determine if sending to tool / upgrade or main container slots
                    int slotRangeStart = 0;
                    int slotRangeEnd = Util.ITEM_SLOT_END_RANGE;
                    if (isUpgradeItem(itemStack)) {
                        slotRangeStart = Util.UPGRADE_SLOT_START_RANGE;
                        slotRangeEnd = Util.UPGRADE_SLOT_END_RANGE;
                    } else if(isToolItem((itemStack))) {
                        slotRangeStart = Util.TOOL_SLOT_START_RANGE;
                        slotRangeEnd = Util.TOOL_SLOT_END_RANGE;
                    }

                    for(int i = slotRangeStart; i < slotRangeEnd; ++i) {
                        if (container.getItem(i).isEmpty()) {
                            freeContainerSlot = i;
                            break;
                        }
                    }

                    boolean freeSlots = slotId < Util.ITEM_SLOT_END_RANGE ? player.getInventory().getFreeSlot() > -1 : freeContainerSlot > -1;
                    while (freeSlots && !itemStack.isEmpty() && ItemStack.isSameItem(slot.getItem(), itemStack)) {
                        itemStack = this.quickMoveStack(player, slotId);
                        freeSlots = slotId < Util.ITEM_SLOT_END_RANGE ? player.getInventory().getFreeSlot() > -1 : freeContainerSlot > -1;
                    }
                }
            } else {
                // PICKUP TYPE
                if (slotId < 0) {
                    return;
                }

                Slot slot = this.slots.get(slotId);
                ItemStack itemStack = slot.getItem();
                ItemStack carriedItemStack = this.getCarried();
                if (!this.tryItemClickBehaviourOverride(player, clickAction, slot, itemStack, carriedItemStack)) {
                    if (itemStack.isEmpty()) {
                        if (!carriedItemStack.isEmpty()) {
                            int o = clickAction == ClickAction.PRIMARY ? carriedItemStack.getCount() : 1;
                            //FXNTStorage.LOGGER.info("Do Click Safe Insert 1 {}", carriedItemStack.toString());
                            this.setCarried(slot.safeInsert(carriedItemStack, o));
                        }
                    } else if (slot.mayPickup(player)) {
                        if (carriedItemStack.isEmpty()) {
                            int o = clickAction == ClickAction.PRIMARY ? itemStack.getCount() : (itemStack.getCount() + 1) / 2;
                            Optional<ItemStack> optional = slot.tryRemove(o, slot.getItem().getMaxStackSize(), player);
                            //FXNTStorage.LOGGER.info("Set Carried 1");
                            optional.ifPresent(stack -> {
                                this.setCarried(stack);
                                slot.onTake(player, stack);
                            });
                        } else if (slot.mayPlace(carriedItemStack)) {
                            //FXNTStorage.LOGGER.info("Slot Max Stack Size {}", slot.getMaxStackSize(carriedItemStack));
                            if (ItemStack.isSameItemSameTags(itemStack, carriedItemStack)) {
                                int o = clickAction == ClickAction.PRIMARY ? carriedItemStack.getCount() : 1;
                                //FXNTStorage.LOGGER.info("Do Click Safe Insert 2 {}", carriedItemStack.toString());
                                this.setCarried(slot.safeInsert(carriedItemStack, o));


                            } else if (carriedItemStack.getCount() <= slot.getMaxStackSize(carriedItemStack)) {
                                //FXNTStorage.LOGGER.info("Set Carried 2");

                                // Prevent swapping with item that has more than a stack
                                int amountToPickup = slot.getItem().getCount();
                                int defaultStackSize = slot.getItem().getMaxStackSize();

                                if (amountToPickup <= defaultStackSize) {
                                    this.setCarried(itemStack);
                                    slot.setByPlayer(carriedItemStack);
                                }
                            }
                        } else if (ItemStack.isSameItemSameTags(itemStack, carriedItemStack)) {

                            //FXNTStorage.LOGGER.info("Carried Item Stack Size {}", carriedItemStack.getMaxStackSize());
                            Optional<ItemStack> optional2 = slot.tryRemove(itemStack.getCount(), carriedItemStack.getMaxStackSize() - carriedItemStack.getCount(), player);
                            optional2.ifPresent(stack -> {
                                carriedItemStack.grow(stack.getCount());
                                slot.onTake(player, stack);
                            });
                        }
                    }
                }

                slot.setChanged();
            }
        } else if (clickType == ClickType.SWAP) {
            //FXNTStorage.LOGGER.info("SWAP");
            Slot thisSlot = this.slots.get(slotId);
            ItemStack inventoryItemStack = inventory.getItem(button);
            ItemStack thisSlotItem = thisSlot.getItem();

            if ((!inventoryItemStack.isEmpty() || !thisSlotItem.isEmpty())) {
                if (inventoryItemStack.isEmpty()) {
                    if (thisSlot.mayPickup(player)) {
                        // DROP ITEM INTO PLAYER INVENTORY
                        //FXNTStorage.LOGGER.info("DROP ITEM INTO PLAYER INVENTORY");
                        int itemMaxSize = thisSlotItem.getItem().getMaxStackSize();
                        int amountToMove = thisSlotItem.getCount();
                        //FXNTStorage.LOGGER.info("Max Allowed {} Got {}", itemMaxSize, amountToMove);
                        if (amountToMove <= itemMaxSize) {
                            inventory.setItem(button, thisSlotItem);
                            thisSlot.setByPlayer(ItemStack.EMPTY);
                            thisSlot.onTake(player, thisSlotItem);
                        } else {
                            // THIS WORKS
                            //FXNTStorage.LOGGER.info("Too Big");
                            ItemStack newStack = thisSlotItem.copyWithCount(amountToMove - itemMaxSize);
                            inventory.setItem(button, thisSlotItem.copyWithCount(itemMaxSize));
                            thisSlot.setByPlayer(newStack);
                            //thisSlot.getItem().setCount(amountToMove - itemMaxSize);
                            thisSlot.onTake(player, thisSlotItem);
                        }
                    }
                } else if (thisSlotItem.isEmpty()) {
                    if (thisSlot.mayPlace(inventoryItemStack)) {
                        //FXNTStorage.LOGGER.info("This Slot May Pace Max Size {}", thisSlot.getMaxStackSize(inventoryItemStack));
                        int p = thisSlot.getMaxStackSize(inventoryItemStack);
                        if (inventoryItemStack.getCount() > p) {
                            thisSlot.setByPlayer(inventoryItemStack.split(p));
                        } else {
                            inventory.setItem(button, ItemStack.EMPTY);
                            thisSlot.setByPlayer(inventoryItemStack);
                        }
                    }
                } else if (thisSlot.mayPickup(player) && thisSlot.mayPlace(inventoryItemStack)) {

                    //FXNTStorage.LOGGER.info("This Slot May Pickup Max Size {}", thisSlot.getMaxStackSize(inventoryItemStack));
                    int p = thisSlot.getMaxStackSize(inventoryItemStack);
                    if (inventoryItemStack.getCount() > p) {
                        // TODO
                        // HAVEN'T BEEN ABLE TO TRIGGER THIS SECTION
                        //FXNTStorage.LOGGER.info("TODO SWAP NOT TRIGGERED YET");
                        thisSlot.setByPlayer(inventoryItemStack.split(p));
                        thisSlot.onTake(player, thisSlotItem);
                        if (!inventory.add(thisSlotItem)) {
                            player.drop(thisSlotItem, true);
                        }
                    } else {
                        int itemMaxSize = thisSlotItem.getItem().getMaxStackSize();
                        int amountToMove = thisSlotItem.getCount();
                        // WORKS
                        //FXNTStorage.LOGGER.info("Else Max Size {} Got {}", itemMaxSize, amountToMove);
                        if (amountToMove <= itemMaxSize) {
                            inventory.setItem(button, thisSlotItem);
                            thisSlot.setByPlayer(inventoryItemStack);
                            thisSlot.onTake(player, thisSlotItem);
                        } else {
                            // WORKS
                            //FXNTStorage.LOGGER.warn("Fail as inventory slot too big to move");
                        }
                    }
                }
            }
        } else if (clickType == ClickType.CLONE && player.getAbilities().instabuild && this.getCarried().isEmpty() && slotId >= 0) {
            // CREATIVE MODE
            //FXNTStorage.LOGGER.info("CLONE");
            Slot thisSlot = this.slots.get(slotId);
            if (thisSlot.hasItem()) {
                ItemStack inventoryItemStack = thisSlot.getItem();

                //FXNTStorage.LOGGER.info("Inventory Stack Max Size {}", inventoryItemStack.getMaxStackSize());
                this.setCarried(inventoryItemStack.copyWithCount(inventoryItemStack.getMaxStackSize()));
            }
        } else if (clickType == ClickType.THROW && this.getCarried().isEmpty() && slotId >= 0) {
            // THROW ITEM WORKS!
            //FXNTStorage.LOGGER.info("THROW");
            Slot thisSlot = this.slots.get(slotId);
            int j = button == 0 ? 1 : thisSlot.getItem().getCount();
            //ItemStack itemStack = thisSlot.safeTake(j, Integer.MAX_VALUE, player);
            ItemStack itemStack = thisSlot.safeTake(j, thisSlot.getItem().getMaxStackSize(), player);
            player.drop(itemStack, true);
        } else if (clickType == ClickType.PICKUP_ALL && slotId >= 0) {
            //FXNTStorage.LOGGER.info("PICK UP ALL");
            Slot thisSlot = this.slots.get(slotId);
            ItemStack inventoryItemStack = this.getCarried();
            if (!inventoryItemStack.isEmpty() && (!thisSlot.hasItem() || !thisSlot.mayPickup(player))) {
                int k = button == 0 ? 0 : this.slots.size() - 1;
                int p = button == 0 ? 1 : -1;

                for(int o = 0; o < 2; ++o) {
                    //FXNTStorage.LOGGER.info("Inventory Stack Max Size {}", inventoryItemStack.getMaxStackSize());
                    for(int q = k; q >= 0 && q < this.slots.size() && inventoryItemStack.getCount() < inventoryItemStack.getMaxStackSize(); q += p) {
                        Slot slot4 = this.slots.get(q);
                        if (slot4.hasItem() && this.fxnt$canItemQuickReplace(slot4, inventoryItemStack, true) && slot4.mayPickup(player) && this.canTakeItemForPickAll(inventoryItemStack, slot4)) {
                            ItemStack itemStack5 = slot4.getItem();

                            //FXNTStorage.LOGGER.info("Item Stack 5 Max Size {}", itemStack5.getMaxStackSize());
                            if (o != 0 || itemStack5.getCount() != itemStack5.getMaxStackSize()) {
                                //FXNTStorage.LOGGER.info("Inventory Stack Max Size {}", inventoryItemStack.getMaxStackSize());
                                ItemStack itemStack6 = slot4.safeTake(itemStack5.getCount(), inventoryItemStack.getMaxStackSize() - inventoryItemStack.getCount(), player);
                                inventoryItemStack.grow(itemStack6.getCount());
                            }
                        }
                    }
                }
            }
        }
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
        //FXNTStorage.LOGGER.info("Reset Quick Craft {} {}", quickcraftStatus, quickcraftSlots.toString());
    }
    private boolean tryItemClickBehaviourOverride(Player player, ClickAction action, Slot slot, ItemStack clickedItem, ItemStack carriedItem) {
        FeatureFlagSet featureFlagSet = player.level().enabledFeatures();
        if (carriedItem.isItemEnabled(featureFlagSet) && carriedItem.overrideStackedOnOther(slot, action, player)) {
            return true;
        } else {
            return clickedItem.isItemEnabled(featureFlagSet) && clickedItem.overrideOtherStackedOnMe(carriedItem, slot, action, player, this.createCarriedSlotAccess());
        }
    }

    public boolean fxnt$canItemQuickReplace(@Nullable Slot slot, ItemStack stack, boolean stackSizeMatters) {
        boolean bl = slot == null || !slot.hasItem();
        if (!bl && ItemStack.isSameItemSameTags(stack, slot.getItem())) {

            int maxSlotSize = stack.getMaxStackSize();
            if (slot instanceof BackPackSlot) {
                maxSlotSize = new BackPackSlot(this.container, this.player.level(), -1, 0, 0).getMaxStackSize();
            } else if (slot instanceof UpgradeSlot) {
                maxSlotSize = UpgradeSlot.getMaxStackSizeStatic();
            }
            return slot.getItem().getCount() + (stackSizeMatters ? 0 : stack.getCount()) <= maxSlotSize;
        } else {
            return bl;
        }
    }

    public boolean toggleUpgrade(int slotId, boolean ctrlKeyDown) {

        Slot slot = this.slots.get(slotId);
        if (slotId >= Util.UPGRADE_SLOT_START_RANGE && slotId < Util.UPGRADE_SLOT_END_RANGE) {
            if(slot.getItem().is(ModTags.BACK_PACK_UPGRADE) && slot.getItem().getItem() instanceof UpgradeItem upgradeItem) {
                String itemName = upgradeItem.getUpgradeName();
                String baseItemName = itemName
                        .replace("back_pack_", "")
                        .replace("_upgrade", "")
                        .replace("_deactivated", "");
                if (ctrlKeyDown) {
                    ItemStack itemStack = this.slots.get(slotId).getItem();
                    if (!itemStack.isEmpty()) {
                        if (itemName.contains("_deactivated")) {
                            itemStack = switch (baseItemName) {
                                case "magnet" -> new ItemStack(ModItems.BACK_PACK_MAGNET_UPGRADE);
                                case "pickblock" -> new ItemStack(ModItems.BACK_PACK_PICKBLOCK_UPGRADE);
                                case "itempickup" -> new ItemStack(ModItems.BACK_PACK_ITEMPICKUP_UPGRADE);
                                case "flight" -> new ItemStack(ModItems.BACK_PACK_FLIGHT_UPGRADE);
                                case "refill" -> new ItemStack(ModItems.BACK_PACK_REFILL_UPGRADE);
                                case "feeder" -> new ItemStack(ModItems.BACK_PACK_FEEDER_UPGRADE);
                                case "toolswap" -> new ItemStack(ModItems.BACK_PACK_TOOLSWAP_UPGRADE);
                                case "falldamage" -> new ItemStack(ModItems.BACK_PACK_FALLDAMAGE_UPGRADE);
                                default -> ItemStack.EMPTY;
                            };
                        } else {
                            itemStack = switch (baseItemName) {
                                case "magnet" -> new ItemStack(ModItems.BACK_PACK_MAGNET_UPGRADE_DEACTIVATED);
                                case "pickblock" -> new ItemStack(ModItems.BACK_PACK_PICKBLOCK_UPGRADE_DEACTIVATED);
                                case "itempickup" -> new ItemStack(ModItems.BACK_PACK_ITEMPICKUP_UPGRADE_DEACTIVATED);
                                case "flight" -> new ItemStack(ModItems.BACK_PACK_FLIGHT_UPGRADE_DEACTIVATED);
                                case "refill" -> new ItemStack(ModItems.BACK_PACK_REFILL_UPGRADE_DEACTIVATED);
                                case "feeder" -> new ItemStack(ModItems.BACK_PACK_FEEDER_UPGRADE_DEACTIVATED);
                                case "toolswap" -> new ItemStack(ModItems.BACK_PACK_TOOLSWAP_UPGRADE_DEACTIVATED);
                                case "falldamage" -> new ItemStack(ModItems.BACK_PACK_FALLDAMAGE_UPGRADE_DEACTIVATED);
                                default -> ItemStack.EMPTY;
                            };
                        }
                        if (!itemStack.isEmpty()) {
                            this.slots.get(slotId).set(itemStack.copyWithCount(slot.getItem().getCount()));
                            //BackPackNetworkHelper.sendToServer(slotId, itemStack.copyWithCount(slot.getItem().getCount()), menu.backPackType, menu.blockPos);
                        }
                        return true;
                    }
                } else {
                    // If try to pickup deactivated item, then toggle back to activated version
                    if (itemName.contains("_deactivated")) {
                        ItemStack itemStack = switch (baseItemName) {
                            case "magnet" -> new ItemStack(ModItems.BACK_PACK_MAGNET_UPGRADE);
                            case "pickblock" -> new ItemStack(ModItems.BACK_PACK_PICKBLOCK_UPGRADE);
                            case "itempickup" -> new ItemStack(ModItems.BACK_PACK_ITEMPICKUP_UPGRADE);
                            case "flight" -> new ItemStack(ModItems.BACK_PACK_FLIGHT_UPGRADE);
                            case "refill" -> new ItemStack(ModItems.BACK_PACK_REFILL_UPGRADE);
                            case "feeder" -> new ItemStack(ModItems.BACK_PACK_FEEDER_UPGRADE);
                            case "toolswap" -> new ItemStack(ModItems.BACK_PACK_TOOLSWAP_UPGRADE);
                            case "falldamage" -> new ItemStack(ModItems.BACK_PACK_FALLDAMAGE_UPGRADE);
                            default -> ItemStack.EMPTY;
                        };
                        this.slots.get(slotId).set(itemStack.copyWithCount(slot.getItem().getCount()));
                        //BackPackNetworkHelper.sendToServer(slotId, itemStack.copyWithCount(slot.getItem().getCount()), menu.backPackType, menu.blockPos);
                    }
                }
            }
        }
        return false;
    }
}
