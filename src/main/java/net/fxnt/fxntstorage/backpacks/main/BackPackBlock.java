package net.fxnt.fxntstorage.backpacks.main;

import com.simibubi.create.compat.Mods;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.util.BackPackHandler;
import net.fxnt.fxntstorage.backpacks.util.BackPackHelper;
import net.fxnt.fxntstorage.cache.BackPackShapeCache;
import net.fxnt.fxntstorage.compat.trinkets.Trinkets;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BackPackBlock extends BaseEntityBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final int containerSlotCount = 108;
    public final int maxStackSize;
    public static final int toolSlotCount = 24;
    public static final int upgradeSlotCount = 6;
    public static final int totalSlotCount = containerSlotCount + toolSlotCount + upgradeSlotCount;
    public BackPackBlock(FabricBlockSettings properties, String title, int maxStackSize) {
        super(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).nonOpaque().notSolid().breakInstantly().sound(SoundType.WOOL));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
        //this.title = "container.fxntstorage." + title;
        this.maxStackSize = maxStackSize;
    }

    public static int getSlotCount() {
        return totalSlotCount;
    }
    public static int getContainerSlotCount() {
        return containerSlotCount;
    }
    public static int getToolSlotCount() {
        return toolSlotCount;
    }
    public static int getUpgradeSlotCount() {
        return upgradeSlotCount;
    }

    public int getMaxStackSize() {
        return this.maxStackSize;
    }


    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        BackPackEntity blockEntity = new BackPackEntity(pos, state);
        blockEntity.setData(totalSlotCount, this.maxStackSize);
        return blockEntity;
    }

    public ItemStack saveEntityToStack(BackPackEntity blockEntity, ItemStack itemStack) {
        itemStack = blockEntity.saveToItemStack(itemStack);
        return itemStack;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BackPackEntity) {
                if (!((BackPackEntity) blockEntity).isBeingPickedUp()) {  // Check if not being picked up
                    ItemStack itemStack = new ItemStack(state.getBlock());
                    if (!level.isClientSide && blockEntity instanceof BackPackEntity backPackEntity) {
                        itemStack = saveEntityToStack(backPackEntity, itemStack);
                        // Drop the single ItemStack with all contents stored
                        popResource(level, pos, itemStack);
                    }
                    level.updateNeighbourForOutputSignal(pos, this);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BackPackEntity) {
                ((BackPackEntity)blockEntity).setCustomName(stack.getHoverName());
            }
        }

    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide() || hand == InteractionHand.OFF_HAND) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown() && !new BackPackHelper().isWearingBackPack(player)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof BackPackEntity backPackEntity)) {
                return InteractionResult.FAIL;
            }


            level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.5F, 1.0F);
            FXNTStorage.LOGGER.info("Play Sound on Take BackPack");
            backPackEntity.setBeingPickedUp(true);

            ItemStack itemStack = new ItemStack(Item.byBlock(this));

            itemStack = saveEntityToStack(backPackEntity, itemStack);
            level.removeBlock(pos, false);

            boolean equipped = false;
            if (Mods.TRINKETS.isLoaded()) {
                equipped = Trinkets.setBackPackTrinket(player, itemStack);
            }
            if (!equipped) {
                player.setItemSlot(EquipmentSlot.CHEST, itemStack);
            }

            return InteractionResult.CONSUME;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BackPackEntity backPackEntity) {
            BackPackHandler.openBackpackFromBlock((ServerPlayer) player, backPackEntity);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return BackPackShapeCache.getShape(direction);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        return BackPackShapeCache.getShape(direction);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return BackPackShapeCache.getShape(direction);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlocks.BACK_PACK_ENTITY, (type, world, pos, entity) -> {
            //FXNTStorage.LOGGER.info("Block Ticker: {}", pos);
            entity.serverTick(type, world, entity);
        });
    }

}
