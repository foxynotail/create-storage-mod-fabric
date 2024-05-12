package net.fxnt.fxntstorage.containers.util;

import net.fxnt.fxntstorage.containers.StorageBoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ContainerActions {
    public static InteractionResult transferItemsToContainer(Level level, BlockPos blockPos, Player player, BlockHitResult hit, DirectionProperty facing) {

        if (player == null || level.isClientSide || player.isSpectator() || hit == null) return InteractionResult.FAIL;

        ItemStack heldItemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItemStack.isEmpty()) return InteractionResult.FAIL;

        Container container = (Container) level.getBlockEntity(blockPos);
        int inventorySlot = player.getInventory().selected;

        if (container instanceof StorageBoxEntity) {
            ((StorageBoxEntity) container).transferItems(heldItemStack, inventorySlot, player, false);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult transferItemsFromContainer(Level level, BlockPos blockPos, Player player, BlockHitResult hit, DirectionProperty facing) {

        if (player == null || level.isClientSide || player.isSpectator() || hit == null) return InteractionResult.FAIL;

        Container container = (Container) level.getBlockEntity(blockPos);
        // Get first non-empty slot from container
        int containerSlot = getFirstUsedItemSlotFromContainer(container);
        if (containerSlot < 0) return InteractionResult.FAIL;

        ItemStack itemStack = container.getItem(containerSlot);

        if (container instanceof StorageBoxEntity) {
            // Need to consider only receiving if match item player holding
            // Search for first slot that has matching item
            ItemStack playerItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!playerItem.isEmpty()) {
                containerSlot = getFirstMatchingItemSlotFromContainer(container, playerItem);
                // Don't take any items, even if they exist, if there aren't any matching the players main hand
                if (containerSlot < 0)  return InteractionResult.FAIL;
            }

            ((StorageBoxEntity) container).transferItems(itemStack, containerSlot, player, true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static int getFirstUsedItemSlotFromContainer(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static int getFirstMatchingItemSlotFromContainer(Container container, ItemStack itemStack) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty() && container.getItem(i).is(itemStack.getItem())) {
                return i;
            }
        }
        return -1;
    }
    @NotNull
    public static BlockHitResult rayTraceEyes(@NotNull Level level, @NotNull Player player, @NotNull BlockPos blockPos) {
        Vec3 eyePos = player.getEyePosition(1);
        Vec3 lookVector = player.getViewVector(1);
        Vec3 endPos = eyePos.add(lookVector.scale(eyePos.distanceTo(Vec3.atCenterOf(blockPos)) + 1));
        ClipContext context = new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        return level.clip(context);
    }
}
