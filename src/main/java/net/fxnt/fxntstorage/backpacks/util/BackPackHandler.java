package net.fxnt.fxntstorage.backpacks.util;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fxnt.fxntstorage.backpacks.main.*;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BackPackHandler {
    public static void openBackpackFromInventory(ServerPlayer player, byte backPackType) {

        if (player.level().isClientSide) return;

        ItemStack itemStack = ItemStack.EMPTY;
        if (backPackType == Util.BACKPACK_ON_BACK) {
            itemStack = new BackPackHelper().getWornBackPackStack(player);
        } else if (backPackType == Util.BACKPACK_IN_HAND) {
            itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        }

        if (itemStack.getItem() instanceof BackPackItem) {

            ItemStack backPack = itemStack;
            BackPackContainer container = new BackPackContainer(itemStack);


            player.openMenu(new ExtendedScreenHandlerFactory() {
                @Override
                public Component getDisplayName() {
                    return container.getDisplayName();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    return new BackPackItemMenu(containerId, playerInventory, container, backPackType);
                }

                @Override
                public void writeScreenOpeningData(ServerPlayer serverPlayerEntity, FriendlyByteBuf buf) {
                    buf.writeItem(backPack);
                    buf.writeByte(backPackType);
                }
            });
        }
    }
    public static void openBackpackFromBlock(ServerPlayer player, BackPackEntity blockEntity) {
        if (player.level().isClientSide) return;
        Container container = blockEntity;
        player.openMenu(new ExtendedScreenHandlerFactory() {
            @Override
            public Component getDisplayName() {
                return blockEntity.getDisplayName();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new BackPackBlockMenu(containerId, playerInventory, container, Util.BACKPACK_AS_BLOCK);
            }

            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                buf.writeBlockPos(blockEntity.getBlockPos());
            }
        });
    }
}