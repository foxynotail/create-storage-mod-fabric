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

    public static void passItems(Level level, Storage<ItemVariant> srcStorage, Storage<ItemVariant> dstStorage, Direction facing, long amount, boolean fixedAmount, ItemVariant filterItem) {

        if (!srcStorage.supportsExtraction()) return;
        if (!dstStorage.supportsInsertion()) return;

        try (Transaction transaction = Transaction.openOuter()) {
            for (StorageView<ItemVariant> srcStack : srcStorage) {

                if (srcStack.isResourceBlank()) continue;
                ItemVariant srcItem = srcStack.getResource();

                // Check Filter
                if (!FilterItemStack.of(filterItem.toStack()).test(level, srcItem.toStack())) continue;
                if (fixedAmount && srcStack.getAmount() < amount) continue;

                // Check if can insert items into destination
                long insert = dstStorage.insert(srcItem, amount, transaction);
                boolean doExtract = false;
                if (insert > 0) {
                    if (fixedAmount && insert == amount) {
                        doExtract = true;
                    } else if (!fixedAmount) {
                        doExtract = true;
                    }
                }

                if (doExtract) {
                    long extract = srcStorage.extract(srcItem, insert, transaction);
                    if (extract == insert) {
                        transaction.commit();
                        return;
                    } else if (extract > 0 && extract < insert) {
                        // If not enough items to insert full amount, close transaction & reduce amount inserted
                        transaction.abort();
                        try(Transaction newTransaction = Transaction.openOuter()) {
                            insert = dstStorage.insert(srcItem, extract, newTransaction);
                            extract = srcStorage.extract(srcItem, extract, newTransaction);
                            if (insert == extract) {
                                newTransaction.commit();
                                return;
                            } else {
                                newTransaction.abort();
                                return;
                            }
                        }
                    }
                }
            }
            transaction.abort();
        }
    }

}
