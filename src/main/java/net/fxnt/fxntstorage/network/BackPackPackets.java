package net.fxnt.fxntstorage.network;

import com.simibubi.create.content.equipment.armor.BacktankItem;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpacks.main.BackPackContainer;
import net.fxnt.fxntstorage.backpacks.main.BackPackMenu;
import net.fxnt.fxntstorage.backpacks.upgrades.BackPackOnBackUpgradeHandler;
import net.fxnt.fxntstorage.backpacks.upgrades.JetpackController;
import net.fxnt.fxntstorage.backpacks.util.BackPackHandler;
import net.fxnt.fxntstorage.backpacks.util.BackPackHelper;
import net.fxnt.fxntstorage.config.Config;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackPackPackets {
    public static final ResourceLocation UPDATE_BACKPACK = new ResourceLocation(FXNTStorage.MOD_ID, "update_backpack");
    public static final ResourceLocation BACKPACK_CLIENT_KEY = new ResourceLocation(FXNTStorage.MOD_ID, "backpack_client_key");
    public static final ResourceLocation UPGRADE_PICK_BLOCK = new ResourceLocation(FXNTStorage.MOD_ID, "upgrade_pick_block");
    public static final ResourceLocation JETPACK_FUEL_REQUEST = new ResourceLocation(FXNTStorage.MOD_ID, "jetpack_fuel_request");
    public static final ResourceLocation JETPACK_FUEL_SEND = new ResourceLocation(FXNTStorage.MOD_ID, "jetpack_fuel_send");
    public static final ResourceLocation JETPACK_FUEL_DEPLETE = new ResourceLocation(FXNTStorage.MOD_ID, "jetpack_fuel_deplete");
    public static final ResourceLocation FLIGHT_UPGRADE_REQUEST = new ResourceLocation(FXNTStorage.MOD_ID, "flight_upgrade_request");
    public static final ResourceLocation FLIGHT_UPGRADE_SEND = new ResourceLocation(FXNTStorage.MOD_ID, "flight_upgrade_send");

    public static void register() {

        ServerPlayNetworking.registerGlobalReceiver(UPDATE_BACKPACK,
                (server, player, handler, buf, responseSender) -> {
                    int slot = buf.readVarInt();
                    ItemStack itemStack = buf.readItem();
                    byte backPackType = buf.readByte();
                    BlockPos blockPos = buf.readBlockPos();

                    server.execute(() -> {
                        //FXNTStorage.LOGGER.info("Server Handle {}", backPackType);
                        Container container;
                        switch (backPackType) {
                            case Util.BACKPACK_ON_BACK: {
                                ItemStack backPackStack = new BackPackHelper().getWornBackPackStack(player);
                                container = new BackPackContainer(backPackStack);
                                //container = BackPackContainerFactory.getInstance().getContainer((ServerPlayer)player, backPackStack);
                                break;
                            }
                            case Util.BACKPACK_IN_HAND: {
                                ItemStack backPackStack = player.getItemInHand(InteractionHand.MAIN_HAND);
                                container = new BackPackContainer(backPackStack);
                                //container = BackPackContainerFactory.getInstance().getContainer((ServerPlayer)player,backPackStack);
                                break;
                            }
                            case Util.BACKPACK_AS_BLOCK: {
                                container = new BackPackHelper().getBackPackContainerFromBlockPos(player.level(), blockPos);
                                break;
                            }
                            default: throw new RuntimeException("Backpack Type is wrong");
                        }
                        container.setItem(slot, itemStack);
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(BACKPACK_CLIENT_KEY,
                (server, player, handler, buf, responseSender) -> {
                    byte key = buf.readByte();
                    server.execute(() -> {
                        if (key == Util.OPEN_BACKPACK) BackPackHandler.openBackpackFromInventory(player, Util.BACKPACK_ON_BACK);
                        if (key == Util.TOGGLE_HOVER) new JetpackController(player).toggleHover();
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(UPGRADE_PICK_BLOCK,
                (server, player, handler, buf, responseSender) -> {
                    BlockPos blockPos = buf.readBlockPos();
                    server.execute(() -> {
                        new BackPackOnBackUpgradeHandler(player).applyPickBlockUpgrade(blockPos);
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(JETPACK_FUEL_REQUEST,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        float fuelRemaining = (float) calculateJetPackFuel(player);
                        FriendlyByteBuf responseBuf = PacketByteBufs.create();
                        responseBuf.writeFloat(fuelRemaining);
                        ServerPlayNetworking.send(player, JETPACK_FUEL_SEND, responseBuf);

                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(JETPACK_FUEL_DEPLETE,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        doDepleteJetPackFuel(player);
                    });
                }
        );
        ServerPlayNetworking.registerGlobalReceiver(FLIGHT_UPGRADE_REQUEST,
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        boolean hasFlightUpgrade = new BackPackOnBackUpgradeHandler(player).hasUpgrade(Util.FLIGHT_UPGRADE);
                        FriendlyByteBuf responseBuf = new FriendlyByteBuf(Unpooled.buffer());
                        responseBuf.writeBoolean(hasFlightUpgrade);
                        ServerPlayNetworking.send(player, FLIGHT_UPGRADE_SEND, responseBuf);

                    });
                }
        );
    }

    public static double calculateJetPackFuel(Player player) {
        BackPackHelper helper = new BackPackHelper();
        float fuelRemaining = 0.0f;
        if (helper.isWearingBackPack(player)) {
            Container container;
            if (player.containerMenu instanceof BackPackMenu backPackMenu && backPackMenu.backPackType == Util.BACKPACK_ON_BACK) {
                container = backPackMenu.container;
            } else {
                ItemStack itemStack = helper.getWornBackPackStack(player);
                container =  new BackPackContainer(itemStack);
            }

            if (container == null) return 0.0d;
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slotItem = container.getItem(i);
                if (slotItem.getItem() instanceof BacktankItem) {
                    if (BacktankUtil.hasAirRemaining(slotItem)) {
                        fuelRemaining += BacktankUtil.getAir(slotItem);
                    }
                }
            }
        }
        return fuelRemaining;
    }

    private static void doDepleteJetPackFuel(Player player) {
        BackPackHelper helper = new BackPackHelper();
        if (helper.isWearingBackPack(player)) {

            Container container;
            if (player.containerMenu instanceof BackPackMenu backPackMenu && backPackMenu.backPackType == Util.BACKPACK_ON_BACK) {
                container = backPackMenu.container;
            } else {
                ItemStack itemStack = helper.getWornBackPackStack(player);
                container =  new BackPackContainer(itemStack);
            }
            if (container == null) return;


            float fuelDepleteAmount = (float)(double) Config.JETPACK_FUEL_DEPLETION_AMOUNT.get();
            ItemStack firstFuelSource = ItemStack.EMPTY;
            float totalAirSupply = 0.0f;
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack slotItem = container.getItem(i);
                if (slotItem.getItem() instanceof BacktankItem) {
                    if (BacktankUtil.hasAirRemaining(slotItem)) {
                        firstFuelSource = slotItem;
                        totalAirSupply += BacktankUtil.getAir(slotItem);
                    }
                }
            }
            if (!firstFuelSource.isEmpty()) {
                CompoundTag tag = firstFuelSource.getOrCreateTag();
                int maxAir = BacktankUtil.maxAir(firstFuelSource);
                float air = BacktankUtil.getAir(firstFuelSource);
                float newAir = Math.max(air - fuelDepleteAmount, 0);
                tag.putFloat("Air", Math.min(newAir, maxAir));
                firstFuelSource.setTag(tag);

                float totalNewAirSupply = totalAirSupply - fuelDepleteAmount;

                float threshold = fuelDepleteAmount * 100;

                sendFuelWarning(player, totalAirSupply, totalNewAirSupply, threshold);
                sendFuelWarning(player, totalAirSupply, totalNewAirSupply, 1f);

            }
        }
    }

    private static void sendFuelWarning(Player player, float air, float newAir, float threshold) {
        if (newAir > threshold) return;
        if (air <= threshold) return;
        String message = "Jetpack fuel is almost depleted";
        if (threshold == 1f) message = "Jetpack fuel is depleted!";
        player.displayClientMessage(Component.literal(message), true);
    }

}
