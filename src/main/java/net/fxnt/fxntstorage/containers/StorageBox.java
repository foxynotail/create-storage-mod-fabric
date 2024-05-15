package net.fxnt.fxntstorage.containers;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fxnt.fxntstorage.containers.util.ContainerActions;
import net.fxnt.fxntstorage.containers.util.EnumProperties;
import net.fxnt.fxntstorage.init.ModBlocks;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StorageBox extends BaseEntityBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty STORAGE_USED = EnumProperty.create("storage_used", EnumProperties.StorageUsed.class);
    public static final BooleanProperty VOID_UPGRADE = BooleanProperty.create("void_upgrade");
    private final String title;
    private final int slotCount;
    private static long lastInteractionTime = 0;
    private static final long INTERACTION_COOLDOWN = 200; // cooldown in milliseconds

    public StorageBox(FabricBlockSettings properties, int slotCount, String title) {
        super(properties.copyOf(Blocks.IRON_BLOCK));
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
        this.registerDefaultState(this.defaultBlockState().setValue(STORAGE_USED, EnumProperties.StorageUsed.EMPTY));
        this.registerDefaultState(this.defaultBlockState().setValue(VOID_UPGRADE, false));
        this.slotCount = slotCount;
        this.title = "container.fxntstorage." + title;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        StorageBoxEntity blockEntity = new StorageBoxEntity(pos, state);
        blockEntity.initializeEntity(title, slotCount);
        return blockEntity;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlocks.STORAGE_BOX_ENTITY, (type, world, pos, entity) -> {
            entity.serverTick(type, world, entity);
        });
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof Container) {
                ItemStack itemStack = new ItemStack(state.getBlock());
                if (!level.isClientSide && blockEntity instanceof StorageBoxEntity) {
                    CompoundTag nbt = new CompoundTag();
                    ((StorageBoxEntity) blockEntity).saveItems(nbt);
                    if (!nbt.isEmpty()) {
                        if (!itemStack.hasTag()) {
                            itemStack.setTag(new CompoundTag());
                        }
                        itemStack.getTag().put("BlockEntityTag", nbt);
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
            if (blockEntity instanceof StorageBoxEntity) {
                ((StorageBoxEntity)blockEntity).getDisplayName();
            }
        }
    }

    private boolean hitFront (BlockState blockState, BlockHitResult hit) {
        Direction side = hit.getDirection();
        return blockState.getValue(FACING) == side;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
        boolean isClient = level.isClientSide();
        if (!isClient) {
            if (hand == InteractionHand.OFF_HAND) return InteractionResult.SUCCESS;
            if (!hitFront(blockState, hit))  return InteractionResult.PASS;

            if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                if (blockEntity instanceof StorageBoxEntity storageBoxEntity) {
                    if (!player.isShiftKeyDown()) {
                        // If interact with empty hand while standing then open menu
                        player.openMenu(storageBoxEntity);
                        return InteractionResult.CONSUME;
                    } else {
                        // If interact with empty hand while crouching then toggle void upgrade
                        storageBoxEntity.toggleVoidUpgrade();
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return ContainerActions.transferItemsToContainer(level, blockPos, player, hit, FACING);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void attack(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull Player player) {
        long currentTime = Util.getMillis();
        if (level.isClientSide || currentTime - lastInteractionTime < INTERACTION_COOLDOWN) return;

        lastInteractionTime = currentTime;
        BlockHitResult hit = ContainerActions.rayTraceEyes(level, player, blockPos);
        if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(blockPos)) return;

        if (!hitFront(blockState, hit)) {
            return;
        }
        ContainerActions.transferItemsFromContainer(level, blockPos, player, hit, FACING);
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
        pBuilder.add(FACING, STORAGE_USED, VOID_UPGRADE);
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
        if (!(state.getBlock() instanceof StorageBox))
            return null;
        return ((StorageBox) state.getBlock()).getFacing(state);
    }

    protected Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }
    public boolean getVoidUpgradeStatus(BlockState state) {
        return state.getValue(VOID_UPGRADE);
    }
}
