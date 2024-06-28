package net.fxnt.fxntstorage.passer;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class PasserHelper {

    @Nullable
    public static Storage<ItemVariant> getStorage(Level level, BlockPos blockPos, Direction facing, boolean source) {
        BlockPos containerPos = source ? blockPos.relative(facing.getOpposite()) : blockPos.relative(facing);
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity != null) {
            Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(level, blockPos, blockEntity.getBlockState(), blockEntity, facing);
            return itemStorage;
        }
        return null;
    }
    public static boolean passItems(Level level, Storage<ItemVariant> srcStorage, Storage<ItemVariant> dstStorage, Direction facing, long amount, boolean fixedAmount, ItemVariant filterItem) {

        try (Transaction transaction = Transaction.openOuter()) {
            long moved = 0;
            for (StorageView<ItemVariant> view : srcStorage) {
                if (view.isResourceBlank()) continue;
                ItemVariant variant = view.getResource();

                // Check Filter
                if (!FilterItemStack.of(filterItem.toStack()).test(level, variant.toStack())) continue;
                if (fixedAmount && view.getAmount() < amount) continue;

                long extracted = srcStorage.extract(variant, amount, transaction);
                if (extracted > 0) {
                    long inserted = dstStorage.insert(variant, extracted, transaction);
                    moved += inserted;

                    if (inserted < extracted) {
                        srcStorage.insert(variant, extracted - inserted, transaction);
                    }
                }
                if (moved >= amount) {
                    transaction.commit();
                    return true;
                }
            }
            transaction.commit();
        }
        return false;
    }
}
