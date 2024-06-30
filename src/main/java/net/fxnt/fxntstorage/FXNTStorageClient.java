package net.fxnt.fxntstorage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fxnt.fxntstorage.backpacks.main.BackPackScreen;
import net.fxnt.fxntstorage.backpacks.renderer.BackPackRenderPlayer;
import net.fxnt.fxntstorage.backpacks.util.BackPackKeyBinds;
import net.fxnt.fxntstorage.containers.StorageBoxEntityRenderer;
import net.fxnt.fxntstorage.containers.StorageBoxScreen;
import net.fxnt.fxntstorage.init.ModBlocks;
import net.fxnt.fxntstorage.init.ModKeyBinds;
import net.fxnt.fxntstorage.init.ModMenuTypes;
import net.fxnt.fxntstorage.network.BackPackClientPackets;
import net.fxnt.fxntstorage.passer.PasserEntityRenderer;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntityRenderer;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

@Environment(EnvType.CLIENT)
public class FXNTStorageClient implements ClientModInitializer {

    public void onInitializeClient() {

        // Register Key Bindings
        ModKeyBinds.register();
        BackPackKeyBinds.register();
        BackPackClientPackets.register();

        // Container Screens
        MenuScreens.register(ModMenuTypes.STORAGE_BOX_MENU, StorageBoxScreen::createScreen);
        MenuScreens.register(ModMenuTypes.SIMPLE_STORAGE_BOX_MENU, SimpleStorageBoxScreen::createScreen);
        MenuScreens.register(ModMenuTypes.BACK_PACK_ITEM_MENU, BackPackScreen::createScreen);
        MenuScreens.register(ModMenuTypes.BACK_PACK_BLOCK_MENU, BackPackScreen::createScreen);

        // Render Text on Storage Boxes
        BlockEntityRendererRegistry.register(ModBlocks.STORAGE_BOX_ENTITY, StorageBoxEntityRenderer::new);
        BlockEntityRendererRegistry.register(ModBlocks.SIMPLE_STORAGE_BOX_ENTITY, SimpleStorageBoxEntityRenderer::new);
        BlockEntityRendererRegistry.register(ModBlocks.SMART_PASSER_ENTITY, PasserEntityRenderer::new);

        // Render BackPack on Player
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerRenderer renderer) {
                registrationHelper.register(new BackPackRenderPlayer((RenderLayerParent) renderer));
            }
        });
    }
}
