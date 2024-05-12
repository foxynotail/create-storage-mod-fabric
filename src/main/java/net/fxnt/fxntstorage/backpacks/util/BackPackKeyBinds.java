package net.fxnt.fxntstorage.backpacks.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fxnt.fxntstorage.backpacks.upgrades.JetpackController;
import net.fxnt.fxntstorage.cache.BackPackShapeCache;
import net.fxnt.fxntstorage.init.ModKeyBinds;
import net.fxnt.fxntstorage.network.BackPackPackets;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class BackPackKeyBinds {

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Open BackPack
            if (ModKeyBinds.BACKPACK_OPEN_KEYBIND.consumeClick() && client.player != null) {
                BackPackNetworkHelper.sendKeyToServer(Util.OPEN_BACKPACK);
            }

            // Clear BackPack Shape Cache
            if (ModKeyBinds.CLEAR_BACKPACK_SHAPE_CACHE.consumeClick() && client.player != null) {
                Player player = client.player;
                BackPackShapeCache.clearCache();
                player.sendSystemMessage(Component.literal("BackPack Shape Cache Cleared"));
            }

            // BackPack Toggle Hover
            if (ModKeyBinds.BACKPACK_HOVER_KEYBIND.consumeClick() && client.player != null) {
                new JetpackController(client.player).toggleHover();
                //BackPackNetworkHelper.sendKeyToServer(Util.TOGGLE_HOVER);
            }
        });
    }
}
