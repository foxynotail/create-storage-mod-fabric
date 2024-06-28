package net.fxnt.fxntstorage.containers;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.containers.util.ContainerSaveContents;
import net.fxnt.fxntstorage.containers.util.StorageBoxEntityHelper;
import net.fxnt.fxntstorage.containers.util.StorageBoxFilteringBox;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.init.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.fxnt.fxntstorage.containers.StorageBox.VOID_UPGRADE;

public class StorageBoxEntity extends SmartBlockEntity implements WorldlyContainer, ContainerSaveContents, ExtendedScreenHandlerFactory {

    public String title = "Default Storage Box";
    public int slotCount = 999; // Needs to be higher than will be set by any storage box
    public int[] SLOTS_FOR_ALL_DIRECTIONS = new int[slotCount];
    public NonNullList<ItemStack> items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    protected ContainerData containerData;
    public BlockPos pos;
    public int storedAmount = -1;
    public int percentageUsed = 0;
    public boolean voidUpgrade = false;
    public int lastTick = 0;
    public boolean doTick = false;
    public int updateEveryXTicks = Config.STORAGE_BOX_UPDATE_TIME.get();
    public FilteringBehaviour filtering;
    public InvManipulationBehaviour invManipulation;
    public VersionedInventoryTrackerBehaviour invVersionTracker;

    private final StorageBoxEntityHelper<StorageBoxEntity> helper;

    public StorageBoxEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.STORAGE_BOX_ENTITY, pos, blockState);
        this.pos = pos;
        this.helper = new StorageBoxEntityHelper<>(this);
        this.voidUpgrade = blockState.getValue(VOID_UPGRADE);
        initializeSlotsForAllDirections();
        this.containerData = new ContainerData() {
            @Override
            public int get(int index) {
                return slotCount;
            }
            @Override
            public void set(int index, int value) {}
            @Override
            public int getCount() {
                return slotCount;
            }
        };
    }

    public void initializeEntity(String title, int slotCount) {
        this.title = title;
        this.slotCount = slotCount;
        this.items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        initializeSlotsForAllDirections();
    }

    public void saveItems(CompoundTag nbt) {
        NonNullList<ItemStack> items = this.getItems();
        ContainerHelper.saveAllItems(nbt, items, true);
    }

    public void onLoad() { helper.onLoad();}

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        helper.read(tag);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        helper.write(tag);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        invManipulation = new InvManipulationBehaviour(this, (w, p, s) -> new BlockFace(p, StorageBox.getDirectionFacing(s).getOpposite()));
        behaviours.add(invManipulation);
        behaviours.add(invVersionTracker = new VersionedInventoryTrackerBehaviour(this));
        filtering = new FilteringBehaviour(this, new StorageBoxFilteringBox());
        behaviours.add(1, filtering);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        helper.writeScreenOpeningData(buf);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(title);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new StorageBoxMenu(i, inventory, this, this.containerData);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public int getContainerSize() {
        return slotCount;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public int getStoredAmount() {
        return this.storedAmount;
    }

    public int calculateStoredAmount() {
        return helper.calculateStoredAmount();
    }

    public int getPercentageUsed() {
        return this.percentageUsed;
    }

    public int calculatePercentageUsed() {
        return helper.calculatePercentageUsed();
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

    public <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockEntity blockEntity) {
        helper.serverTick(level, blockPos, blockEntity);
    }

    // Transfering Items
    public void initializeSlotsForAllDirections() {
        SLOTS_FOR_ALL_DIRECTIONS = new int[this.slotCount];
        for (int i = 0; i < slotCount; i++) {
            SLOTS_FOR_ALL_DIRECTIONS[i] = i;
        }
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        initializeSlotsForAllDirections();
        if (SLOTS_FOR_ALL_DIRECTIONS.length < 1) return new int[]{0};
        return SLOTS_FOR_ALL_DIRECTIONS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return helper.canPlaceItemThroughFace(this.level, index, itemStack, direction);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack itemStack, Direction direction) {
        return helper.canTakeItemThroughFace(this.level, index, itemStack, direction);
    }

    public boolean transferItemsToPlayer(Player player) {
        return helper.transferItemsToPlayer(this.level, this, player);
    }

    public boolean transferItemsFromPlayer(Player player) {
        return helper.transferItemsFromPlayer(this.level, this, player);
    }


    public boolean filterTest(Level level, ItemStack stack) {
        // Prevent inception
        if (stack.is(ModTags.STORAGE_BOX_ITEM)) {
            return false;
        }

        ItemStack filterItem = filtering.getFilter();
        return FilterItemStack.of(filterItem).test(level, stack);
    }

    public void toggleVoidUpgrade() {
        BlockState blockState = this.getBlockState();
        Level level = this.getLevel();
        if (level != null) {
            this.voidUpgrade = !blockState.getValue(VOID_UPGRADE);
            level.setBlockAndUpdate(this.getBlockPos(), blockState.setValue(VOID_UPGRADE, this.voidUpgrade));
        }
    }
}
