package net.fxnt.fxntstorage.controller;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class StorageControllerBlock extends BaseEntityBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private long lastInteractionTime = 0;
    private final long INTERACTION_COOLDOWN = 200; // cooldown in milliseconds

    public StorageControllerBlock(FabricBlockSettings properties) {
        super(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        StorageControllerEntity blockEntity = new StorageControllerEntity(pos, state);
        return blockEntity;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlocks.STORAGE_CONTROLLER_ENTITY, (type, world, pos, entity) -> {
            entity.serverTick(type, world, entity);
        });
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator() || level.isClientSide) return InteractionResult.SUCCESS;
        if (hand == InteractionHand.OFF_HAND) return InteractionResult.SUCCESS;
        if (!hitFront(blockState, hit)) return InteractionResult.PASS;

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof StorageControllerEntity storageControllerEntity) {
            // Transfer items from player to box
            storageControllerEntity.transferItemsFromPlayer(player);
        }
        return InteractionResult.SUCCESS;
    }

    private boolean hitFront (BlockState blockState, BlockHitResult hit) {
        Direction side = hit.getDirection();
        return blockState.getValue(FACING) == side;
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
    public static Direction getDirectionFacing(BlockState state) {
        if (!(state.getBlock() instanceof StorageControllerBlock))
            return null;
        return ((StorageControllerBlock) state.getBlock()).getFacing(state);
    }

    protected Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }
}
