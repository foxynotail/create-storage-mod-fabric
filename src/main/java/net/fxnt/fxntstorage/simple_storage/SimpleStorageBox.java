package net.fxnt.fxntstorage.simple_storage;

import com.simibubi.create.AllTags;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fxnt.fxntstorage.containers.util.EnumProperties;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.init.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleStorageBox extends BaseEntityBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<EnumProperties.StorageUsed> STORAGE_USED = EnumProperty.create("storage_used", EnumProperties.StorageUsed.class);
    private long lastInteractionTime = 0;
    private final long INTERACTION_COOLDOWN = 200; // cooldown in milliseconds

    public SimpleStorageBox(FabricBlockSettings properties) {
        super(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
        this.registerDefaultState(this.defaultBlockState().setValue(STORAGE_USED, EnumProperties.StorageUsed.EMPTY));
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        SimpleStorageBoxEntity blockEntity = new SimpleStorageBoxEntity(pos, state);
        return blockEntity;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlocks.SIMPLE_STORAGE_BOX_ENTITY, (type, world, pos, entity) -> {
            entity.serverTick(type, world, entity);
        });
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof Container) {
                ItemStack itemStack = new ItemStack(state.getBlock());
                if (!level.isClientSide && blockEntity instanceof SimpleStorageBoxEntity) {
                    CompoundTag tag = new CompoundTag();
                    ((SimpleStorageBoxEntity) blockEntity).write(tag, false);
                    if (!tag.isEmpty()) {
                        itemStack.getOrCreateTag().put("BlockEntityTag", tag);
                    }
                    // Drop the single ItemStack with all contents stored
                    popResource(level, pos, itemStack);
                }
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SimpleStorageBoxEntity) {
                ((SimpleStorageBoxEntity)blockEntity).getDisplayName();
            }
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator() || level.isClientSide) return InteractionResult.SUCCESS;
        if (hand == InteractionHand.OFF_HAND) return InteractionResult.SUCCESS;
        if (!hitFront(blockState, hit)) return InteractionResult.PASS;

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SimpleStorageBoxEntity simpleStorageBoxEntity) {

            ItemStack handItem = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (handItem.isEmpty() && player.isShiftKeyDown()) {
                // If interact with empty hand while sneaking then open menu
                player.openMenu(simpleStorageBoxEntity);
                return InteractionResult.CONSUME;
            } else if (handItem.is(AllTags.AllItemTags.WRENCH.tag) && simpleStorageBoxEntity.getStoredAmount() == 0 && !simpleStorageBoxEntity.filterItem.isEmpty()) {
                // If box empty, holding wrench & has filter item then remove filter
                simpleStorageBoxEntity.removeFilter();
            } else {
                // Set filter if item is not upgrade or empty hand and no items exist and no filter exists
                if (!handItem.isEmpty() && !handItem.is(ModTags.STORAGE_BOX_UPGRADE) && simpleStorageBoxEntity.getStoredAmount() == 0 && simpleStorageBoxEntity.filterItem.isEmpty()) {
                    simpleStorageBoxEntity.setFilter(handItem);
                }
                // Transfer items from player to box
                simpleStorageBoxEntity.transferItemsFromPlayer(player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void attack(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player) {
        long currentTime = Util.getMillis();
        if (player.isSpectator() || level.isClientSide || currentTime - lastInteractionTime < INTERACTION_COOLDOWN) return;

        lastInteractionTime = currentTime;
        BlockHitResult hit = rayTraceEyes(level, player, blockPos);
        if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(blockPos)) return;

        if (!hitFront(blockState, hit)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SimpleStorageBoxEntity storageBoxEntity) {
            storageBoxEntity.transferItemsToPlayer(player);
        }
    }

    private boolean hitFront (BlockState blockState, BlockHitResult hit) {
        Direction side = hit.getDirection();
        return blockState.getValue(FACING) == side;
    }

    @NotNull
    public static BlockHitResult rayTraceEyes(@NotNull Level level, @NotNull Player player, @NotNull BlockPos blockPos) {
        Vec3 eyePos = player.getEyePosition(1);
        Vec3 lookVector = player.getViewVector(1);
        Vec3 endPos = eyePos.add(lookVector.scale(eyePos.distanceTo(Vec3.atCenterOf(blockPos)) + 1));
        ClipContext context = new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        return level.clip(context);
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
        pBuilder.add(FACING, STORAGE_USED);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag compoundTag = BlockItem.getBlockEntityData(stack);
        if (compoundTag != null) {
            if (compoundTag.contains("LootTable", 8)) {
                tooltip.add(Component.literal("???????"));
            }

            if (compoundTag.contains("Items", 9)) {
                NonNullList<ItemStack> nonNullList = NonNullList.withSize(27, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(compoundTag, nonNullList);
                int i = 0;
                int j = 0;

                for(ItemStack itemStack : nonNullList) {
                    if (!itemStack.isEmpty()) {
                        ++j;
                        if (i <= 4) {
                            ++i;
                            MutableComponent mutableComponent = itemStack.getHoverName().copy();
                            mutableComponent.append(" x").append(String.valueOf(itemStack.getCount()));
                            tooltip.add(mutableComponent);
                        }
                    }
                }

                if (j - i > 0) {
                    tooltip.add(Component.translatable("container.shulkerBox.more", j - i).withStyle(ChatFormatting.ITALIC));
                }
            }
        }
    }

    @Nullable
    public static Direction getDirectionFacing(BlockState state) {
        if (!(state.getBlock() instanceof SimpleStorageBox))
            return null;
        return ((SimpleStorageBox) state.getBlock()).getFacing(state);
    }

    protected Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }
}
