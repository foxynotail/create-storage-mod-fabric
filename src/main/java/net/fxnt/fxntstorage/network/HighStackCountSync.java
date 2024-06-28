package net.fxnt.fxntstorage.network;

import net.fxnt.fxntstorage.backpacks.util.BackPackNetworkHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;

public class HighStackCountSync implements ContainerSynchronizer {

    private final ServerPlayer player;

    public HighStackCountSync(ServerPlayer player) {
        this.player = player;
    }
    @Override
    public void sendInitialData(AbstractContainerMenu container, NonNullList<ItemStack> items, ItemStack carriedItem, int[] initialData) {
        BackPackNetworkHelper.syncContainer(player, container.containerId, container.getStateId(), items, carriedItem);
    }

    @Override
    public void sendSlotChange(AbstractContainerMenu container, int slot, ItemStack itemStack) {
        BackPackNetworkHelper.syncSlotCount(player, container.containerId, container.getStateId(), slot, itemStack);
    }

    @Override
    public void sendCarriedChange(AbstractContainerMenu containerMenu, ItemStack stack) {
        player.connection.send(new ClientboundContainerSetSlotPacket(-1, containerMenu.incrementStateId(), -1, stack));
    }

    @Override
    public void sendDataChange(AbstractContainerMenu container, int id, int value) {

    }
}
