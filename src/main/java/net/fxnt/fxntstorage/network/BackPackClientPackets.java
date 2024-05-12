package net.fxnt.fxntstorage.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.main.BackPackMenu;
import net.fxnt.fxntstorage.backpacks.upgrades.JetpackController;
import net.fxnt.fxntstorage.backpacks.util.BackPackNetworkHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BackPackClientPackets {
    public static final ResourceLocation JETPACK_FUEL_SEND = new ResourceLocation(FXNTStorage.MOD_ID, "jetpack_fuel_send");
    public static final ResourceLocation FLIGHT_UPGRADE_SEND = new ResourceLocation(FXNTStorage.MOD_ID, "flight_upgrade_send");
    public static final ResourceLocation SYNC_SLOT_COUNT = new ResourceLocation(FXNTStorage.MOD_ID, "sync_slot_count");
    public static final ResourceLocation SYNC_CONTAINER = new ResourceLocation(FXNTStorage.MOD_ID, "sync_container");

    public static void register() {

        ClientPlayNetworking.registerGlobalReceiver(JETPACK_FUEL_SEND,
                (client, handler, buf, responseSender) -> {
                    float fuelRemaining = buf.readFloat();
                    client.execute(() -> {
                        JetpackController.JetpackState.setFuelLevel(fuelRemaining);
                    });
        });
        ClientPlayNetworking.registerGlobalReceiver(FLIGHT_UPGRADE_SEND,
                (client, handler, buf, responseSender) -> {
                    boolean hasFlightUpgrade = buf.readBoolean();
                    client.execute(() -> {
                        JetpackController.JetpackState.setHasFlightUpgrade(hasFlightUpgrade);
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_SLOT_COUNT,
                (client, handler, buf, responseSender) -> {
                    int containerId = buf.readVarInt();
                    int stateId = buf.readVarInt();
                    int slot = buf.readVarInt();
                    ItemStack stack = BackPackNetworkHelper.readItemStack(buf);
                    client.execute(() -> {
                        if (client.player == null) return;
                        Player player = client.player;
                        if (player.containerMenu instanceof BackPackMenu && player.containerMenu.containerId == containerId) {
                            player.containerMenu.setItem(slot, stateId, stack);
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_CONTAINER,
                (client, handler, buf, responseSender) -> {
                    int containerId = buf.readVarInt();
                    int stateId = buf.readVarInt();
                    int size = buf.readShort();
                    List<ItemStack> stacks = NonNullList.withSize(size, ItemStack.EMPTY);
                    for (int i = 0; i < size; i++) {
                        stacks.set(i, BackPackNetworkHelper.readItemStack(buf));
                    }
                    ItemStack stack = buf.readItem();
                    client.execute(() -> {
                        if (client.player == null) return;
                        Player player = client.player;
                        if (player.containerMenu instanceof BackPackMenu && player.containerMenu.containerId == containerId) {
                            player.containerMenu.initializeContents(stateId, stacks, stack);
                        }
                    });
                });
    }
}
