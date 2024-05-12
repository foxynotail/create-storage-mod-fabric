package net.fxnt.fxntstorage.containers.util;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.fxnt.fxntstorage.containers.StorageBox;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class StorageBoxFilteringBox extends ValueBoxTransform.Sided {

    @Override
    protected Vec3 getSouthLocation() { return Vec3.ZERO; }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Direction side = getSide();
        float horizontalAngle = AngleHelper.horizontalAngle(side);
        return VecHelper.rotateCentered(VecHelper.voxelSpace(8, 10.8, 14.5f), horizontalAngle, Direction.Axis.Y);
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {

        Direction facing = StorageBox.getDirectionFacing(state);

        if (facing.getAxis().isVertical()) {
            super.rotate(state, ms);
            return;
        }

        if (state.getBlock() instanceof StorageBox) {
            super.rotate(state, ms);
            TransformStack.cast(ms).rotateX(0f);
            return;
        }
        float yRot = AngleHelper.horizontalAngle(StorageBox.getDirectionFacing(state)) + (facing == Direction.DOWN ? 180 : 0);
        TransformStack.cast(ms).rotateY(yRot).rotateX(facing == Direction.DOWN ? -90 : 90);
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction direction) {
        Direction facing = StorageBox.getDirectionFacing(state);
        if (facing == null) return false;
        if (facing.getAxis().isVertical()) return direction.getAxis().isHorizontal();
        return direction == facing;
    }

}