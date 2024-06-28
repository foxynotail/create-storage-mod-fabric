package net.fxnt.fxntstorage.passer;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static net.fxnt.fxntstorage.passer.PasserBlock.FACING;

public class PasserSmartEntity extends SmartBlockEntity {
    public int lastTick = 0;
    public boolean doTick = false;
    public int updateEveryXTicks = 10;
    private Direction facing;
    public FilteringBehaviour filtering;
    public PasserSmartEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.SMART_PASSER_ENTITY, pos, blockState);
        this.facing = this.getBlockState().getValue(FACING);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(filtering =
                new FilteringBehaviour(this, new PasserFilteringBox()).showCount());
    }

    public <T extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockEntity blockEntity) {
        if (level != null && !level.isClientSide) {
            lastTick++;
            if (lastTick >= updateEveryXTicks) {
                lastTick = 0;
                doTick = true;
            }
            if (!doTick) return;
            doTick = false;

            this.facing = this.getBlockState().getValue(FACING);
            Storage<ItemVariant> srcContainer = PasserHelper.getStorage(level, blockPos, this.facing, true);
            if (srcContainer == null) {
                return;
            }
            Storage<ItemVariant> dstContainer = PasserHelper.getStorage(level, blockPos, this.facing, false);
            if (dstContainer == null) {
                return;
            }

            ItemVariant filterItem = ItemVariant.of(filtering.getFilter());
            long amount = filtering.getAmount();
            boolean fixedAmount = !filtering.upTo;

            PasserHelper.passItems(level, srcContainer, dstContainer, this.facing, amount, fixedAmount, filterItem); // Set to limit set by filter
        }
    }

}
