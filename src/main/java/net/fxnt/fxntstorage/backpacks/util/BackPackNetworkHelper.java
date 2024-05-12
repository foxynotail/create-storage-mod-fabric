package net.fxnt.fxntstorage.backpacks.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fxnt.fxntstorage.FXNTStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BackPackNetworkHelper {
    public static final ResourceLocation UPDATE_BACKPACK = new ResourceLocation(FXNTStorage.MOD_ID, "update_backpack");
    public static final ResourceLocation BACKPACK_CLIENT_KEY = new ResourceLocation(FXNTStorage.MOD_ID, "backpack_client_key");
    public static final ResourceLocation UPGRADE_PICK_BLOCK = new ResourceLocation(FXNTStorage.MOD_ID, "upgrade_pick_block");
    public static final ResourceLocation JETPACK_FUEL_REQUEST = new ResourceLocation(FXNTStorage.MOD_ID, "jetpack_fuel_request");
    public static final ResourceLocation JETPACK_FUEL_DEPLETE = new ResourceLocation(FXNTStorage.MOD_ID, "jetpack_fuel_deplete");
    public static final ResourceLocation FLIGHT_UPGRADE_REQUEST = new ResourceLocation(FXNTStorage.MOD_ID, "flight_upgrade_request");
    public static final ResourceLocation SYNC_SLOT_COUNT = new ResourceLocation(FXNTStorage.MOD_ID, "sync_slot_count");
    public static final ResourceLocation SYNC_CONTAINER = new ResourceLocation(FXNTStorage.MOD_ID, "sync_container");


    public static void sendToServer(int slotIndex, ItemStack itemStack, byte backPackType, BlockPos blockPos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(slotIndex);
        buf.writeItem(itemStack);
        buf.writeByte(backPackType);
        buf.writeBlockPos(blockPos);
        ClientPlayNetworking.send(UPDATE_BACKPACK, buf);
    }

    public static void sendKeyToServer(byte key) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeByte(key);
        ClientPlayNetworking.send(BACKPACK_CLIENT_KEY, buf);
    }

    public static void doPickBlock(BlockPos blockPos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockPos);
        ClientPlayNetworking.send(UPGRADE_PICK_BLOCK, buf);
    }

    public static void updateJetPackFuel() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(JETPACK_FUEL_REQUEST, buf);
    }

    public static void depleteJetPackFuel() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(JETPACK_FUEL_DEPLETE, buf);
    }

    public static void checkHasFlightUpgrade() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(FLIGHT_UPGRADE_REQUEST, buf);
    }



    public static void syncSlotCount(ServerPlayer player, int containerId, int stateId, int slot, ItemStack stack) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(containerId);
        buf.writeVarInt(stateId);
        buf.writeVarInt(slot);
        writeItemStack(stack, buf);
        ServerPlayNetworking.send(player, SYNC_SLOT_COUNT, buf);
    }

    public static void syncContainer(ServerPlayer player, int containerId, int stateId, List<ItemStack> stacks, ItemStack stack) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(containerId);
        buf.writeVarInt(stateId);
        buf.writeShort(stacks.size());
        for (ItemStack itemStack : stacks) {
            writeItemStack(itemStack, buf);
        }
        buf.writeItem(stack);
        ServerPlayNetworking.send(player, SYNC_CONTAINER, buf);
    }

    private static void writeItemStack(ItemStack stack, FriendlyByteBuf buf) {
        if (stack.isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            Item item = stack.getItem();
            buf.writeVarInt(Item.getId(item));
            buf.writeVarInt(stack.getCount());
            CompoundTag compoundTag = null;
            if (stack.hasTag()) {
                compoundTag = stack.getOrCreateTag();
            }
            buf.writeNbt(compoundTag);
        }
    }


    public static ItemStack readItemStack(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            int itemId = buf.readVarInt();
            int itemCount = buf.readVarInt();
            ItemStack itemstack = new ItemStack(Item.byId(itemId), itemCount);
            itemstack.setTag(buf.readNbt());
            return itemstack;
        }
    }

}
