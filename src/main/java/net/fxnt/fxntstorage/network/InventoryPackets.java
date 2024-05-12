package net.fxnt.fxntstorage.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fxnt.fxntstorage.FXNTStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class InventoryPackets {

    public static final ResourceLocation UPDATE_INVENTORY = new ResourceLocation(FXNTStorage.MOD_ID, "update_inventory");

    public static void register() {

        ServerPlayNetworking.registerGlobalReceiver(UPDATE_INVENTORY,
                (server, player, handler, buf, responseSender) -> {
                    // This lambda is executed when the server receives the packet
                    InventoryPackets packet = InventoryPackets.decode(buf);
                    server.execute(() -> {
                        packet.handle(player);
                    });
                }
        );
    }
    private final int slotIndex;
    private final ItemStack itemStack;

    public InventoryPackets(int slotIndex, ItemStack itemStack) {
        this.slotIndex = slotIndex;
        this.itemStack = itemStack;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(slotIndex);
        buf.writeItem(itemStack);
    }

    public static InventoryPackets decode(FriendlyByteBuf buf) {
        return new InventoryPackets(buf.readVarInt(), buf.readItem());
    }

    public void handle(ServerPlayer player) {
        // Handle the received packet on the server side, for example:
        // You can update the server's understanding of the client's inventory slot here.
        player.getInventory().setItem(slotIndex, itemStack);
    }

    public static void sendToServer(int slotIndex, ItemStack itemStack) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        new InventoryPackets(slotIndex, itemStack).encode(buf);
        ClientPlayNetworking.send(UPDATE_INVENTORY, buf);
    }
}