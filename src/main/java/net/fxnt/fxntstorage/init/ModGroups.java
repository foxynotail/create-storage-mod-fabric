package net.fxnt.fxntstorage.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fxnt.fxntstorage.FXNTStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModGroups {
    public static final CreativeModeTab FXNTStorage_Tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(FXNTStorage.MOD_ID, "fxntstorage"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.fxntstorage.main"))
                    .icon(() -> new ItemStack(ModBlocks.STORAGE_BOX))
                    .displayItems((pParameters, entries) -> {

                        // Create Blocks
                        entries.accept(ModBlocks.STORAGE_BOX);
                        entries.accept(ModBlocks.ANDESITE_STORAGE_BOX);
                        entries.accept(ModBlocks.COPPER_STORAGE_BOX);
                        entries.accept(ModBlocks.BRASS_STORAGE_BOX);
                        entries.accept(ModBlocks.HARDENED_STORAGE_BOX);
                        entries.accept(ModBlocks.BACK_PACK);
                        entries.accept(ModBlocks.ANDESITE_BACK_PACK);
                        entries.accept(ModBlocks.COPPER_BACK_PACK);
                        entries.accept(ModBlocks.BRASS_BACK_PACK);
                        entries.accept(ModBlocks.HARDENED_BACK_PACK);
                        entries.accept(ModItems.BACK_PACK_BLANK_UPGRADE);
                        entries.accept(ModItems.BACK_PACK_MAGNET_UPGRADE);
                        entries.accept(ModItems.BACK_PACK_PICKBLOCK_UPGRADE);
                        entries.accept(ModItems.BACK_PACK_ITEMPICKUP_UPGRADE);
                        entries.accept(ModItems.BACK_PACK_FLIGHT_UPGRADE);
                        entries.accept(ModItems.BACK_PACK_REFILL_UPGRADE);
                        entries.accept(ModItems.BACK_PACK_FEEDER_UPGRADE);
                        entries.accept(ModItems.BACK_PACK_TOOLSWAP_UPGRADE);
                        entries.accept(ModItems.BACK_PACK_FALLDAMAGE_UPGRADE);

                    }).build());
    public static void register(){
        FXNTStorage.LOGGER.info("Registering Creative Tab Group for " + FXNTStorage.NAME);
    }

}
