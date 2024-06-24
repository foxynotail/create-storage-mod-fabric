package net.fxnt.fxntstorage.passer;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class PasserHelper {
    @SuppressWarnings("UnstableApiUsage")
    @Nullable
    public static Storage<ItemVariant> getStorage(Level level, BlockPos blockPos, Direction facing, boolean source) {
        BlockPos containerPos;
        Direction interactSide;
        if (source) {
            containerPos = blockPos.relative(facing.getOpposite());
            interactSide = facing;
        } else {
            containerPos = blockPos.relative(facing);
            interactSide = facing.getOpposite();
        }

        return ItemStorage.SIDED.find(level, containerPos, interactSide);
    }

    @Nullable
    public static Container getContainer(Level level, BlockPos blockPos, Direction facing, boolean source) {

        Container container = null;
        BlockPos containerPos;
        if (source) {
            containerPos = blockPos.relative(facing.getOpposite());
        } else {
            containerPos = blockPos.relative(facing);
        }
        BlockState blockState = level.getBlockState(containerPos);
        Block block = blockState.getBlock();
        if (block instanceof WorldlyContainerHolder) {
            container = ((WorldlyContainerHolder)block).getContainer(blockState, level, containerPos);
        } else if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = level.getBlockEntity(containerPos);
            if (blockEntity instanceof Container) {
                container = (Container)blockEntity;
                if (container instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    container = ChestBlock.getContainer((ChestBlock)block, blockState, level, containerPos, true);
                }
            }
        }
        return container;
    }

    public static boolean passItems(Level level, Container srcContainer, Container dstContainer, Direction facing, int amount, boolean fixedAmount, ItemStack filterItem) {

        Direction srcDirection = facing.getOpposite();
        Direction dstDirection = facing;
        if (isFullContainer(dstContainer, dstDirection)) {
            return false;
        } else {
            for(int i = 0; i < srcContainer.getContainerSize(); ++i) {
                if (!srcContainer.getItem(i).isEmpty()) {
                    ItemStack srcStack = srcContainer.getItem(i).copy();

                    if (!canTakeItemFromContainer(dstContainer, srcContainer, srcStack, i, srcDirection)) continue;

                    // Check Filter
                    if (!FilterItemStack.of(filterItem).test(level, srcStack)) continue;
                    if (fixedAmount && srcContainer.getItem(i).getCount() < amount) continue;

                    ItemStack moveStack = addItem(srcContainer, dstContainer, srcContainer.removeItem(i, amount), dstDirection);
                    if (moveStack.isEmpty()) {
                        dstContainer.setChanged();
                        return true;
                    }

                    srcContainer.setItem(i, moveStack);
                }
            }

            return false;
        }
    }

    public static IntStream getSlots(Container container, Direction direction) {
        return container instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer)container).getSlotsForFace(direction)) : IntStream.range(0, container.getContainerSize());
    }

    public static boolean isFullContainer(Container container, Direction direction) {
        return getSlots(container, direction).allMatch((slot) -> {
            ItemStack itemStack = container.getItem(slot);
            return itemStack.getCount() >= itemStack.getMaxStackSize();
        });
    }
    public static boolean canPlaceItemInContainer(Container dstContainer, ItemStack stack, int slot, @Nullable Direction direction) {
        if (!dstContainer.canPlaceItem(slot, stack)) {
            return false;
        } else {
            if (dstContainer instanceof WorldlyContainer worldlyContainer) {
                if (!worldlyContainer.canPlaceItemThroughFace(slot, stack, direction)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean canTakeItemFromContainer(Container srcContainer, Container dstContainer, ItemStack stack, int slot, Direction direction) {
        if (!dstContainer.canTakeItem(srcContainer, slot, stack)) {
            return false;
        } else {
            if (dstContainer instanceof WorldlyContainer worldlyContainer) {
                if (!worldlyContainer.canTakeItemThroughFace(slot, stack, direction)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static ItemStack addItem(@Nullable Container srcContainer, Container dstContainer, ItemStack stack, @Nullable Direction direction) {
        int i;
        if (dstContainer instanceof WorldlyContainer worldlyContainer) {
            if (direction != null) {
                int[] dstSlots = worldlyContainer.getSlotsForFace(direction);

                for(i = 0; i < dstSlots.length && !stack.isEmpty(); ++i) {
                    stack = tryMoveInItem(dstContainer, stack, dstSlots[i], direction);
                }

                return stack;
            }
        }

        int j = dstContainer.getContainerSize();

        for(i = 0; i < j && !stack.isEmpty(); ++i) {
            stack = tryMoveInItem(dstContainer, stack, i, direction);
        }

        return stack;
    }

    public static ItemStack tryMoveInItem(Container dstContainer, ItemStack stack, int slot, @Nullable Direction direction) {
        ItemStack itemStack = dstContainer.getItem(slot);
        if (canPlaceItemInContainer(dstContainer, stack, slot, direction)) {
            boolean success = false;
            if (itemStack.isEmpty()) {
                dstContainer.setItem(slot, stack);
                stack = ItemStack.EMPTY;
                success = true;
            } else if (canMergeItems(itemStack, stack)) {
                int i = stack.getMaxStackSize() - itemStack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.shrink(j);
                itemStack.grow(j);
                success = j > 0;
            }

            if (success) {
                dstContainer.setChanged();
            }
        }
        return stack;
    }

    public static boolean canMergeItems(ItemStack stack1, ItemStack stack2) {
        return stack1.getCount() <= stack1.getMaxStackSize() && ItemStack.isSameItemSameTags(stack1, stack2);
    }
}
