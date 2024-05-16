package net.fxnt.fxntstorage.passer;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static net.fxnt.fxntstorage.passer.PasserBlock.FACING;

public class PasserFilteringBox extends ValueBoxTransform.Sided {

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction side = getSide();
        float horizontalAngle = AngleHelper.horizontalAngle(side);
        Vec3 location = Vec3.ZERO;
        return switch (facing) {
            case DOWN -> VecHelper.rotateCentered(VecHelper.voxelSpace(8f, 12f, 12.5f), horizontalAngle, Direction.Axis.Y);
            case UP -> VecHelper.rotateCentered(VecHelper.voxelSpace(8f, 4f, 12.5f), horizontalAngle, Direction.Axis.Y);
            case NORTH -> switch (side) {
                case UP -> VecHelper.voxelSpace(8f, 12.5f, 12f);
                case DOWN -> VecHelper.voxelSpace(8f, 3.5f, 12f);
                case EAST -> VecHelper.voxelSpace(12.5f, 8f, 12f);
                case WEST -> VecHelper.voxelSpace(3.5f, 8f, 12f);
                case NORTH, SOUTH -> location;
            };
            case SOUTH -> switch (side) {
                case UP -> VecHelper.voxelSpace(8f, 12.5f, 4f);
                case DOWN -> VecHelper.voxelSpace(8f, 3.5f, 4f);
                case EAST -> VecHelper.voxelSpace(12.5f, 8f, 4f);
                case WEST -> VecHelper.voxelSpace(3.5f, 8f, 4f);
                case NORTH, SOUTH -> location;
            };
            case EAST -> switch (side) {
                case UP -> VecHelper.voxelSpace(4f, 12.5f, 8f);
                case DOWN -> VecHelper.voxelSpace(4f, 3.5f, 8f);
                case NORTH -> VecHelper.voxelSpace(4f, 8f, 3.5f);
                case SOUTH -> VecHelper.voxelSpace(4f, 8f, 12.5f);
                case EAST, WEST -> location;
            };
            case WEST -> switch (side) {
                case UP -> VecHelper.voxelSpace(12f, 12.5f, 8f);
                case DOWN -> VecHelper.voxelSpace(12f, 3.5f, 8f);
                case NORTH -> VecHelper.voxelSpace(12f, 8f, 3.5f);
                case SOUTH -> VecHelper.voxelSpace(12f, 8f, 12.5f);
                case EAST, WEST -> location;
            };
        };

    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        Direction facing = state.getValue(FACING);
        Direction side = getSide();
        float yRot = AngleHelper.horizontalAngle(side) + 180;
        float xRot = side == Direction.UP ? 90 : side == Direction.DOWN ? 270 : 0;

        switch (facing) {
            case NORTH -> yRot = side == Direction.UP ? 0 : yRot;
            case SOUTH -> yRot = side == Direction.DOWN ? 0 : yRot;
            case EAST -> yRot = side == Direction.UP ? 270 : side == Direction.DOWN ? 90 : yRot;
            case WEST -> yRot = side == Direction.UP ? 90 : side == Direction.DOWN ? 270 : yRot;
        }

        TransformStack.cast(ms).rotateY(yRot).rotateX(xRot);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction facing = state.getValue(FACING);
        if (facing == Direction.UP || facing == Direction.DOWN) {
            return direction.getAxis().isHorizontal();
        } else {
            if (direction != facing && direction != facing.getOpposite()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }

}