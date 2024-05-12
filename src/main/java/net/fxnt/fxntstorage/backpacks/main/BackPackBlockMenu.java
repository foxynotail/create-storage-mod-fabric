package net.fxnt.fxntstorage.backpacks.main;

import net.fxnt.fxntstorage.init.ModMenuTypes;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BackPackBlockMenu extends BackPackMenu {

    public BackPackBlockMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, buf.readBlockPos()), Util.BACKPACK_AS_BLOCK);
    }

    public BackPackBlockMenu(int containerId, Inventory playerInventory, Container container, byte backPackType) {
        super(ModMenuTypes.BACK_PACK_BLOCK_MENU, containerId, playerInventory, container, backPackType);
    }
    private static Container getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntityAtPos = playerInventory.player.level().getBlockEntity(pos);

        if (blockEntityAtPos instanceof BackPackEntity backPackEntity) {
            return backPackEntity;
        } else {
            throw new IllegalStateException("Block entity is not correct or is null: " + blockEntityAtPos);
        }
    }
}
