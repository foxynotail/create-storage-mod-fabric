package net.fxnt.fxntstorage.containers.util;

import net.fxnt.fxntstorage.containers.StorageBoxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ContainerActions {
    public static InteractionResult transferItemsToContainer(Level level, BlockPos blockPos, Player player, BlockHitResult hit, DirectionProperty facing) {

        if (player == null || level.isClientSide || player.isSpectator() || hit == null) return InteractionResult.FAIL;

        ItemStack heldItemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItemStack.isEmpty()) return InteractionResult.FAIL;

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof StorageBoxEntity storageBoxEntity) {
            if (storageBoxEntity.transferItemsFromPlayer(player)) {
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult transferItemsFromContainer(Level level, BlockPos blockPos, Player player, BlockHitResult hit, DirectionProperty facing) {

        if (player == null || level.isClientSide || player.isSpectator() || hit == null) return InteractionResult.FAIL;

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof StorageBoxEntity storageBoxEntity) {
            if (storageBoxEntity.transferItemsToPlayer(player)) {
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
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
